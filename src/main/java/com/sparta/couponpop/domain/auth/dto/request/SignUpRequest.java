package com.sparta.couponpop.domain.auth.dto.request;

import com.sparta.couponpop.domain.member.enums.MemberType;

public record SignUpRequest(

        String email,
        String username,
        String password,
        String confirmPassword,
        String phoneNumber,
        MemberType memberType
) {
}