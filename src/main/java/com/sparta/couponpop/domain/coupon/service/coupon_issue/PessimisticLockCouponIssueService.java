package com.sparta.couponpop.domain.coupon.service.coupon_issue;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Primary
@Service
@RequiredArgsConstructor
public class PessimisticLockCouponIssueService implements CouponIssueFacade {

    private final CouponEventRepository couponEventRepository;
    private final DefaultCouponIssueService defaultCouponIssueService;

    @Transactional
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = validateEventPeriodAndTime(eventId, currentDateTime);
        defaultCouponIssueService.validateCouponDuplication(memberId, eventId);
        defaultCouponIssueService.saveCouponIssue(memberId, currentDateTime, event);
    }

    public CouponEvent validateEventPeriodAndTime(Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = couponEventRepository.findByEventIdForUpdate(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));

        event.validateIssuable(currentDateTime);
        return event;
    }

}
