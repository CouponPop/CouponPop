package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse createStore(Long memberId, CreateStoreRequest request) {

        Store store = Store.createStore(
                memberId,
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
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        // TODO: 인증 구현 후 매장 소유자 검증 로직 추가
        // if (!store.getMemberId().equals(memberId)) {
        //     throw new IllegalArgumentException("매장 수정 권한이 없습니다.");
        // }

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

        Store updatedStore = storeRepository.save(store);

        return StoreResponse.from(updatedStore);
    }
}
