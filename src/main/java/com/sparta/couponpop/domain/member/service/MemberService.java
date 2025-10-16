package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.exception.CommonErrorCode;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.dto.request.CreateMemberRequest;
import com.sparta.couponpop.domain.member.dto.response.CreateMemberResponse;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberServiceApi {

    private static final String DUPLICATION_ERROR_TARGET = "email";

    private final MemberRepository memberRepository;

    @Transactional
    public CreateMemberResponse createMember(CreateMemberRequest createMemberRequest) {

        if (memberRepository.existsByEmail(createMemberRequest.email())) {
            throw new GlobalException(MemberErrorCode.EMAIL_DUPLICATED);
        }

        Member newMember = Member.signUp(createMemberRequest.email(),
                createMemberRequest.username(),
                createMemberRequest.encodedPassword(),
                createMemberRequest.phoneNumber(),
                createMemberRequest.memberType());

        try {
            Member savedMember = memberRepository.save(newMember);
            memberRepository.flush(); // 제약조건 즉시 검증을 위해 사용
            return CreateMemberResponse.from(savedMember);
        } catch (DataIntegrityViolationException e) {
            if (e.getMostSpecificCause().getMessage().contains(DUPLICATION_ERROR_TARGET)) {
                throw new GlobalException(MemberErrorCode.EMAIL_DUPLICATED);
            }

            throw new GlobalException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
