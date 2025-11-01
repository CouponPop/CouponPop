package com.sparta.couponpop.domain.notification.dto.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CouponIssuedNotificationPayload(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotBlank(message = "쿠폰명은 필수입니다.")
        String couponName,

        @NotBlank(message = "쿠폰 코드는 필수입니다.")
        String couponCode,

        @NotNull(message = "쿠폰 만료일은 필수입니다.")
        LocalDateTime expireAt
) {

    public static CouponIssuedNotificationPayload of(Long memberId, String couponName, String couponCode, LocalDateTime expireAt) {
        return new CouponIssuedNotificationPayload(memberId, couponName, couponCode, expireAt);
    }
}
