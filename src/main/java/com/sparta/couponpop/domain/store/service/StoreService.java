package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreDetailResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreLocationProjection;
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
    private final MemberRepository memberRepository;

    @Transactional
    public StoreResponse createStore(Long memberId, CreateStoreRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.MEMBER_NOT_FOUND));

        Store store = Store.createStore(
                member,
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
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

        return StoreResponse.from(savedStore);
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, Long memberId, CreateStoreRequest request) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 검증
        if (!store.getMember().getId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_UPDATE_PERMISSION_DENIED);
        }

        store.updateStoreInfo(
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );

        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getStoresByOwner(Long memberId) {

        List<Store> stores = storeRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return stores.stream()
                .map(StoreResponse::from)
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
        if (!store.getMember().getId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_DELETE_PERMISSION_DENIED);
        }

        store.deleteStore();
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(Long storeId, Long memberId) {

        Store store = storeRepository.findByIdWithMember(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        if (!store.getMember().getId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_ACCESS_PERMISSION_DENIED);
        }

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreMapResponse> getStoresByLocation(double latitude, double longitude, double radiusKm) {

        return storeRepository.findByLocation(latitude, longitude, radiusKm)
            .stream()
            .map(StoreLocationProjection::toStoreMapResponse)
            .toList();
    }
}
