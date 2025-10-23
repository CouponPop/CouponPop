package com.sparta.couponpop.domain.couponevent.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;

import java.time.LocalDateTime;

public record CouponEventWithUsedCountProjection(
        Long id,
        String eventName,
        LocalDateTime start,
        LocalDateTime end,
        CouponEventStatus eventStatus,
        int totalCount,
        int issuedCount,
        int usedCouponCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    @QueryProjection
    public CouponEventWithUsedCountProjection {
    }
}
