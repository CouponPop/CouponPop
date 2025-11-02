package com.sparta.couponpop.common.dto.member.response;

public record GetMemberIdResponse(
        Long memberId
) {

    public static GetMemberIdResponse of(Long memberId) {
        return new GetMemberIdResponse(memberId);
    }
}
