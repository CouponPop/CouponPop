package com.sparta.couponpop.domain.auth.dto.response;

import com.sparta.couponpop.domain.member.dto.response.CreateMemberResponse;

public record SignUpResponse(

        Long memberId,
        String email,
        String username,
        String phoneNumber,
        String memberType
) {
    public static SignUpResponse from(CreateMemberResponse createMemberResponse) {
        return new SignUpResponse(
                createMemberResponse.memberId(),
                createMemberResponse.email(),
                createMemberResponse.username(),
                createMemberResponse.phoneNumber(),
                createMemberResponse.memberType()
        );
    }
}
