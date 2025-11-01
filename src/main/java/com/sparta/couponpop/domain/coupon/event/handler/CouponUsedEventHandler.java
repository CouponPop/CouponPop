package com.sparta.couponpop.domain.coupon.event.handler;

import com.sparta.couponpop.domain.coupon.event.CouponUsedEvent;
import com.sparta.couponpop.domain.couponhistory.service.CouponHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedEventHandler {

    private final CouponHistoryService couponHistoryService;

    /**
     * 쿠폰 사용 이벤트 발생 후, 독립 트랜잭션에서 DB 적재
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponUsedEvent(CouponUsedEvent event) {
        couponHistoryService.saveCouponHistory(event.toCouponUsedDto());
        // TODO : 쿠폰 사용 -> 손님(자기 자신)한테 알림
    }
}
