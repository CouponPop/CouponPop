package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.dto.member.response.MemberResponse;

public interface MemberInternalService {

    MemberResponse getMemberById(Long memberId);
}

