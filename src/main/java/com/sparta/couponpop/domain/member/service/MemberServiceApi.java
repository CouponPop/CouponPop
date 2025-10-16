package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.domain.member.dto.request.CreateMemberRequest;
import com.sparta.couponpop.domain.member.dto.response.CreateMemberResponse;

public interface MemberServiceApi {

    CreateMemberResponse createMember(CreateMemberRequest createMemberRequest);
}
