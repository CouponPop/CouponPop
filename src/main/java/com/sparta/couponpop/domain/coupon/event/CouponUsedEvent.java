package com.sparta.couponpop.domain.coupon.event;

import java.time.LocalDateTime;

public record CouponUsedEvent(
        Long memberId,
        Long couponId,
        Long storeId,
        String dong,
        LocalDateTime usedAt
) {

    public static CouponUsedEvent of(Long memberId, Long couponId, Long storeId, String dong, LocalDateTime usedAt) {
        return new CouponUsedEvent(memberId, couponId, storeId, dong, usedAt);
    }
}
