package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.dto.member.response.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberInternalService 테스트")
class MemberInternalServiceTest {

    private final MemberInternalService memberInternalService = new MemberInternalServiceImpl();

    @Test
    @DisplayName("회원 ID로 회원 정보 조회 성공")
    void getMemberById_Success() {
        // given
        Long memberId = 1L;

        // when
        MemberResponse result = memberInternalService.getMemberById(memberId);

        // then
        assertThat(result.id()).isEqualTo(memberId);
        assertThat(result.username()).isEqualTo("mock_user_" + memberId);
    }

    @Test
    @DisplayName("다른 회원 ID로 회원 정보 조회 성공")
    void getMemberById_WithDifferentId_Success() {
        // given
        Long memberId = 999L;

        // when
        MemberResponse result = memberInternalService.getMemberById(memberId);

        // then
        assertThat(result.id()).isEqualTo(memberId);
        assertThat(result.username()).isEqualTo("mock_user_999");
    }
}

