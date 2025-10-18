package com.sparta.couponpop.domain.notification.dto.request;

public record FcmRequest(
        String token,
        String title,
        String body
) {
}
