package com.sparta.couponpop.domain.member.dto.response;

import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;

public record MemberProfileResponse(
        Long id,
        String username,
        String email,
        String phoneNumber,
        MemberType memberType
) {
    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getMemberType()
        );
    }
}
