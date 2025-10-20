package com.sparta.couponpop.domain.member.controller;

import com.sparta.couponpop.common.security.JwtAuthenticationToken;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.member.dto.response.MemberProfileResponse;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("인증된 사용자가 자신의 프로필을 조회한다.")
    void getMyProfileSuccess() throws Exception {

        // given
        Long mockMemberId = 1L;
        MemberProfileResponse mockResponse = new MemberProfileResponse(
                mockMemberId,
                "테스트이름",
                "test@example.com",
                "01012345678",
                MemberType.CUSTOMER
        );

        given(memberService.getMemberProfile(mockMemberId)).willReturn(mockResponse);
        AuthMember authMember = AuthMember.from(1L, "테스트이름", MemberType.CUSTOMER);
        Authentication authentication = new JwtAuthenticationToken(authMember);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/members/me")
                .with(authentication(authentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("테스트이름"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.memberType").value("CUSTOMER"))
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 조회를 하려면 인증이 필요하다.")
    void getMyProfileUnauthorized() throws Exception {

        // given
        SecurityContextHolder.clearContext();

        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}