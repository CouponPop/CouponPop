package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.dto.request.CreateMemberRequest;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("이미 존재하는 이메일로 회원 가입을 시도하면 예외가 발생한다.")
    @Test
    void createMemberFailureEmailDuplicated() {

        // given
        CreateMemberRequest request = CreateMemberRequest.of(
                "test@example.com",
                "테스트이름",
                "test1234!",
                "01012345678",
                MemberType.CUSTOMER);

        given(memberRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            memberService.createMember(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.EMAIL_DUPLICATED);
    }
}