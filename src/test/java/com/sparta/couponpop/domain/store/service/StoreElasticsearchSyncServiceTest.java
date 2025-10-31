package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.store.document.StoreDocument;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreElasticsearchSyncService 테스트")
class StoreElasticsearchSyncServiceTest {

    @Mock
    private StoreSearchRepository storeSearchRepository;

    @InjectMocks
    private StoreElasticsearchSyncService elasticsearchSyncService;

    @Test
    @DisplayName("매장 생성 시 Elasticsearch에 인덱싱 성공")
    void indexStore_Success() {
        // given
        Member member = createMember(1L);
        Store store = createStore(member);

        StoreDocument document = StoreDocument.from(store);
        given(storeSearchRepository.save(any(StoreDocument.class))).willReturn(document);

        // when
        elasticsearchSyncService.indexStore(store);

        // then
        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 생성 시 Elasticsearch 인덱싱 실패해도 예외 발생하지 않음")
    void indexStore_ExceptionDoesNotPropagate() {
        // given
        Member member = createMember(1L);
        Store store = createStore(member);

        given(storeSearchRepository.save(any(StoreDocument.class)))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when & then
        // 예외가 발생하지 않아야 함
        elasticsearchSyncService.indexStore(store);

        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 수정 시 Elasticsearch 업데이트 성공")
    void updateStore_Success() {
        // given
        Member member = createMember(1L);
        Store store = createStore(member);
        store.updateStoreInfo(
                "스타벅스 홍대점 (수정)",
                "02123456789",
                "수정된 설명",
                "1234567890",
                "서울시 마포구 홍익로 124",
                "홍대동",
                37.5666,
                126.9781,
                "https://example.com/updated.jpg",
                StoreCategory.CAFE,
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                LocalTime.of(9, 0),
                LocalTime.of(23, 30)
        );

        StoreDocument document = StoreDocument.from(store);
        given(storeSearchRepository.save(any(StoreDocument.class))).willReturn(document);

        // when
        elasticsearchSyncService.updateStore(store);

        // then
        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 수정 시 Elasticsearch 업데이트 실패해도 예외 발생하지 않음")
    void updateStore_ExceptionDoesNotPropagate() {
        // given
        Member member = createMember(1L);
        Store store = createStore(member);

        given(storeSearchRepository.save(any(StoreDocument.class)))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when & then
        // 예외가 발생하지 않아야 함
        elasticsearchSyncService.updateStore(store);

        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 삭제 시 Elasticsearch에서 삭제 성공")
    void deleteStore_Success() {
        // given
        Long storeId = 1L;

        // when
        elasticsearchSyncService.deleteStore(storeId);

        // then
        then(storeSearchRepository).should(times(1)).deleteByStoreId(storeId);
    }

    @Test
    @DisplayName("매장 삭제 시 Elasticsearch 삭제 실패해도 예외 발생하지 않음")
    void deleteStore_ExceptionDoesNotPropagate() {
        // given
        Long storeId = 1L;
        org.mockito.Mockito.doThrow(new RuntimeException("Elasticsearch error"))
                .when(storeSearchRepository).deleteByStoreId(storeId);

        // when & then
        // 예외가 발생하지 않아야 함
        elasticsearchSyncService.deleteStore(storeId);

        then(storeSearchRepository).should(times(1)).deleteByStoreId(storeId);
    }

    @Test
    @DisplayName("다양한 카테고리 매장 인덱싱 성공")
    void indexStore_DifferentCategories_Success() {
        // given
        Member member = createMember(1L);
        
        Store cafeStore = createStoreWithCategory(member, StoreCategory.CAFE);
        Store foodStore = createStoreWithCategory(member, StoreCategory.FOOD);
        Store convenienceStore = createStoreWithCategory(member, StoreCategory.CONVENIENCE);

        given(storeSearchRepository.save(any(StoreDocument.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        elasticsearchSyncService.indexStore(cafeStore);
        elasticsearchSyncService.indexStore(foodStore);
        elasticsearchSyncService.indexStore(convenienceStore);

        // then
        then(storeSearchRepository).should(times(3)).save(any(StoreDocument.class));
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
        fieldValues.put("phone", "02123456789");
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

    private Store createStoreWithCategory(Member member, StoreCategory category) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 1L);
        fieldValues.put("member", member);
        fieldValues.put("name", "테스트 매장");
        fieldValues.put("phone", "02123456789");
        fieldValues.put("description", "테스트 매장입니다.");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 테스트구");
        fieldValues.put("dong", "테스트동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/image.jpg");
        fieldValues.put("storeCategory", category);
        fieldValues.put("weekdayOpenTime", LocalTime.of(9, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(10, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }
}

