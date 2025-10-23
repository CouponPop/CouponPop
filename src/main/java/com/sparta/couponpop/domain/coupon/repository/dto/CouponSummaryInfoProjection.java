package com.sparta.couponpop.domain.coupon.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.store.enums.StoreCategory;

import java.time.LocalDateTime;

public record CouponSummaryInfoProjection(
        Long id,
        CouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expireAt,
        LocalDateTime usedAt,
        EventInfo event,
        StoreInfo store
) {

    public record EventInfo(
            Long id,
            String name,
            EventPeriod period
    ) {
        public record EventPeriod(LocalDateTime start, LocalDateTime end) {
        }
    }

    public record StoreInfo(
            Long id,
            String name,
            StoreCategory storeCategory,
            double latitude,
            double longitude,
            String imageUrl
    ) {
    }

    @QueryProjection
    public CouponSummaryInfoProjection(
            Long id,
            CouponStatus status,
            LocalDateTime issuedAt,
            LocalDateTime expireAt,
            LocalDateTime usedAt,
            Long eventId,
            String eventName,
            LocalDateTime eventStartAt,
            LocalDateTime eventEndAt,
            Long storeId,
            String storeName,
            StoreCategory storeCategory,
            double latitude,
            double longitude,
            String imageUrl
    ) {
        this(id, status, issuedAt, expireAt, usedAt,
                new EventInfo(eventId, eventName, new EventInfo.EventPeriod(eventStartAt, eventEndAt)),
                new StoreInfo(storeId, storeName, storeCategory, latitude, longitude, imageUrl)
        );
    }
}
