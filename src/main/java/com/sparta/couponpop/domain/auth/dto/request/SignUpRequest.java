package com.sparta.couponpop.domain.auth.dto.request;

public record SignUpRequest(

        String email,
        String username,
        String password,
        String confirmPassword,
        String phoneNumber,
        String memberType
) {
}