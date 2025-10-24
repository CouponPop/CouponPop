package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreMapResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreLocationProjection;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("매장 등록 성공")
    void createStore_Success() {

        // given
        Long memberId = 1L;
        CreateStoreRequest request = createStoreRequest();
        Member member = createMember(memberId);
        Store savedStore = createStore(member);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));
        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, request);

        // then
        assertThat(result.id()).isEqualTo(savedStore.getId());
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.memberUsername()).isEqualTo(member.getUsername());
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.phone()).isEqualTo(request.phone());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.businessNumber()).isEqualTo(request.businessNumber());
        assertThat(result.address()).isEqualTo(request.address());
        assertThat(result.dong()).isEqualTo(request.dong());
        assertThat(result.latitude()).isEqualTo(request.latitude());
        assertThat(result.longitude()).isEqualTo(request.longitude());
        assertThat(result.imageUrl()).isEqualTo(request.imageUrl());
        assertThat(result.storeCategory()).isEqualTo(request.storeCategory());
        assertThat(result.weekdayOpenTime()).isEqualTo(request.weekdayOpenTime());
        assertThat(result.weekdayCloseTime()).isEqualTo(request.weekdayCloseTime());
        assertThat(result.weekendOpenTime()).isEqualTo(request.weekendOpenTime());
        assertThat(result.weekendCloseTime()).isEqualTo(request.weekendCloseTime());

        then(memberRepository).should(times(1)).findById(memberId);
        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 등록 시 정확한 데이터로 Store 엔티티 생성")
    void createStore_CreatesStoreWithCorrectData() {

        // given
        Long memberId = 2L;
        CreateStoreRequest request = createStoreRequest();
        Member member = createMember(memberId);
        Store expectedStore = createStore(member);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));
        given(storeRepository.save(any(Store.class)))
                .willReturn(expectedStore);

        // when
        storeService.createStore(memberId, request);

        // then
        then(memberRepository).should(times(1)).findById(memberId);
        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("다른 카테고리 매장 등록 성공")
    void createStore_WithFoodCategory_Success() {

        // given
        Long memberId = 3L;
        CreateStoreRequest request = createFoodStoreRequest();
        Member member = createMember(memberId);
        Store savedStore = createFoodStore(member);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));
        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, request);

        // then
        assertThat(result.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(result.name()).isEqualTo("맛있는 식당");
        assertThat(result.memberId()).isEqualTo(memberId);

        then(memberRepository).should(times(1)).findById(memberId);
        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 등록 시 빈 문자열 description 처리")
    void createStore_WithEmptyDescription_Success() {

        // given
        Long memberId = 4L;
        CreateStoreRequest request = new CreateStoreRequest(
                "테스트 매장",
                "0212345678",
                "", // description이 빈 문자열
                "1234567890",
                "서울시 테스트구 테스트로 123",
                "테스트동",
                37.5665,
                126.9780,
                "https://example.com/test-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        Member member = createMember(memberId);
        Store savedStore = Store.createStore(
                member,
                "테스트 매장",
                "0212345678",
                "",
                "1234567890",
                "서울시 테스트구 테스트로 123",
                "테스트동",
                37.5665,
                126.9780,
                "https://example.com/test-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));
        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, request);

        // then
        assertThat(result.description()).isEmpty();
        assertThat(result.name()).isEqualTo("테스트 매장");

        then(memberRepository).should(times(1)).findById(memberId);
        then(storeRepository).should(times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 수정 성공")
    void updateStore_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        CreateStoreRequest request = createUpdateRequest();
        Member member = createMember(memberId);
        Store existingStore = createStore(member);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(existingStore));
        // when
        StoreResponse result = storeService.updateStore(storeId, memberId, request);

        // then
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.phone()).isEqualTo(request.phone());
        assertThat(result.storeCategory()).isEqualTo(request.storeCategory());

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
    }

    @Test
    @DisplayName("존재하지 않는 매장 수정 시 예외 발생")
    void updateStore_WithNonExistentStore_ThrowsException() {

        // given
        Long storeId = 999L;
        Long memberId = 1L;
        CreateStoreRequest request = createUpdateRequest();

        given(storeRepository.findById(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.updateStore(storeId, memberId, request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
    }

    @Test
    @DisplayName("다른 카테고리로 매장 수정 성공")
    void updateStore_WithDifferentCategory_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        CreateStoreRequest request = createFoodUpdateRequest();
        Member member = createMember(memberId);
        Store existingStore = createStore(member);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(existingStore));
        // when
        StoreResponse result = storeService.updateStore(storeId, memberId, request);

        // then
        assertThat(result.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(result.name()).isEqualTo("맛있는 식당");

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
    }

    @Test
    @DisplayName("다른 회원의 매장 수정 시 권한 없음 예외 발생")
    void updateStore_WithDifferentMember_ThrowsPermissionException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Long otherMemberId = 2L;
        CreateStoreRequest request = createUpdateRequest();
        Member otherMember = createMember(otherMemberId);
        Store store = createStore(otherMember);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.updateStore(storeId, memberId, request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장 수정 권한이 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 삭제 성공")
    void deleteStore_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Member member = createMember(memberId);
        Store existingStore = createStore(member);

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.of(existingStore));

        // when
        storeService.deleteStore(storeId, memberId);

        // then
        assertThat(existingStore.getDeletedAt()).isNotNull();
        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("존재하지 않는 매장 삭제 시 예외 발생")
    void deleteStore_WithNonExistentStore_ThrowsException() {

        // given
        Long storeId = 999L;
        Long memberId = 1L;

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("다른 회원의 매장 삭제 시 권한 없음 예외 발생")
    void deleteStore_WithDifferentMember_ThrowsPermissionException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Long otherMemberId = 2L;
        Member otherMember = createMember(otherMemberId);
        Store store = createStore(otherMember);

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장 삭제 권한이 없습니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("이미 삭제된 매장 삭제 시 예외 발생")
    void deleteStore_WithAlreadyDeletedStore_ThrowsException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Member member = createMember(memberId);
        Store deletedStore = createStore(member);
        deletedStore.deleteStore(); // 이미 삭제된 상태

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.of(deletedStore));

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("이미 삭제된 매장입니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 성공")
    void getStoresByLocation_Success() {

        // given
        double latitude = 37.5665; // 서울시청 위도
        double longitude = 126.9780; // 서울시청 경도
        double radius = 5.0; // 5km 반경

        Member member1 = createMember(1L);
        Member member2 = createMember(2L);
        
        Store store1 = createNearStore(member1); // 가까운 매장
        Store store2 = createFarStore(member2); // 먼 매장

        // Mock 데이터: StoreLocationProjection 인터페이스 프로젝션
        StoreLocationProjection result1 = projection(
                store1.getId(),
                store1.getName(),
                store1.getAddress(),
                store1.getDong(),
                store1.getStoreCategory(),
                store1.getLatitude(),
                store1.getLongitude(),
                store1.getImageUrl(),
                0.5
        );
        StoreLocationProjection result2 = projection(
                store2.getId(),
                store2.getName(),
                store2.getAddress(),
                store2.getDong(),
                store2.getStoreCategory(),
                store2.getLatitude(),
                store2.getLongitude(),
                store2.getImageUrl(),
                3.2
        );
        List<StoreLocationProjection> mockResults = Arrays.asList(result1, result2);

        given(storeRepository.findByLocation(latitude, longitude, radius))
                .willReturn(mockResults);

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(2);
        
        // 첫 번째 매장 (가까운 매장)
        StoreMapResponse firstStore = result.get(0);
        assertThat(firstStore.id()).isEqualTo(store1.getId());
        assertThat(firstStore.name()).isEqualTo(store1.getName());
        assertThat(firstStore.storeCategory()).isEqualTo(store1.getStoreCategory());
        assertThat(firstStore.distance()).isEqualTo(0.5);
        
        // 두 번째 매장 (먼 매장)
        StoreMapResponse secondStore = result.get(1);
        assertThat(secondStore.id()).isEqualTo(store2.getId());
        assertThat(secondStore.name()).isEqualTo(store2.getName());
        assertThat(secondStore.storeCategory()).isEqualTo(store2.getStoreCategory());
        assertThat(secondStore.distance()).isEqualTo(3.2);

        then(storeRepository).should(times(1)).findByLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 반경 내 매장이 없는 경우")
    void getStoresByLocation_NoStoresInRadius_ReturnsEmptyList() {

        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 1.0; // 1km 반경 (매우 좁은 반경)

        given(storeRepository.findByLocation(latitude, longitude, radius))
                .willReturn(Arrays.asList());

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).isEmpty();

        then(storeRepository).should(times(1)).findByLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 다양한 카테고리 매장")
    void getStoresByLocation_DifferentCategories_Success() {

        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        Member member1 = createMember(1L);
        Member member2 = createMember(2L);
        
        Store cafeStore = createCafeStore(member1);
        Store foodStore = createFoodStore(member2);

        StoreLocationProjection result1 = projection(
                cafeStore.getId(),
                cafeStore.getName(),
                cafeStore.getAddress(),
                cafeStore.getDong(),
                cafeStore.getStoreCategory(),
                cafeStore.getLatitude(),
                cafeStore.getLongitude(),
                cafeStore.getImageUrl(),
                1.2
        );
        StoreLocationProjection result2 = projection(
                foodStore.getId(),
                foodStore.getName(),
                foodStore.getAddress(),
                foodStore.getDong(),
                foodStore.getStoreCategory(),
                foodStore.getLatitude(),
                foodStore.getLongitude(),
                foodStore.getImageUrl(),
                2.8
        );
        List<StoreLocationProjection> mockResults = Arrays.asList(result1, result2);

        given(storeRepository.findByLocation(latitude, longitude, radius))
                .willReturn(mockResults);

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(2);
        
        // 카페 매장 검증
        StoreMapResponse cafe = result.get(0);
        assertThat(cafe.storeCategory()).isEqualTo(StoreCategory.CAFE);
        assertThat(cafe.name()).isEqualTo("스타벅스 강남점");
        
        // 음식점 매장 검증
        StoreMapResponse food = result.get(1);
        assertThat(food.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(food.name()).isEqualTo("맛있는 식당");

        then(storeRepository).should(times(1)).findByLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 거리순 정렬 확인")
    void getStoresByLocation_DistanceOrdering_Success() {

        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        Member member1 = createMember(1L);
        Member member2 = createMember(2L);
        Member member3 = createMember(3L);
        
        Store farStore = createStore(member1);
        Store nearStore = createStore(member2);
        Store middleStore = createStore(member3);

        // 거리순으로 정렬된 결과 (가까운 순)
        StoreLocationProjection result1 = projection(
                nearStore.getId(),
                nearStore.getName(),
                nearStore.getAddress(),
                nearStore.getDong(),
                nearStore.getStoreCategory(),
                nearStore.getLatitude(),
                nearStore.getLongitude(),
                nearStore.getImageUrl(),
                0.8
        );
        StoreLocationProjection result2 = projection(
                middleStore.getId(),
                middleStore.getName(),
                middleStore.getAddress(),
                middleStore.getDong(),
                middleStore.getStoreCategory(),
                middleStore.getLatitude(),
                middleStore.getLongitude(),
                middleStore.getImageUrl(),
                2.1
        );
        StoreLocationProjection result3 = projection(
                farStore.getId(),
                farStore.getName(),
                farStore.getAddress(),
                farStore.getDong(),
                farStore.getStoreCategory(),
                farStore.getLatitude(),
                farStore.getLongitude(),
                farStore.getImageUrl(),
                4.5
        );
        List<StoreLocationProjection> mockResults = Arrays.asList(result1, result2, result3);

        given(storeRepository.findByLocation(latitude, longitude, radius))
                .willReturn(mockResults);

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(3);
        
        // 거리순 정렬 확인
        assertThat(result.get(0).distance()).isEqualTo(0.8);
        assertThat(result.get(1).distance()).isEqualTo(2.1);
        assertThat(result.get(2).distance()).isEqualTo(4.5);

        then(storeRepository).should(times(1)).findByLocation(latitude, longitude, radius);
    }

    private CreateStoreRequest createStoreRequest() {
        return new CreateStoreRequest(
                "스타벅스 홍대점",
                "0212345678",
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
    }

    private CreateStoreRequest createFoodStoreRequest() {
        return new CreateStoreRequest(
                "맛있는 식당",
                "0312345678",
                "정말 맛있는 음식을 제공하는 식당입니다.",
                "9876543210",
                "서울시 강남구 테헤란로 456",
                "역삼동",
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

    private Member createMember(Long memberId) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", memberId);
        fieldValues.put("email", "test@example.com");
        fieldValues.put("username", "testuser");
        fieldValues.put("password", "encodedPassword");
        fieldValues.put("phone", "01012345678");
        fieldValues.put("memberType", MemberType.OWNER);
        
        return TestUtils.createEntity(Member.class, fieldValues);
    }

    private Store createStore(Member member) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 1L);
        fieldValues.put("member", member);
        fieldValues.put("name", "스타벅스 홍대점");
        fieldValues.put("phone", "0212345678");
        fieldValues.put("description", "홍대 중심가에 위치한 스타벅스입니다.");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 마포구 홍익로 123");
        fieldValues.put("dong", "홍대동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/store-image.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(7, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(8, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private Store createFoodStore(Member member) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 2L);
        fieldValues.put("member", member);
        fieldValues.put("name", "맛있는 식당");
        fieldValues.put("phone", "0312345678");
        fieldValues.put("description", "정말 맛있는 음식을 제공하는 식당입니다.");
        fieldValues.put("businessNumber", "9876543210");
        fieldValues.put("address", "서울시 강남구 테헤란로 456");
        fieldValues.put("dong", "역삼동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/food-store-image.jpg");
        fieldValues.put("storeCategory", StoreCategory.FOOD);
        fieldValues.put("weekdayOpenTime", LocalTime.of(11, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(12, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private CreateStoreRequest createUpdateRequest() {
        return new CreateStoreRequest(
                "스타벅스 홍대점 수정",
                "0212345679",
                "홍대 중심가에 위치한 스타벅스입니다. (수정됨)",
                "1234567891",
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
    }

    private CreateStoreRequest createFoodUpdateRequest() {
        return new CreateStoreRequest(
                "맛있는 식당",
                "0312345678",
                "정말 맛있는 음식을 제공하는 식당입니다.",
                "9876543210",
                "서울시 강남구 테헤란로 456",
                "역삼동",
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

    private Store createNearStore(Member member) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 3L);
        fieldValues.put("member", member);
        fieldValues.put("name", "가까운 카페");
        fieldValues.put("phone", "0212345678");
        fieldValues.put("description", "가까운 위치의 카페입니다.");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 중구 세종대로 110");
        fieldValues.put("dong", "중림동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/near-cafe.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(7, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(8, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private Store createFarStore(Member member) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 4L);
        fieldValues.put("member", member);
        fieldValues.put("name", "먼 카페");
        fieldValues.put("phone", "0212345679");
        fieldValues.put("description", "먼 위치의 카페입니다.");
        fieldValues.put("businessNumber", "1234567891");
        fieldValues.put("address", "서울시 강남구 테헤란로 123");
        fieldValues.put("dong", "역삼동");
        fieldValues.put("latitude", 37.5000);
        fieldValues.put("longitude", 127.0000);
        fieldValues.put("imageUrl", "https://example.com/far-cafe.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(7, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(8, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private Store createCafeStore(Member member) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 5L);
        fieldValues.put("member", member);
        fieldValues.put("name", "스타벅스 강남점");
        fieldValues.put("phone", "0212345678");
        fieldValues.put("description", "강남에 위치한 스타벅스입니다.");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 강남구 테헤란로 123");
        fieldValues.put("dong", "역삼동");
        fieldValues.put("latitude", 37.5000);
        fieldValues.put("longitude", 127.0000);
        fieldValues.put("imageUrl", "https://example.com/starbucks-gangnam.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(7, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(8, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private StoreLocationProjection projection(Long id,
                                               String name,
                                               String address,
                                               String dong,
                                               StoreCategory category,
                                               Double latitude,
                                               Double longitude,
                                               String imageUrl,
                                               Double distance) {
        return new StoreLocationProjection() {
            @Override public Long getId() { return id; }
            @Override public String getName() { return name; }
            @Override public String getAddress() { return address; }
            @Override public String getDong() { return dong; }
            @Override public String getStoreCategory() { return category.name(); }
            @Override public Double getLatitude() { return latitude; }
            @Override public Double getLongitude() { return longitude; }
            @Override public String getImageUrl() { return imageUrl; }
            @Override public Double getDistance() { return distance; }
        };
    }

    @Test
    @DisplayName("손님용 매장 상세 조회 성공")
    void getStoreDetailForCustomer_Success() {

        // given
        Long storeId = 1L;
        Member member = createMember(10L);
        Store store = createStore(member);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(store));

        // when
        var result = storeService.getStoreDetailForCustomer(storeId);

        // then
        assertThat(result.imageUrl()).isEqualTo(store.getImageUrl());
        assertThat(result.name()).isEqualTo(store.getName());
        assertThat(result.description()).isEqualTo(store.getDescription());
        assertThat(result.storeCategory()).isEqualTo(store.getStoreCategory());
        assertThat(result.address()).isEqualTo(store.getAddress());
        assertThat(result.dong()).isEqualTo(store.getDong());
        assertThat(result.weekdayOpenTime()).isEqualTo(store.getWeekdayOpenTime());
        assertThat(result.weekdayCloseTime()).isEqualTo(store.getWeekdayCloseTime());
        assertThat(result.weekendOpenTime()).isEqualTo(store.getWeekendOpenTime());
        assertThat(result.weekendCloseTime()).isEqualTo(store.getWeekendCloseTime());

        then(storeRepository).should(times(1)).findById(storeId);
    }

    @Test
    @DisplayName("손님용 매장 상세 조회 - 존재하지 않는 매장 예외")
    void getStoreDetailForCustomer_NotFound_ThrowsException() {

        // given
        Long storeId = 999L;

        given(storeRepository.findById(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetailForCustomer(storeId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
    }

    @Test
    @DisplayName("매장명 검색 성공")
    void searchStoresByName_Success() {

        // given
        String keyword = "스타벅스";
        Member member1 = createMember(1L);
        Member member2 = createMember(2L);
        
        Store store1 = createStore(member1);
        Store store2 = createCafeStore(member2);
        List<Store> stores = Arrays.asList(store1, store2);

        given(storeRepository.findByNameContainingIgnoreCase(keyword))
                .willReturn(stores);

        // when
        List<StoreResponse> result = storeService.searchStoresByName(keyword);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo(store1.getName());
        assertThat(result.get(1).name()).isEqualTo(store2.getName());

        then(storeRepository).should(times(1)).findByNameContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("매장명 검색 - 검색 결과 없음")
    void searchStoresByName_NoResults_ReturnsEmptyList() {

        // given
        String keyword = "존재하지않는매장";

        given(storeRepository.findByNameContainingIgnoreCase(keyword))
                .willReturn(Arrays.asList());

        // when
        List<StoreResponse> result = storeService.searchStoresByName(keyword);

        // then
        assertThat(result).isEmpty();

        then(storeRepository).should(times(1)).findByNameContainingIgnoreCase(keyword);
    }
}
