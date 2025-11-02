package com.sparta.couponpop.domain.coupon.service.coupon_issue;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.db.CouponRepository;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DefaultCouponIssueService implements CouponIssueFacade {

    // ****** Coupon Domain ****** //
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;

    // TODO : 이벤트가 해당 매장에서 진행 중인지 검증해야 할까?
    /*
        if (!event.getStoreId().equals(store.getId())) {
            throw new GlobalException(CouponEventErrorCode.EVENT_NOT_BELONG_TO_STORE);
        }
     */
    @Transactional
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        // 쿠폰 중복 수령 검증
        validateCouponDuplication(memberId, eventId);
        // 이벤트 기간 검증
        CouponEvent event = validateEventPeriodAndTime(eventId, currentDateTime);
        // 쿠폰 발급 및 쿠폰 저장
        saveCouponIssue(memberId, currentDateTime, event);
    }

    public void validateCouponDuplication(Long memberId, Long eventId) {
        if (couponRepository.existsByMemberIdAndCouponEventId(memberId, eventId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }
    }

    public CouponEvent validateEventPeriodAndTime(Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));

        event.validateIssuable(currentDateTime);
        return event;
    }

    public void saveCouponIssue(Long memberId, LocalDateTime currentDateTime, CouponEvent event) {
        event.issueCoupon();
        Coupon issuedCoupon = Coupon.createIssuedCoupon(memberId, event.getStoreId(), event, currentDateTime);
        couponRepository.save(issuedCoupon);
    }
}
