package com.sparta.couponpop.domain.auth.dto.request;

import com.sparta.couponpop.domain.member.entity.Member;
import lombok.Builder;

@Builder
public record SignUpResponse(

        Long memberId,
        String email,
        String username,
        String phoneNumber,
        String memberType
) {
    public static SignUpResponse from(Member createdMember) {
        return SignUpResponse.builder()
                .memberId(createdMember.getId())
                .email(createdMember.getEmail())
                .username(createdMember.getUsername())
                .phoneNumber(createdMember.getPhoneNumber())
                .memberType(createdMember.getMemberType().name())
                .build();
    }
}
