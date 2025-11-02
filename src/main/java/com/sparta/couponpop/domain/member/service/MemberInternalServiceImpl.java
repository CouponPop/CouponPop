package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.dto.member.response.GetMemberIdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberInternalServiceImpl implements MemberInternalService {

    /*
     * TODO
     *   - 회원 ID 조회
     *   - memberId 값을 받아서 실제 회원 정보를 조회한 후 회원의 ID 값을 GetMemberIdResponse 객체에 담아 반환하는 기능 구현 필요
     *   - 회원이 존재하지 않을 경우 예외 발생
     */
    @Override
    public GetMemberIdResponse getMemberId(Long memberId) {
        return GetMemberIdResponse.of(1L);
    }
}
