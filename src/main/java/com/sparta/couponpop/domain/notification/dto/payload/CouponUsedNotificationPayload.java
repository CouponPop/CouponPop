package com.sparta.couponpop.domain.notification.dto.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CouponUsedNotificationPayload(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long customerId,

        @NotBlank(message = "쿠폰명은 필수입니다.")
        String couponName,

        @NotBlank(message = "매장명은 필수입니다.")
        String storeName,

        @NotNull(message = "쿠폰 만료일은 필수입니다.")
        LocalDateTime expireAt
) {

    public static CouponUsedNotificationPayload of(Long customerId, String couponName, String storeName, LocalDateTime expireAt) {
        return new CouponUsedNotificationPayload(customerId, couponName, storeName, expireAt);
    }
}
