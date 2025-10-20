package com.sparta.couponpop.domain.coupon.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public void issueEventCoupon(Long memberId, Long storeId, Long eventId, LocalDateTime issuedTime) {
        // 매장 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));
        CouponEvent event = validateEventBelongsToStore(eventId, store);

        // 이벤트 유효성 검증
        event.validateIssuable(LocalDateTime.now());

        // 쿠폰 중복 수령 방지
        if (couponRepository.existsByMemberIdAndCouponEventId(memberId, eventId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 발급 처리
        event.issue();

        // 쿠폰 생성 및 저장
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        Coupon issuedCoupon = Coupon.createIssuedCoupon(member, event, issuedTime);
        couponRepository.save(issuedCoupon);
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
