package com.sparta.couponpop.domain.store.dto.response;

import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;

public record StoreMapResponse(
        Long id,
        String name,
        String address,
        StoreCategory storeCategory,
        double latitude,
        double longitude,
        String imageUrl,
        double distance // km 단위
) {
    public static StoreMapResponse from(Store store, double distance) {
        return new StoreMapResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getStoreCategory(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImageUrl(),
                distance
        );
    }
}
