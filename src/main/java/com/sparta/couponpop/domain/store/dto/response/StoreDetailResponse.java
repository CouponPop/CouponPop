package com.sparta.couponpop.domain.store.dto.response;

import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;

import java.time.LocalTime;

public record StoreDetailResponse(
        String imageUrl,
        String name,
        String description,
        StoreCategory storeCategory,
        String address,
        LocalTime weekdayOpenTime,
        LocalTime weekdayCloseTime,
        LocalTime weekendOpenTime,
        LocalTime weekendCloseTime
) {
    public static StoreDetailResponse from(Store store) {
        return new StoreDetailResponse(
                store.getImageUrl(),
                store.getName(),
                store.getDescription(),
                store.getStoreCategory(),
                store.getAddress(),
                store.getWeekdayOpenTime(),
                store.getWeekdayCloseTime(),
                store.getWeekendOpenTime(),
                store.getWeekendCloseTime()
        );
    }
}


