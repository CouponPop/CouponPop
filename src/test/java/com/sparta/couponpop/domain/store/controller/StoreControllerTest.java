package com.sparta.couponpop.domain.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.common.security.JwtAuthFilter;
import com.sparta.couponpop.common.security.JwtAuthenticationToken;
import com.sparta.couponpop.common.security.JwtProvider;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreDetailResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreMapResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import com.sparta.couponpop.domain.store.service.StoreIndexInitService;
import com.sparta.couponpop.domain.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("StoreController 테스트")
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private StoreIndexInitService storeIndexInitService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private StoreSearchRepository storeSearchRepository;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.from(1L, "testUser", MemberType.OWNER);
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("소유자의 매장 목록 조회 성공")
    void getStores_Success() throws Exception {
        // given
        Long memberId = 1L;
        List<StoreResponse> storeResponses = Arrays.asList(
                new StoreResponse(1L, memberId, "testuser", "스타벅스 홍대점", "02123456789",
                        "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                        37.5665, 126.9780, "https://example.com/image.jpg",
                        StoreCategory.CAFE, LocalTime.of(9, 0), LocalTime.of(22, 0),
                        LocalTime.of(10, 0), LocalTime.of(23, 0),
                        LocalDateTime.now(), LocalDateTime.now()),
                new StoreResponse(2L, memberId, "testuser", "카페베네", "02987654321",
                        "홍대 카페베네", "0987654321", "서울시 마포구", "홍대동",
                        37.5665, 126.9780, "https://example.com/image2.jpg",
                        StoreCategory.CAFE, LocalTime.of(8, 0), LocalTime.of(21, 0),
                        LocalTime.of(9, 0), LocalTime.of(22, 0),
                        LocalDateTime.now(), LocalDateTime.now())
        );

        given(storeService.getStoresByOwner(anyLong())).willReturn(storeResponses);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/owner/stores")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("스타벅스 홍대점"))
                .andExpect(jsonPath("$.data[1].name").value("카페베네"))
                .andDo(print());
    }

    @Test
    @DisplayName("매장 생성 성공")
    void createStore_Success() throws Exception {
        // given
        CreateStoreRequest request = new CreateStoreRequest(
                "스타벅스 홍대점",
                "02123456789",
                "홍대 중심가에 위치한 스타벅스입니다.",
                "1234567890",
                "서울시 마포구 홍익로 123",
                "홍대동",
                37.5665,
                126.9780,
                "https://example.com/store-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0)
        );

        StoreResponse response = new StoreResponse(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 중심가에 위치한 스타벅스입니다.", "1234567890",
                "서울시 마포구 홍익로 123", "홍대동",
                37.5665, 126.9780, "https://example.com/store-image.jpg",
                StoreCategory.CAFE, LocalTime.of(7, 0), LocalTime.of(22, 0),
                LocalTime.of(8, 0), LocalTime.of(23, 0),
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(storeService.createStore(anyLong(), any(CreateStoreRequest.class))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/owner/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("스타벅스 홍대점"))
                .andExpect(jsonPath("$.data.storeCategory").value("CAFE"))
                .andDo(print());
    }

    @Test
    @DisplayName("매장 생성 시 매장명이 없으면 실패")
    void createStore_WithoutName_Fail() throws Exception {
        // given
        CreateStoreRequest request = new CreateStoreRequest(
                "",
                "02123456789",
                "홍대 중심가에 위치한 스타벅스입니다.",
                "1234567890",
                "서울시 마포구 홍익로 123",
                "홍대동",
                37.5665,
                126.9780,
                "https://example.com/store-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0)
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/owner/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("매장명은 필수입니다"))
                .andDo(print());
    }

    @Test
    @DisplayName("매장 생성 시 전화번호 형식이 잘못되면 실패")
    void createStore_WithInvalidPhoneFormat_Fail() throws Exception {
        // given
        CreateStoreRequest request = new CreateStoreRequest(
                "스타벅스 홍대점",
                "02-1234-5678", // 잘못된 형식
                "홍대 중심가에 위치한 스타벅스입니다.",
                "1234567890",
                "서울시 마포구 홍익로 123",
                "홍대동",
                37.5665,
                126.9780,
                "https://example.com/store-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0)
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/owner/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("전화번호는 11자리 숫자여야 합니다"))
                .andDo(print());
    }

    @Test
    @DisplayName("매장 수정 성공")
    void updateStore_Success() throws Exception {
        // given
        Long storeId = 1L;
        CreateStoreRequest request = new CreateStoreRequest(
                "스타벅스 홍대점 (수정)",
                "02123456789",
                "수정된 설명입니다.",
                "1234567890",
                "서울시 마포구 홍익로 124",
                "홍대동",
                37.5666,
                126.9781,
                "https://example.com/store-image-updated.jpg",
                StoreCategory.CAFE,
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                LocalTime.of(9, 0),
                LocalTime.of(23, 30)
        );

        StoreResponse response = new StoreResponse(
                storeId, 1L, "testuser", "스타벅스 홍대점 (수정)", "02123456789",
                "수정된 설명입니다.", "1234567890",
                "서울시 마포구 홍익로 124", "홍대동",
                37.5666, 126.9781, "https://example.com/store-image-updated.jpg",
                StoreCategory.CAFE, LocalTime.of(8, 0), LocalTime.of(23, 0),
                LocalTime.of(9, 0), LocalTime.of(23, 30),
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(storeService.updateStore(anyLong(), anyLong(), any(CreateStoreRequest.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/owner/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(storeId))
                .andExpect(jsonPath("$.data.name").value("스타벅스 홍대점 (수정)"))
                .andExpect(jsonPath("$.data.description").value("수정된 설명입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("매장 삭제 성공")
    void deleteStore_Success() throws Exception {
        // given
        Long storeId = 1L;
        doNothing().when(storeService).deleteStore(anyLong(), anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/owner/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(storeService).deleteStore(eq(storeId), anyLong());
    }

    @Test
    @DisplayName("소유자의 매장 상세 조회 성공")
    void getStoreDetail_Success() throws Exception {
        // given
        Long storeId = 1L;
        StoreDetailResponse response = new StoreDetailResponse(
                "https://example.com/image.jpg",
                "스타벅스 홍대점",
                "홍대 스타벅스입니다.",
                StoreCategory.CAFE,
                "서울시 마포구",
                "홍대동",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                LocalTime.of(10, 0),
                LocalTime.of(23, 0)
        );

        given(storeService.getStoreDetail(anyLong(), anyLong())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/owner/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("스타벅스 홍대점"))
                .andExpect(jsonPath("$.data.storeCategory").value("CAFE"))
                .andDo(print());
    }

    @Test
    @DisplayName("위치 기반 매장 조회 성공")
    void getStoresByLocation_Success() throws Exception {
        // given
        double lat = 37.5665;
        double lng = 126.9780;
        double radius = 5.0;

        List<StoreMapResponse> responses = Arrays.asList(
                new StoreMapResponse(1L, "스타벅스 홍대점", "서울시 마포구", "홍대동",
                        StoreCategory.CAFE, 37.5665, 126.9780,
                        "https://example.com/image.jpg", 0.5),
                new StoreMapResponse(2L, "카페베네", "서울시 마포구", "홍대동",
                        StoreCategory.CAFE, 37.5666, 126.9781,
                        "https://example.com/image2.jpg", 1.2)
        );

        given(storeService.getStoresByLocation(anyDouble(), anyDouble(), anyDouble()))
                .willReturn(responses);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/stores")
                        .param("lat", String.valueOf(lat))
                        .param("lng", String.valueOf(lng))
                        .param("radius", String.valueOf(radius))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].distance").value(0.5))
                .andExpect(jsonPath("$.data[1].distance").value(1.2))
                .andDo(print());
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 기본 반경값 사용")
    void getStoresByLocation_WithDefaultRadius_Success() throws Exception {
        // given
        double lat = 37.5665;
        double lng = 126.9780;
        List<StoreMapResponse> responses = Arrays.asList(
                new StoreMapResponse(1L, "스타벅스 홍대점", "서울시 마포구", "홍대동",
                        StoreCategory.CAFE, 37.5665, 126.9780,
                        "https://example.com/image.jpg", 0.5)
        );

        given(storeService.getStoresByLocation(anyDouble(), anyDouble(), anyDouble()))
                .willReturn(responses);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/stores")
                        .param("lat", String.valueOf(lat))
                        .param("lng", String.valueOf(lng))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("손님용 매장 상세 조회 성공")
    void getStoreDetailForCustomer_Success() throws Exception {
        // given
        Long storeId = 1L;
        StoreDetailResponse response = new StoreDetailResponse(
                "https://example.com/image.jpg",
                "스타벅스 홍대점",
                "홍대 스타벅스입니다.",
                StoreCategory.CAFE,
                "서울시 마포구",
                "홍대동",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                LocalTime.of(10, 0),
                LocalTime.of(23, 0)
        );

        given(storeService.getStoreDetailForCustomer(anyLong())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("스타벅스 홍대점"))
                .andExpect(jsonPath("$.data.storeCategory").value("CAFE"))
                .andDo(print());
    }

    @Test
    @DisplayName("매장명 검색 성공")
    void searchStores_Success() throws Exception {
        // given
        String keyword = "스타벅스";
        List<StoreResponse> responses = Arrays.asList(
                new StoreResponse(1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                        "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                        37.5665, 126.9780, "https://example.com/image.jpg",
                        StoreCategory.CAFE, LocalTime.of(9, 0), LocalTime.of(22, 0),
                        LocalTime.of(10, 0), LocalTime.of(23, 0),
                        LocalDateTime.now(), LocalDateTime.now()),
                new StoreResponse(2L, 2L, "testuser2", "스타벅스 강남점", "02987654321",
                        "강남 스타벅스", "0987654321", "서울시 강남구", "역삼동",
                        37.5000, 127.0000, "https://example.com/image2.jpg",
                        StoreCategory.CAFE, LocalTime.of(9, 0), LocalTime.of(22, 0),
                        LocalTime.of(10, 0), LocalTime.of(23, 0),
                        LocalDateTime.now(), LocalDateTime.now())
        );

        given(storeService.searchStoresByName(anyString())).willReturn(responses);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/stores/search")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("스타벅스 홍대점"))
                .andExpect(jsonPath("$.data[1].name").value("스타벅스 강남점"))
                .andDo(print());
    }

    @Test
    @DisplayName("매장 재색인 성공")
    void reindexStores_Success() throws Exception {
        // given
        doNothing().when(storeIndexInitService).fullReindex();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/admin/stores/reindex")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Store reindexing completed successfully"))
                .andDo(print());

        verify(storeIndexInitService).fullReindex();
    }
}

