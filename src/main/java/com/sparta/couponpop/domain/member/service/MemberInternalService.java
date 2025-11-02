package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.dto.member.response.GetMemberIdResponse;

public interface MemberInternalService {
    GetMemberIdResponse getMemberId(Long memberId);
}
