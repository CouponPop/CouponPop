package com.sparta.couponpop.domain.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.common.security.JwtAuthFilter;
import com.sparta.couponpop.common.security.JwtAuthenticationToken;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.location.dto.request.CreateLocationRequest;
import com.sparta.couponpop.domain.location.service.LocationService;
import com.sparta.couponpop.domain.member.enums.MemberType;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private LocationService locationService;

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.from(123L, "테스트사용자", MemberType.CUSTOMER);
        Authentication authentication = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/v1/locations/current")
    class CreateCurrentLocation {

        private static final String URL = "/api/v1/locations/current";

        @Test
        @DisplayName("회원이 유효한 위치 정보를 전송하면 현재 위치를 캐시하고 204를 반환한다.")
        void createLocation_success_validRequest() throws Exception {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "sample-fcm-token",
                    "device-123",
                    37.5665,
                    126.9780
            );

            willDoNothing().given(locationService).cacheLocation(any(CreateLocationRequest.class), anyLong());

            // when
            mockMvc.perform(
                            post(URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // then
            ArgumentCaptor<CreateLocationRequest> requestCaptor = ArgumentCaptor.forClass(CreateLocationRequest.class);
            ArgumentCaptor<Long> memberIdCaptor = ArgumentCaptor.forClass(Long.class);
            verify(locationService).cacheLocation(requestCaptor.capture(), memberIdCaptor.capture());

            CreateLocationRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.fcmToken()).isEqualTo("sample-fcm-token");
            assertThat(capturedRequest.deviceIdentifier()).isEqualTo("device-123");
            assertThat(capturedRequest.latitude()).isEqualTo(37.5665);
            assertThat(capturedRequest.longitude()).isEqualTo(126.9780);
            assertThat(memberIdCaptor.getValue()).isEqualTo(123L);
        }

        @Test
        @DisplayName("위도 값이 허용 범위를 벗어나면 현재 위치 저장 요청이 400을 반환한다.")
        void createLocation_fail_invalidLatitude() throws Exception {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "sample-fcm-token",
                    "device-123",
                    95.0,
                    126.9780
            );

            // when & then
            mockMvc.perform(
                            post(URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("위도는 90 이하이어야 합니다."));

            verify(locationService, never()).cacheLocation(any(CreateLocationRequest.class), anyLong());
        }
    }
}
