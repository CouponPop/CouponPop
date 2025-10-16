package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.auth.dto.request.SignUpRequest;
import com.sparta.couponpop.domain.auth.dto.response.SignUpResponse;
import com.sparta.couponpop.domain.auth.exception.AuthErrorCode;
import com.sparta.couponpop.domain.member.dto.request.CreateMemberRequest;
import com.sparta.couponpop.domain.member.dto.response.CreateMemberResponse;
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

        String encodedPassword = passwordEncoder.encode(signUpRequest.password());
        CreateMemberRequest createMemberRequest = CreateMemberRequest.of(
                signUpRequest.email(),
                signUpRequest.username(),
                encodedPassword,
                signUpRequest.phoneNumber(),
                signUpRequest.memberType()
        );

        CreateMemberResponse createMemberResponse = memberService.createMember(createMemberRequest);

        return SignUpResponse.from(createMemberResponse);
    }
}
