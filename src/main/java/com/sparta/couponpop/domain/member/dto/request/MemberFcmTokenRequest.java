package com.sparta.couponpop.domain.member.dto.request;

import lombok.Builder;

@Builder
public record MemberFcmTokenRequest(
        String fcmToken,
        String deviceType,
        String deviceIdentifier
) {
}
