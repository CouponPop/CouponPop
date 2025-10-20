package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.dto.response.MemberProfileResponse;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원의 고유식별자를 통해 회원 프로필을 조회한다.")
    void getMemberProfileSuccess() {

        // given
        Long mockMemberId = 1L;
        Member mockMember = TestUtils.createEntity(Member.class, Map.of(
                "id", mockMemberId,
                "username", "테스트이름",
                "email", "test@example.com",
                "phoneNumber", "01012345678",
                "memberType", MemberType.CUSTOMER));

        given(memberRepository.findById(mockMemberId)).willReturn(Optional.of(mockMember));

        // when
        MemberProfileResponse response = memberService.getMemberProfile(mockMemberId);

        // then
        assertThat(response.userId()).isEqualTo(mockMemberId);
        assertThat(response.username()).isEqualTo("테스트이름");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.phoneNumber()).isEqualTo("01012345678");
        assertThat(response.memberType()).isEqualTo(MemberType.CUSTOMER);
    }

    @Test
    @DisplayName("회원의 고유식별자로 회원 프로필 조회 시, 회원을 찾을 수 없으면 예외가 발생한다.")
    void getMemberProfileFailureMemberNotFound() {

        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberProfile(999L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}