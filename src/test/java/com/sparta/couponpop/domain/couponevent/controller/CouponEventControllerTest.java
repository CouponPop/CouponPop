package com.sparta.couponpop.domain.couponevent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.service.CouponEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponEventService couponEventService;


    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(123L, null, List.of());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Owner 쿠폰 이벤트 생성 - 성공")
    void createCouponEvent_success() throws Exception {
        // given
        LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 17, 0);
        LocalDateTime eventEndAt = LocalDateTime.of(2025, 10, 15, 12, 0);

        CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                .storeId(1L)
                .name("아이스 아메리카노 1+1")
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .totalCount(30)
                .build();

        CreateCouponEventResponse response = CreateCouponEventResponse.builder()
                .eventId(1L)
                .name("아이스 아메리카노 1+1")
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .eventStatus(CouponEventStatus.SCHEDULED)
                .totalCount(30)
                .createdAt(LocalDateTime.of(2025, 10, 14, 15, 0))
                .build();


        given(couponEventService.createCouponEvent(any(CreateCouponEventRequest.class), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/owner/coupons/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("아이스 아메리카노 1+1"))
                .andExpect(jsonPath("$.data.eventStatus").value(CouponEventStatus.SCHEDULED.name()))
        ;
    }

    @Test
    @DisplayName("쿠폰 이벤트 정보 상세 조회 - 성공")
    void getCouponEvent_success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 10, 14, 17, 0);

        LocalDateTime eventStartAt = now.minusDays(1);
        LocalDateTime eventEndAt = now.plusDays(1);
        Long eventId = 1L;

        CouponEventDetailResponse response = CouponEventDetailResponse.builder()
                .id(eventId)
                .name("아이스 아메리카노 1+1")
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .eventStatus(CouponEventStatus.SCHEDULED)
                .totalCount(30)
                .unclaimedCount(30)
                .issuedCount(0)
                .usedCount(0)
                .unusedCount(0)
                .createdAt(LocalDateTime.of(2025, 10, 14, 15, 0))
                .updatedAt(LocalDateTime.of(2025, 10, 14, 15, 0))
                .build();


        given(couponEventService.getCouponEvent(anyLong(), anyLong(), any(LocalDateTime.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/owner/coupons/events/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("아이스 아메리카노 1+1"))
                .andExpect(jsonPath("$.data.eventStatus").value(CouponEventStatus.SCHEDULED.name()))
        ;
    }
}