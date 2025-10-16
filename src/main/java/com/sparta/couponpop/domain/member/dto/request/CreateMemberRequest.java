package com.sparta.couponpop.domain.member.dto.request;

import com.sparta.couponpop.domain.member.enums.MemberType;

public record CreateMemberRequest(
        String email,
        String username,
        String encodedPassword,
        String phoneNumber,
        MemberType memberType
) {

    public static CreateMemberRequest of(String email, String username, String encodedPassword, String phoneNumber, MemberType memberType) {
        return new CreateMemberRequest(email, username, encodedPassword, phoneNumber, memberType);
    }
}
