package com.sparta.couponpop.common.dto.store.response;

import com.sparta.couponpop.domain.store.enums.StoreCategory;

public record StoreResponse(
        Long id,
        String name,
        StoreCategory storeCategory,
        double latitude,
        double longitude,
        String imageUrl
) {
    public static StoreResponse of(Long id, String name, StoreCategory storeCategory, double latitude, double longitude, String imageUrl) {
        return new StoreResponse(id, name, storeCategory, latitude, longitude, imageUrl);
    }
}
