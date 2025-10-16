package com.sparta.couponpop.domain.auth.dto.response;

import com.sparta.couponpop.domain.member.entity.Member;

public record SignUpResponse(

        Long memberId,
        String email,
        String username,
        String phoneNumber,
        String memberType
) {
    public static SignUpResponse from(Member createdMember) {
        return new SignUpResponse(
                createdMember.getId(),
                createdMember.getEmail(),
                createdMember.getUsername(),
                createdMember.getPhoneNumber(),
                createdMember.getMemberType().name()
        );
    }
}
