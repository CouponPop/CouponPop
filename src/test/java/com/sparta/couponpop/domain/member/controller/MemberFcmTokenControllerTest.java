package com.sparta.couponpop.domain.member.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.common.security.JwtAuthFilter;
import com.sparta.couponpop.common.security.JwtAuthenticationToken;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.member.dto.request.MemberFcmTokenRequest;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.service.MemberFcmTokenService;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(MemberFcmTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberFcmTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private MemberFcmTokenService memberFcmTokenService;

    @MockitoBean
    private StoreSearchRepository storeSearchRepository;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.from(123L, "testUser", MemberType.CUSTOMER);
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/v1/members/fcm-token")
    class UpsertMemberFcmToken {

        private static final String URL = "/api/v1/members/fcm-token";

        @Test
        @DisplayName("회원 FCM 토큰 생성 및 갱신 - 성공")
        void upsertMemberFcmToken_success() throws Exception {
            // given
            MemberFcmTokenRequest request = MemberFcmTokenRequest.builder()
                    .fcmToken("sample_fcm_token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();

            willDoNothing().given(memberFcmTokenService).upsertTokenForMember(any(MemberFcmTokenRequest.class), anyLong());

            // when
            ResultActions resultActions = mockMvc.perform(
                    post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // docs
            resultActions.andDo(document("member-fcmTokenUpsert",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                            ResourceSnippetParameters.builder()
                                    .summary("회원 FCM 토큰 생성 및 갱신")
                                    .description("회원의 FCM 토큰을 생성하거나 갱신합니다.")
                                    .tag("Member FCM Token")
                                    .requestSchema(Schema.schema("Member.MemberFcmTokenRequest"))
                                    .requestFields(
                                            fieldWithPath("fcmToken").description("FCM 토큰"),
                                            fieldWithPath("deviceType").description("디바이스 타입 (예: ANDROID, IOS)"),
                                            fieldWithPath("deviceIdentifier").description("디바이스 고유 식별자")
                                    )
                                    .build()
                    )));
        }
    }

}