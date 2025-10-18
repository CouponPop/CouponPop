package com.sparta.couponpop.domain.notification.dto.payload;

import java.time.LocalDateTime;

public record CouponIssuedNotificationPayload(
        String couponCode,
        String couponName,
        LocalDateTime expireAt
) {

    public static CouponIssuedNotificationPayload of(String couponName,
                                                     String couponCode,
                                                     LocalDateTime expireAt) {
        return new CouponIssuedNotificationPayload(couponCode, couponName, expireAt);
    }
}
