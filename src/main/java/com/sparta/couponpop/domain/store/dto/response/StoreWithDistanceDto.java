package com.sparta.couponpop.domain.store.dto.response;

import com.sparta.couponpop.domain.store.enums.StoreCategory;

/**
 * 위치 기반 매장 조회를 위한 타입 안전한 DTO
 * 생성자 표현식과 함께 사용됩니다.
 */
public record StoreWithDistanceDto(
        Long id,
        String name,
        String address,
        StoreCategory storeCategory,
        Double latitude,
        Double longitude,
        String imageUrl,
        Double distance
) {
    
    /**
     * StoreWithDistanceDto를 StoreMapResponse로 변환합니다.
     */
    public StoreMapResponse toStoreMapResponse() {
        return new StoreMapResponse(
                id,
                name,
                address,
                storeCategory,
                latitude,
                longitude,
                imageUrl,
                distance
        );
    }
}
