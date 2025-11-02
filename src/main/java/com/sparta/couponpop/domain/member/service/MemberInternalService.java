package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.dto.member.response.MemberDtoResponse;

public interface MemberInternalService {

    MemberDtoResponse getMemberById(Long memberId);
}

