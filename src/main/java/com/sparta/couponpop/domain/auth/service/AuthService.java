package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.auth.dto.request.LoginRequest;
import com.sparta.couponpop.domain.auth.dto.request.LogoutRequest;
import com.sparta.couponpop.domain.auth.dto.request.SignUpRequest;
import com.sparta.couponpop.domain.auth.dto.response.LoginResponse;
import com.sparta.couponpop.domain.auth.dto.response.SignUpResponse;
import com.sparta.couponpop.domain.auth.exception.AuthErrorCode;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final MemberFcmTokenRepository memberFcmTokenRepository;

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
    public LoginResponse login(LoginRequest loginRequest) {

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

    // TODO: JWT Token은 Transactional 영향 받지 않으므로, TransactionalEventListener 등 으로 원자적 처리 고려
    // TODO: 다만 두 작업이 원자적으로 처리되어야 할 필요성이 있는지는 고민 필요
    @Transactional
    public void logout(String authorizationHeader, LogoutRequest logoutRequest, AuthMember authMember) {

        expireToken(authorizationHeader);
        expireFcmToken(authMember.id(), logoutRequest.fcmToken());
    }

    // 로그아웃, 회원 탈퇴 시 블랙리스트 추가하여 토큰 만료 처리
    private void expireToken(String authorizationHeader) {

        String token = jwtProvider.resolveToken(authorizationHeader);
        if (token == null) {
            throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
        }

        long expirationMillis = jwtProvider.getExpirationMillis(token);

        tokenBlacklistService.blacklistToken(token, expirationMillis);
    }

    private void expireFcmToken(Long memberId, String fcmToken) {

        MemberFcmToken memberFcmToken = memberFcmTokenRepository
                .findByMemberIdAndFcmToken(memberId, fcmToken)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_FCM_TOKEN_NOT_FOUND));

        memberFcmTokenRepository.delete(memberFcmToken);
    }
}
