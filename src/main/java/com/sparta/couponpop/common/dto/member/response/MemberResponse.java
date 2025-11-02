package com.sparta.couponpop.common.dto.member.response;

import com.sparta.couponpop.domain.member.entity.Member;

/**
 * Store 도메인에서 사용하는 Member 정보 조회 응답 DTO
 * 요청 도메인: store
 * 담당자: (담당자 이름)
 */
public record MemberResponse(
        Long id,
        String username
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getUsername()
        );
    }
}

