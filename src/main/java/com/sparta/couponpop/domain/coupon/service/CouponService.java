package com.sparta.couponpop.domain.coupon.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.dto.request.MemberIssuedCouponCursor;
import com.sparta.couponpop.domain.coupon.dto.response.CouponDetailResponse;
import com.sparta.couponpop.domain.coupon.dto.response.IssuedCouponListResponse;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.event.CouponUsedEvent;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.db.CouponRepository;
import com.sparta.couponpop.domain.coupon.repository.redis.TemporaryCouponCodeRepository;
import com.sparta.couponpop.domain.coupon.repository.db.dto.CouponSummaryInfoProjection;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.exception.StoreErrorCode;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private static final long TEMP_CODE_TTL_SECONDS = 600L;

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final TemporaryCouponCodeRepository temporaryCouponCodeRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void issueEventCoupon(Long memberId, Long storeId, Long eventId, LocalDateTime issuedTime) {
        // 매장 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));
        CouponEvent event = validateEventBelongsToStore(eventId, store);

        // 이벤트 유효성 검증
        event.validateIssuable(issuedTime);

        // 쿠폰 중복 수령 방지
        if (couponRepository.existsByMemberIdAndCouponEventId(memberId, eventId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 발급 처리
        event.issueCoupon();

        // TODO : 쿠폰 생성 시 만료 시간 누락
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 쿠폰 생성 및 저장
        Coupon issuedCoupon = Coupon.createIssuedCoupon(member, event, issuedTime);
        couponRepository.save(issuedCoupon);
    }

    /**
     * 쿠폰 상세 조회 및 임시 사용 코드 발급
     *
     * <p>쿠폰 ID와 회원 ID를 기반으로 쿠폰을 조회하고, 해당 회원이 소유한 쿠폰인지 검증합니다.
     * 사용 가능한 쿠폰인 경우, 임시 코드(UUID)를 발급하고 Redis에 10분 TTL로 저장합니다.
     *
     * <p>임시 코드는 Optional로 반환되며, 사용 불가 쿠폰의 경우 포함되지 않습니다.
     *
     * @param couponId 조회할 쿠폰 ID
     * @param memberId 요청한 회원 ID
     * @return 쿠폰 상세 정보와 임시 코드(Optional)가 포함된 응답
     * @throws GlobalException 쿠폰이 존재하지 않거나 접근 권한이 없는 경우 발생
     */
    public CouponDetailResponse getCouponDetail(Long couponId, Long memberId) {
        Coupon coupon = couponRepository.findByIdWithCouponEventAndStore(couponId)
                .orElseThrow(() -> new GlobalException(CouponErrorCode.COUPON_NOT_FOUND));

        if (!coupon.getMember().getId().equals(memberId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ACCESS_DENIED);
        }

        // TODO : 쿠폰 만료 여부 표시를 날짜로 할지 일관 처리 할지
        // 임시 코드 발급 + Redis 저장 (TTL 10분)
        Optional<String> tempCode = Optional.empty();
        if (coupon.isAvailable()) {
            String code = UUID.randomUUID().toString();
            temporaryCouponCodeRepository.setTemporaryCoupon(couponId, code, coupon.getCouponCode(), TEMP_CODE_TTL_SECONDS);
            tempCode = Optional.of(code);
            log.info("임시 쿠폰 Redis 저장");
        }
        return CouponDetailResponse.from(coupon, tempCode);
    }

    /**
     * 쿠폰 사용 처리 메서드.
     *
     * <p>사용자가 QR 코드(임시 코드)를 통해 쿠폰을 사용할 때 호출되며,
     * 다음 과정을 순차적으로 수행한다.</p>
     *
     * <ol>
     *     <li>Redis에 저장된 임시 코드(QR) 유효성 검증</li>
     *     <li>비관적 락 기반 쿠폰 + 이벤트 조회 (중복 사용 방어)</li>
     *     <li>이벤트 진행 상태 검증 (시작/종료 시간 포함)</li>
     *     <li>쿠폰 소유자 검증</li>
     *     <li>쿠폰 사용 가능 상태 검증 (USED, EXPIRED 등 방지)</li>
     *     <li>쿠폰 사용 처리 및 사용 시각 기록</li>
     *     <li>Redis에 저장된 임시 코드 삭제</li>
     * </ol>
     *
     * <p><b>동시성 처리:</b> 현재는 비관적 락을 사용 중이며,
     * 추후 Redis 분산 락(Redisson) 또는 낙관적 락(@Version)을 통한 개선 예정.</p>
     *
     * @param couponId 쿠폰 ID
     * @param qrCode   쿠폰 사용 시 전달된 QR 코드(임시 코드)
     * @param memberId 쿠폰 사용 요청자의 회원 ID
     * @param usedAt   쿠폰 사용 시각
     * @throws GlobalException 유효하지 않은 코드, 이벤트 기간 종료, 소유자 불일치, 이미 사용된 쿠폰 등 예외 발생
     */
    // TODO : 도메인 로직의 핵심성과 비핵심성을 분리
    @Transactional
    public void useCoupon(Long couponId, String qrCode, Long memberId, LocalDateTime usedAt) {

        /**
         * TODO: Redis에서 임시 코드 원자적 검증 + 조회
         * - 현재는 validateTemporaryCoupon 후 delete 순서
         * - getAndDelete 또는 Lua 스크립트로 동시성 안전하게 처리
         */
        //  Redis 임시 코드 검증
        if (!temporaryCouponCodeRepository.validateTemporaryCoupon(couponId, qrCode)) {
            throw new GlobalException(CouponErrorCode.COUPON_INVALID_TEMP_CODE);
        }

        /**
         * TODO: [동시성 개선]
         * 현재는 비관적 락(Pessimistic Lock)으로 중복 사용을 방어하고 있음.
         * 추후 Redis 기반 분산 락 또는 낙관적 락(Optimistic Lock)으로 개선 예정.
         * ex) Redisson, @Version 등 활용 가능
         */
        // 쿠폰 + 이벤트 조회
        Coupon coupon = couponRepository.findByIdWithCouponEventForUpdate(couponId)
                .orElseThrow(() -> new GlobalException(CouponErrorCode.COUPON_NOT_FOUND));

        // 이벤트 상태 검증
        coupon.getCouponEvent().validateInProgress(usedAt);

        // 쿠폰 소유자 검증
        if (!coupon.getMember().getId().equals(memberId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ACCESS_DENIED);
        }

        // 쿠폰 사용
        coupon.use(usedAt);

        // Redis 임시 코드 삭제
        temporaryCouponCodeRepository.deleteTemporaryCoupon(couponId, qrCode);

        eventPublisher.publishEvent(CouponUsedEvent.of(
                couponId,
                memberId,
                coupon.getCouponEvent().getStore().getId(),
                coupon.getCouponEvent().getId()
        ));
    }

    /**
     * 사용자가 발급받은 쿠폰 목록을 페이지네이션 방식으로 조회합니다.
     *
     * <p>조회 시 pageSize + 1개를 가져와서 다음 페이지 존재 여부(hasNext)를 판단합니다.</p>
     *
     * @param memberId 조회 대상 회원 ID
     * @param status   조회할 쿠폰 상태
     * @param cursor   이전 페이지 마지막 쿠폰 정보로, 다음 페이지 조회 기준이 됩니다.
     *                 첫 페이지 조회 시 null 또는 MemberIssuedCouponCursor.first() 사용
     * @param pageSize 한 페이지에 보여줄 최대 쿠폰 수
     * @return 쿠폰 목록과 페이지 정보가 포함된 IssuedCouponListResponse
     */
    public IssuedCouponListResponse getIssuedCoupons(Long memberId, CouponStatus status, MemberIssuedCouponCursor cursor, int pageSize) {
        // pageSize + 1 조회 (hasNext 확인용)
        List<CouponSummaryInfoProjection> coupons = couponRepository.findAllByMemberIdWithEventAndStore(
                memberId, status, cursor.lastEventEndAt(), cursor.lastCouponId(), pageSize + 1
        );

        return IssuedCouponListResponse.of(coupons, pageSize);
    }

    private CouponEvent validateEventBelongsToStore(Long eventId, Store store) {
        // 이벤트 존재 여부 검증
        // TODO : Pessimistic Lock 으로 임시 동시성 처리. 추후 성능 비교 후 동시성 제어하기
        CouponEvent event = couponEventRepository.findEventForUpdate(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));

        // 이벤트가 해당 매장에서 진행 중인지 검증
        if (!event.getStore().getId().equals(store.getId())) {
            throw new GlobalException(CouponEventErrorCode.EVENT_NOT_BELONG_TO_STORE);
        }

        return event;
    }
}
