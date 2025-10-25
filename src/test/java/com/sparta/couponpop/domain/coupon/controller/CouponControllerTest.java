package com.sparta.couponpop.domain.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.security.JwtAuthFilter;
import com.sparta.couponpop.common.security.JwtAuthenticationToken;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.coupon.dto.request.CouponIssueRequest;
import com.sparta.couponpop.domain.coupon.dto.request.MemberIssuedCouponCursor;
import com.sparta.couponpop.domain.coupon.dto.request.UseCouponRequest;
import com.sparta.couponpop.domain.coupon.dto.response.CouponDetailResponse;
import com.sparta.couponpop.domain.coupon.dto.response.IssuedCouponListResponse;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.dto.CouponSummaryInfoProjection;
import com.sparta.couponpop.domain.coupon.service.CouponService;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private JwtAuthFilter jwtAuthFilter;

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
            CouponIssueRequest request = new CouponIssueRequest(2L);

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
            CouponIssueRequest request = new CouponIssueRequest(999L);

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
            CouponIssueRequest request = new CouponIssueRequest( 10L);

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
            CouponIssueRequest request = new CouponIssueRequest( 10L);

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
            CouponIssueRequest request = new CouponIssueRequest( 10L);

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

    @Nested
    @DisplayName("쿠폰 정보 상세 조회 요청")
    class GetCouponDetailTests {

        public static CouponDetailResponse createSample(Long storeId, Long eventId, Long couponId) {
            // QR 코드
            CouponDetailResponse.QrCode qrCode = new CouponDetailResponse.QrCode(
                    "https://couponpop.com/q/ABC123XYZ",
                    "사장님께 QR코드를 보여주세요"
            );

            // 이벤트 기간
            CouponDetailResponse.EventInfo.EventPeriod period = new CouponDetailResponse.EventInfo.EventPeriod(
                    LocalDateTime.of(2025, 10, 21, 0, 0),
                    LocalDateTime.of(2025, 10, 31, 23, 59)
            );

            //  이벤트 정보
            CouponDetailResponse.EventInfo eventInfo = new CouponDetailResponse.EventInfo(
                    eventId,
                    "아메리카노 1+1 이벤트",
                    period
            );

            // 매장 정보
            CouponDetailResponse.StoreInfo storeInfo = new CouponDetailResponse.StoreInfo(
                    storeId,
                    "스타벅스 강남점",
                    "서울 강남구 역삼동 123-45",
                    "02-1234-5678"
            );

            // CouponDetailResponse 생성
            return new CouponDetailResponse(
                    couponId,
                    CouponStatus.AVAILABLE,
                    LocalDateTime.of(2025, 10, 21, 12, 0),
                    LocalDateTime.of(2025, 11, 21, 23, 59),
                    null,
                    qrCode,
                    eventInfo,
                    storeInfo
            );
        }

        @Test
        @DisplayName("쿠폰 정보 상세 조회 요청 - 200 반환")
        void getCouponDetail_Success() throws Exception {
            // given
            Long couponId = 1L;
            CouponDetailResponse response = createSample(100L, 1L, couponId);

            given(couponService.getCouponDetail(anyLong(), anyLong())).willReturn(response);

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons/{couponId}", couponId)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                    .andExpect(jsonPath("$.data.event.id").value(1L))
                    .andExpect(jsonPath("$.data.store.id").value(100L));
        }

        @Test
        @DisplayName("쿠폰 정보 상세 조회 실패 - 쿠폰 없음")
        void getCouponDetail_NotFound() throws Exception {
            // given
            willThrow(new GlobalException(CouponErrorCode.COUPON_NOT_FOUND))
                    .given(couponService)
                    .getCouponDetail(anyLong(), anyLong());

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons/{couponId}", 999L)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 정보 상세 조회 실패 - 쿠폰 접근 권한 없음")
        void getCouponDetail_AccessDenied() throws Exception {
            // given
            willThrow(new GlobalException(CouponErrorCode.COUPON_ACCESS_DENIED))
                    .given(couponService)
                    .getCouponDetail(anyLong(), anyLong());

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons/{couponId}", 999L)
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_ACCESS_DENIED.getMessage()));
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 요청")
    class UseCouponTests {

        @Test
        @DisplayName("쿠폰 사용 성공 - 204 No Content")
        void useCoupon_Success() throws Exception {
            // given
            UseCouponRequest request = new UseCouponRequest("TEMP123", 1L);


            // when
            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // then
            verify(couponService, times(1))
                    .useCoupon(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("쿠폰 사용 요청 실패 - 임시 코드 유효하지 않음")
        void useCoupon_InvalidTempCode() throws Exception {
            // given
            UseCouponRequest request = new UseCouponRequest("INVALID", 1L);

            Mockito.doThrow(new GlobalException(CouponErrorCode.COUPON_INVALID_TEMP_CODE))
                    .when(couponService).useCoupon(Mockito.anyLong(), Mockito.anyString(), Mockito.anyLong(), Mockito.any(LocalDateTime.class));

            willThrow(new GlobalException(CouponErrorCode.COUPON_INVALID_TEMP_CODE))
                    .given(couponService)
                    .useCoupon(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_INVALID_TEMP_CODE.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 사용 요청 실패 - qrCode 누락")
        void useCoupon_fail_missingQrCode() throws Exception {
            String requestBody = """
                    {
                        "couponId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("QR 코드는 필수입니다."));
        }

        @Test
        @DisplayName("쿠폰 사용 요청 실패 - couponId 누락")
        void useCoupon_fail_missingCouponId() throws Exception {
            String requestBody = """
                    {
                        "qrCode": "foejsnvp"
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("사용하려는 쿠폰 ID는 필수입니다."));
        }
    }

    @Nested
    @DisplayName("쿠폰 리스트 조회")
    class GetIssuedCoupons {

        private static final LocalDateTime now = LocalDateTime.now();

        @Test
        @DisplayName("첫 페이지 조회 - hasNext true")
        void getIssuedCoupons_firstPage_hasNextTrue() throws Exception {
            // given
            CouponSummaryInfoProjection.EventInfo event = new CouponSummaryInfoProjection.EventInfo(1L, "이벤트",
                    new CouponSummaryInfoProjection.EventInfo.EventPeriod(
                            now.minusDays(2),
                            now.plusDays(1)
                    ));
            List<CouponSummaryInfoProjection> mockCoupons = List.of(
                    new CouponSummaryInfoProjection(1L, CouponStatus.AVAILABLE, now.minusDays(1), now.plusDays(1), null, event, null),
                    new CouponSummaryInfoProjection(2L, CouponStatus.AVAILABLE, now.minusDays(1), now.plusDays(1), null, event, null),
                    new CouponSummaryInfoProjection(3L, CouponStatus.AVAILABLE, now.minusDays(1), now.plusDays(1), null, event, null)
            );
//
            IssuedCouponListResponse mockResponse = IssuedCouponListResponse.of(mockCoupons, 2);

            given(couponService.getIssuedCoupons(anyLong(), eq(CouponStatus.AVAILABLE), any(MemberIssuedCouponCursor.class), eq(2)))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons")
                                    .param("type", "AVAILABLE")
                                    .param("size", "2")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.coupons.length()").value(2))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.nextCursor").isNotEmpty());
        }

        @Test
        @DisplayName("다음 페이지 조회 - 마지막 페이지 (hasNext false)")
        void getIssuedCoupons_nextPage_lastPage() throws Exception {
            // given
            List<CouponSummaryInfoProjection> mockCoupons = List.of(
                    new CouponSummaryInfoProjection(3L, CouponStatus.AVAILABLE, now.minusDays(1), now.plusDays(1), null, null, null)
            );

            IssuedCouponListResponse mockResponse = IssuedCouponListResponse.of(mockCoupons, 2);

            given(couponService.getIssuedCoupons(anyLong(), eq(CouponStatus.AVAILABLE), any(MemberIssuedCouponCursor.class), eq(2)))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons")
                                    .param("type", "AVAILABLE")
                                    .param("lastEventEndAt", now.minusDays(1).toString())
                                    .param("lastCouponId", "2")
                                    .param("size", "2")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.coupons.length()").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.nextCursor").isEmpty());
        }
    }
}