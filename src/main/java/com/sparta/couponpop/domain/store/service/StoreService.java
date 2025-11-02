package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.common.dto.member.response.MemberResponse;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.service.MemberInternalService;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreDetailResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreMapResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.exception.StoreErrorCode;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final MemberInternalService memberInternalService;
    private final StoreElasticsearchSyncService elasticsearchSyncService;
    private final StoreSearchService storeSearchService;

    @Transactional
    public StoreResponse createStore(Long memberId, CreateStoreRequest request) {

        // Member 존재 여부 확인 및 정보 조회
        MemberResponse member = memberInternalService.getMemberById(memberId);

        Store store = Store.createStore(
                memberId,
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.dong(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );

        Store savedStore = storeRepository.save(store);
        
        // Elasticsearch에 동기화
        elasticsearchSyncService.indexStore(savedStore);

        return StoreResponse.from(savedStore, member);
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, Long memberId, CreateStoreRequest request) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 검증
        if (!store.getMemberId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_UPDATE_PERMISSION_DENIED);
        }

        store.updateStoreInfo(
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.dong(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );
        
        // Elasticsearch에 동기화
        elasticsearchSyncService.updateStore(store);

        // Member 정보 조회
        MemberResponse member = memberInternalService.getMemberById(memberId);

        return StoreResponse.from(store, member);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getStoresByOwner(Long memberId) {

        List<Store> stores = storeRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        
        // Member 정보 조회
        MemberResponse member = memberInternalService.getMemberById(memberId);

        return stores.stream()
                .map(store -> StoreResponse.from(store, member))
                .toList();
    }

    @Transactional
    public void deleteStore(Long storeId, Long memberId) {

        Store store = storeRepository.findByIdIncludingDeleted(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 이미 삭제된 매장인지 확인
        if (store.getDeletedAt() != null) {
            throw new GlobalException(StoreErrorCode.STORE_ALREADY_DELETED);
        }

        // 매장 소유자 검증
        if (!store.getMemberId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_DELETE_PERMISSION_DENIED);
        }

        store.deleteStore();
        
        // Elasticsearch에서 삭제
        elasticsearchSyncService.deleteStore(storeId);
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(Long storeId, Long memberId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        if (!store.getMemberId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_ACCESS_PERMISSION_DENIED);
        }

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetailForCustomer(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> searchStoresByName(String keyword) {
        // Elasticsearch를 사용한 검색으로 변경
        return storeSearchService.searchStoresByName(keyword);
    }

    @Transactional(readOnly = true)
    public List<StoreMapResponse> getStoresByLocation(double latitude, double longitude, double radiusKm) {
        // Elasticsearch를 사용한 위치 기반 검색으로 변경
        return storeSearchService.searchStoresByLocation(latitude, longitude, radiusKm);
    }
}
