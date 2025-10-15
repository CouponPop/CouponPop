package com.sparta.couponpop.domain.auth.dto.request;

import lombok.Builder;

@Builder
public record SignUpResponse(

        Long memberId,
        String email,
        String username,
        String phoneNumber,
        String memberType
) {
}
