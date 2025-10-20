package com.sparta.couponpop.domain.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.security.JwtAuthenticationToken;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.coupon.dto.request.CouponIssueRequest;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.service.CouponService;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.member.enums.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.from(123L, "testUser", MemberType.CUSTOMER);
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("쿠폰 발급 요청")
    class IssueCoupon {
        @Test
        @DisplayName("쿠폰 발급 정상 요청 - 204 반환")
        void issueCoupon_success() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(1L, 2L);

            // when
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // then
            verify(couponService, times(1))
                    .issueEventCoupon(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 이벤트 없는 경우 예외")
        void issueCoupon_EventNotFound() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(1L, 999L);

            willThrow(new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND))
                    .given(couponService)
                    .issueEventCoupon(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value(CouponEventErrorCode.EVENT_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 중복 발급 예외")
        void issueCoupon_AlreadyIssued() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(1L, 10L);

            willThrow(new GlobalException(CouponErrorCode.COUPON_ALREADY_ISSUED))
                    .given(couponService)
                    .issueEventCoupon(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_ALREADY_ISSUED.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 이벤트 기간 외 예외")
        void issueCoupon_EventNotInProgressTime() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(1L, 10L);

            willThrow(new GlobalException(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS_TIME))
                    .given(couponService)
                    .issueEventCoupon(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS_TIME.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 이벤트 쿠폰 모두 소진")
        void issueCoupon_CouponSoldOut() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(999L, 10L);

            willThrow(new GlobalException(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT))
                    .given(couponService)
                    .issueEventCoupon(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - storeId 누락")
        void issueCoupon_fail_missingStoreId() throws Exception {
            String requestBody = """
                    {
                        "eventId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("쿠폰 수령을 위한 매장 ID 는 필수입니다."));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - eventId 누락")
        void issueCoupon_fail_missingEventId() throws Exception {
            String requestBody = """
                    {
                        "storeId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("쿠폰 수령을 위한 이벤트 ID 는 필수입니다."));
        }
    }

}