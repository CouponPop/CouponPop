package com.sparta.couponpop.domain.coupon.dto.response;

import com.sparta.couponpop.common.dto.store.response.StoreResponse;
import com.sparta.couponpop.domain.store.enums.StoreCategory;

public record StoreInfoResponse(
        Long id,
        String name,
        StoreCategory storeCategory,
        double latitude,
        double longitude,
        String imageUrl
) {

    public static StoreInfoResponse from(StoreResponse store) {
        return new StoreInfoResponse(
                store.id(),
                store.name(),
                store.storeCategory(),
                store.latitude(),
                store.longitude(),
                store.imageUrl()
        );
    }
}
