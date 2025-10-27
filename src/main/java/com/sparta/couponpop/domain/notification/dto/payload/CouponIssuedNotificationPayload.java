package com.sparta.couponpop.domain.notification.dto.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CouponIssuedNotificationPayload(
        @NotBlank(message = "쿠폰명은 필수입니다.")
        String couponName,

        @NotBlank(message = "쿠폰 코드는 필수입니다.")
        String couponCode,

        @NotNull(message = "쿠폰 만료일은 필수입니다.")
        LocalDateTime expireAt
) {

    public static CouponIssuedNotificationPayload of(String couponName, String couponCode, LocalDateTime expireAt) {
        return new CouponIssuedNotificationPayload(couponName, couponCode, expireAt);
    }
}
