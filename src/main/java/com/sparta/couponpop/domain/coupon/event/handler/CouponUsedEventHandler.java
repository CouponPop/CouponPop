package com.sparta.couponpop.domain.coupon.event.handler;

import com.sparta.couponpop.common.elasticsearch.document.CouponUsageDocument;
import com.sparta.couponpop.common.elasticsearch.repository.CouponUsageRepository;
import com.sparta.couponpop.domain.coupon.event.CouponUsedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedEventHandler {

    private final CouponUsageRepository couponUsageRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponUsedEvent(CouponUsedEvent event) {
        log.info("쿠폰 사용 통계성 데이터 ES 적재");
        CouponUsageDocument doc = CouponUsageDocument.create(event.memberId(), event.couponId(), event.storeId(), event.dong(), event.usedAt());
        couponUsageRepository.save(doc);
    }
}
