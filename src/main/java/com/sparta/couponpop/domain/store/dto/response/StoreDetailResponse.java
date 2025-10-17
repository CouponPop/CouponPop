package com.sparta.couponpop.domain.store.dto.response;

import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import lombok.Builder;

import java.time.LocalTime;

@Builder
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
        return StoreDetailResponse.builder()
                .imageUrl(store.getImageUrl())
                .name(store.getName())
                .description(store.getDescription())
                .storeCategory(store.getStoreCategory())
                .address(store.getAddress())
                .weekdayOpenTime(store.getWeekdayOpenTime())
                .weekdayCloseTime(store.getWeekdayCloseTime())
                .weekendOpenTime(store.getWeekendOpenTime())
                .weekendCloseTime(store.getWeekendCloseTime())
                .build();
    }
}


