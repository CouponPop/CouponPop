package com.sparta.couponpop.domain.member.dto.response;

import com.sparta.couponpop.domain.member.entity.Member;

public record CreateMemberResponse(

        Long memberId,
        String email,
        String username,
        String phoneNumber,
        String memberType
) {
    public static CreateMemberResponse from(Member createdMember) {
        return new CreateMemberResponse(
                createdMember.getId(),
                createdMember.getEmail(),
                createdMember.getUsername(),
                createdMember.getPhoneNumber(),
                createdMember.getMemberType().name()
        );
    }
}