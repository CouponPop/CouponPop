package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.domain.auth.dto.request.LoginRequest;
import com.sparta.couponpop.domain.auth.dto.request.SignUpRequest;
import com.sparta.couponpop.domain.auth.dto.response.LoginResponse;
import com.sparta.couponpop.domain.auth.dto.response.SignUpResponse;
import com.sparta.couponpop.domain.auth.exception.AuthErrorCode;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {

        if (!signUpRequest.password().equals(signUpRequest.confirmPassword())) {
            throw new GlobalException(AuthErrorCode.PASSWORDS_NOT_MATCH);
        }

        if (memberRepository.existsByEmail(signUpRequest.email())) {
            throw new GlobalException(MemberErrorCode.EMAIL_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.password());

        Member newMember = Member.signUp(signUpRequest.email(),
                signUpRequest.username(),
                encodedPassword,
                signUpRequest.phoneNumber(),
                signUpRequest.memberType());

        Member savedMember;
        try {
            savedMember = memberRepository.saveAndFlush(newMember); // 제약조건 즉시 검증을 위해 사용
        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(MemberErrorCode.EMAIL_DUPLICATED);
        }

        return SignUpResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(@Valid LoginRequest loginRequest) {

        Member loginMember = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new GlobalException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.password(), loginMember.getPassword())) {
            throw new GlobalException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.createAccessToken(
                loginMember.getId(),
                loginMember.getUsername(),
                loginMember.getMemberType());

        return LoginResponse.from(accessToken);
    }
}
