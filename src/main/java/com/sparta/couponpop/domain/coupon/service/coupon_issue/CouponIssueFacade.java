package com.sparta.couponpop.domain.coupon.service.coupon_issue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueFacade {

    private final CouponIssueService couponIssueService;

    public void issueRequestV1(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        log.info("락 획득");
        synchronized (this) {
            couponIssueService.issueCoupon(memberId, eventId, currentDateTime);
        }
        log.info("락 반납. 쿠폰 발급 완료");
    }
}
