package com.sparta.couponpop.domain.coupon.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.dto.response.CouponDetailResponse;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
import com.sparta.couponpop.domain.coupon.repository.TemporaryCouponCodeRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        event.issue();

        // TODO : 쿠폰 생성 시 만료 시간 누락
        // 쿠폰 생성 및 저장
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
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
