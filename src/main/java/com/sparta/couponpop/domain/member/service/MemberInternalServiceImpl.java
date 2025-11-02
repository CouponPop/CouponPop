package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.dto.member.response.MemberDtoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberInternalServiceImpl implements MemberInternalService {

    @Override
    @Transactional(readOnly = true)
    public MemberDtoResponse getMemberById(Long memberId) {
        // 기존 구현 메서드
        // Member member = memberRepository.findById(memberId)
        //         .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        //
        // return MemberDtoResponse.from(member);
        
        // TODO: Mock 데이터로 임시 반환 
        return new MemberDtoResponse(memberId, "mock_user_" + memberId);
    }
}

