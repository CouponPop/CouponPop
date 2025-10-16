package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("매장 등록 성공")
    void createStore_Success() {

        // given
        Long memberId = 1L;
        CreateStoreRequest request = createStoreRequest();
        Store savedStore = createStore(memberId);

        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, request);

        // then
        assertThat(result.id()).isEqualTo(savedStore.getId());
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.phone()).isEqualTo(request.phone());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.businessNumber()).isEqualTo(request.businessNumber());
        assertThat(result.address()).isEqualTo(request.address());
        assertThat(result.latitude()).isEqualTo(request.latitude());
        assertThat(result.longitude()).isEqualTo(request.longitude());
        assertThat(result.imageUrl()).isEqualTo(request.imageUrl());
        assertThat(result.storeCategory()).isEqualTo(request.storeCategory());
        assertThat(result.weekdayOpenTime()).isEqualTo(request.weekdayOpenTime());
        assertThat(result.weekdayCloseTime()).isEqualTo(request.weekdayCloseTime());
        assertThat(result.weekendOpenTime()).isEqualTo(request.weekendOpenTime());
        assertThat(result.weekendCloseTime()).isEqualTo(request.weekendCloseTime());

        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 등록 시 정확한 데이터로 Store 엔티티 생성")
    void createStore_CreatesStoreWithCorrectData() {

        // given
        Long memberId = 2L;
        CreateStoreRequest request = createStoreRequest();
        Store expectedStore = createStore(memberId);

        given(storeRepository.save(any(Store.class)))
                .willReturn(expectedStore);

        // when
        storeService.createStore(memberId, request);

        // then
        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("다른 카테고리 매장 등록 성공")
    void createStore_WithFoodCategory_Success() {

        // given
        Long memberId = 3L;
        CreateStoreRequest request = createFoodStoreRequest();
        Store savedStore = createFoodStore(memberId);

        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, request);

        // then
        assertThat(result.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(result.name()).isEqualTo("맛있는 식당");
        assertThat(result.memberId()).isEqualTo(memberId);

        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 등록 시 null 값 처리")
    void createStore_WithNullDescription_Success() {

        // given
        Long memberId = 4L;
        CreateStoreRequest request = new CreateStoreRequest(
                "테스트 매장",
                "0212345678",
                null, // description이 null
                "1234567890",
                "서울시 테스트구 테스트로 123",
                37.5665,
                126.9780,
                "https://example.com/test-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        Store savedStore = Store.createStore(
                memberId,
                "테스트 매장",
                "0212345678",
                null,
                "1234567890",
                "서울시 테스트구 테스트로 123",
                37.5665,
                126.9780,
                "https://example.com/test-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, request);

        // then
        assertThat(result.description()).isNull();
        assertThat(result.name()).isEqualTo("테스트 매장");

        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    private CreateStoreRequest createStoreRequest() {
        return new CreateStoreRequest(
                "스타벅스 홍대점",
                "0212345678",
                "홍대 중심가에 위치한 스타벅스입니다.",
                "1234567890",
                "서울시 마포구 홍익로 123",
                37.5665,
                126.9780,
                "https://example.com/store-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0)
        );
    }

    private CreateStoreRequest createFoodStoreRequest() {
        return new CreateStoreRequest(
                "맛있는 식당",
                "0312345678",
                "정말 맛있는 음식을 제공하는 식당입니다.",
                "9876543210",
                "서울시 강남구 테헤란로 456",
                37.5665,
                126.9780,
                "https://example.com/food-store-image.jpg",
                StoreCategory.FOOD,
                LocalTime.of(11, 0),
                LocalTime.of(22, 0),
                LocalTime.of(12, 0),
                LocalTime.of(23, 0)
        );
    }

    private Store createStore(Long memberId) {
        return Store.createStore(
                memberId,
                "스타벅스 홍대점",
                "0212345678",
                "홍대 중심가에 위치한 스타벅스입니다.",
                "1234567890",
                "서울시 마포구 홍익로 123",
                37.5665,
                126.9780,
                "https://example.com/store-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0)
        );
    }

    private Store createFoodStore(Long memberId) {
        return Store.createStore(
                memberId,
                "맛있는 식당",
                "0312345678",
                "정말 맛있는 음식을 제공하는 식당입니다.",
                "9876543210",
                "서울시 강남구 테헤란로 456",
                37.5665,
                126.9780,
                "https://example.com/food-store-image.jpg",
                StoreCategory.FOOD,
                LocalTime.of(11, 0),
                LocalTime.of(22, 0),
                LocalTime.of(12, 0),
                LocalTime.of(23, 0)
        );
    }
}
