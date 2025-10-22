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
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberFcmTokenRepository memberFcmTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private AuthMember testAuthMember;
    private LogoutRequest testLogoutRequest;
    private String testAuthorizationHeader;
    private String testToken;
    private long testExpirationMillis;

    @BeforeEach
    void setUp() {
        testAuthMember = AuthMember.from(1L, "테스트이름", MemberType.CUSTOMER);
        testLogoutRequest = new LogoutRequest("testFcmToken");
        testAuthorizationHeader = "Bearer " + "testAccessToken";
        testToken = "testAccessToken";
        testExpirationMillis = System.currentTimeMillis() + 3600000;
    }

    @Test
    @DisplayName("회원가입 정보를 받아 멤버를 생성한다.")
    void signUpSuccess() {

        // given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("test@example.com")
                .username("테스트이름")
                .password("test1234!")
                .confirmPassword("test1234!")
                .phoneNumber("01012345678")
                .memberType(MemberType.CUSTOMER)
                .build();


        Member createdMember = Member.signUp("test@example.com",
                "테스트이름",
                "encodedPassword",
                "01012345678",
                MemberType.CUSTOMER
        );

        given(memberRepository.saveAndFlush(any(Member.class))).willReturn(createdMember);

        // when
        SignUpResponse response = authService.signUp(signUpRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(signUpRequest.email());
        assertThat(response.username()).isEqualTo(signUpRequest.username());
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다르면 비밀번호 불일치 예외가 발생한다.")
    void signUpFailurePasswordsNotMatch() {

        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .username("테스트이름")
                .password("test1234!")
                .confirmPassword("test1234@")
                .phoneNumber("01012345678")
                .memberType(MemberType.CUSTOMER)
                .build();

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            authService.signUp(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.PASSWORDS_NOT_MATCH);
    }

    @Test
    @DisplayName("로그인 정보를 받아 토큰 반환에 성공한다.")
    void loginSuccess() {

        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "test1234!");

        Member findMember = Member.signUp(
                "test@example.com",
                "테스트이름",
                "encodedPassword",
                "01012345678",
                MemberType.CUSTOMER
        );

        String expectedToken = "mockedJwtToken";

        // Mock 객체의 행동을 정의합니다.
        given(memberRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(findMember));
        given(passwordEncoder.matches(loginRequest.password(), findMember.getPassword())).willReturn(true);
        given(jwtProvider.createAccessToken(findMember.getId(), findMember.getUsername(), findMember.getMemberType())).willReturn(expectedToken);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("로그인 요청의 이메일을 가진 멤버가 존재하지 않으면 예외가 발생한다.")
    void loginFailureUserNotFound() {

        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "test1234!");

        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            authService.login(loginRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtProvider, never()).createAccessToken(any(), any(), any());
    }

    @Test
    @DisplayName("로그인 요청의 비밀번호와 저장된 비밀번호가 일치하지 않으면 예외가 발생한다.")
    void login_Failure_PasswordMismatch() {

        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "test1234@");
        Member findMember = Member.signUp(
                "test@example.com",
                "테스트이름",
                "encodedPassword",
                "01012345678",
                MemberType.CUSTOMER
        );

        given(memberRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(findMember));
        given(passwordEncoder.matches(loginRequest.password(), findMember.getPassword())).willReturn(false);

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            authService.login(loginRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
        verify(jwtProvider, never()).createAccessToken(any(), any(), any());
    }

    @Test
    @DisplayName("로그아웃 정보를 받아 로그아웃에 성공한다.")
    void logoutSuccess() {

        // given
        MemberFcmToken mockFcmToken = TestUtils.createEntity(MemberFcmToken.class, Map.of("fcmToken", "testFcmToken"));// 임시 객체

        given(jwtProvider.resolveToken(testAuthorizationHeader)).willReturn(testToken);
        given(jwtProvider.getExpirationMillis(testToken)).willReturn(testExpirationMillis);

        given(memberFcmTokenRepository.findByMemberIdAndFcmToken(testAuthMember.id(), testLogoutRequest.fcmToken()))
                .willReturn(Optional.of(mockFcmToken));

        // when
        authService.logout(testAuthorizationHeader, testLogoutRequest, testAuthMember);

        // then
        // 1. expireToken() 검증
        verify(jwtProvider).resolveToken(testAuthorizationHeader);
        verify(jwtProvider).getExpirationMillis(testToken);
        verify(tokenBlacklistService).blacklistToken(testToken, testExpirationMillis);

        // 2. expireFcmToken() 검증
        verify(memberFcmTokenRepository).findByMemberIdAndFcmToken(testAuthMember.id(), testLogoutRequest.fcmToken());
        verify(memberFcmTokenRepository).delete(mockFcmToken);
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면, 로그아웃에 실패한다.")
    void logoutFailureInvalidTokenHeader() {

        // given
        given(jwtProvider.resolveToken(testAuthorizationHeader)).willReturn(null);

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            authService.logout(testAuthorizationHeader, testLogoutRequest, testAuthMember);
        });

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
        verify(memberFcmTokenRepository, never()).findByMemberIdAndFcmToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("FCM 토큰이 존재하지 않으면, 예외를 발생한다.")
    void logout_Failure_FcmTokenNotFound() {

        // given
        given(jwtProvider.resolveToken(testAuthorizationHeader)).willReturn(testToken);
        given(jwtProvider.getExpirationMillis(testToken)).willReturn(testExpirationMillis);

        given(memberFcmTokenRepository.findByMemberIdAndFcmToken(testAuthMember.id(), testLogoutRequest.fcmToken()))
                .willReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            authService.logout(testAuthorizationHeader, testLogoutRequest, testAuthMember);
        });

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_FCM_TOKEN_NOT_FOUND);
        verify(memberFcmTokenRepository, never()).delete(any(MemberFcmToken.class));
    }
}