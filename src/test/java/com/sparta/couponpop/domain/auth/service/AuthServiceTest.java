package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.auth.dto.request.SignUpRequest;
import com.sparta.couponpop.domain.auth.dto.response.SignUpResponse;
import com.sparta.couponpop.domain.auth.exception.AuthErrorCode;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AuthService authService;

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
}