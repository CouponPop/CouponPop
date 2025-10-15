package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberServiceApi {

    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(Member newMember) {
        if (memberRepository.existsByEmail(newMember.getEmail()))
            throw new GlobalException(MemberErrorCode.EMAIL_DUPLICATED);

        return memberRepository.save(newMember);
    }
}
