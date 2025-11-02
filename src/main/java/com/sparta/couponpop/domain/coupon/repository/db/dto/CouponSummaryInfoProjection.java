package com.sparta.couponpop.domain.coupon.repository.db.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;

import java.time.LocalDateTime;

public record CouponSummaryInfoProjection(
        Long id,
        CouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expireAt,
        LocalDateTime usedAt,
        EventInfo event,
        Long storeId
) {

    public record EventInfo(
            Long id,
            String name,
            EventPeriod period
    ) {
        public record EventPeriod(LocalDateTime start, LocalDateTime end) {
        }
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
            Long storeId
    ) {
        this(
                id, status, issuedAt, expireAt, usedAt,
                new EventInfo(eventId, eventName, new EventInfo.EventPeriod(eventStartAt, eventEndAt)),
                storeId
        );
    }
}
