package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.auth.dto.request.SignUpRequest;
import com.sparta.couponpop.domain.auth.dto.request.SignUpResponse;
import com.sparta.couponpop.domain.auth.exception.AuthErrorCode;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.service.MemberServiceApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberServiceApi memberService;
    private final PasswordEncoder passwordEncoder;

    public SignUpResponse signUp(SignUpRequest signUpRequest) {

        if (!signUpRequest.password().equals(signUpRequest.confirmPassword())) {
            throw new GlobalException(AuthErrorCode.PASSWORDS_NOT_MATCH);
        }

        Member createdMember = memberService.createMember(
                Member.signUp(signUpRequest.email(),
                        signUpRequest.username(),
                        passwordEncoder.encode(signUpRequest.password()),
                        signUpRequest.phoneNumber(),
                        signUpRequest.memberType()));

        return SignUpResponse.from(createdMember);
    }
}
