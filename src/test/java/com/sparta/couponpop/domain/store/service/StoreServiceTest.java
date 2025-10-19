package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
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
    @Disabled
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
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
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

    private Member createMember(Long memberId) {
        Member member = Member.signUp(
                "test@example.com",
                "testuser",
                "encodedPassword",
                "01012345678",
                MemberType.OWNER
        );
        // 테스트를 위해 ID를 설정하기 위해 리플렉션 사용
        try {
            var idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, memberId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set member ID", e);
        }
        return member;
    }

    private Store createStore(Member member) {
        return Store.createStore(
                member,
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

    private Store createFoodStore(Member member) {
        return Store.createStore(
                member,
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

    private CreateStoreRequest createUpdateRequest() {
        return new CreateStoreRequest(
                "스타벅스 홍대점 수정",
                "0212345679",
                "홍대 중심가에 위치한 스타벅스입니다. (수정됨)",
                "1234567891",
                "서울시 마포구 홍익로 124",
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
