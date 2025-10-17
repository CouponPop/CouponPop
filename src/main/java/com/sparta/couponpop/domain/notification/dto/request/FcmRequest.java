package com.sparta.couponpop.domain.notification.dto.request;

import lombok.Builder;

@Builder
public record FcmRequest(
        String token,
        String title,
        String body
) {
}
