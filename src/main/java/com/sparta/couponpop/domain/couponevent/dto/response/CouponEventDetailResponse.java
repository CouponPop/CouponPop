package com.sparta.couponpop.domain.couponevent.dto.response;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.repository.dto.CouponEventWithUsedCountProjection;

import java.time.LocalDateTime;

public record CouponEventDetailResponse(
        Long id,
        String eventName,
        EventPeriod eventPeriod,
        EventStatisticSummary summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponEventDetailResponse of(CouponEvent couponEvent, int usedCouponCount, LocalDateTime now) {
        return new CouponEventDetailResponse(
                couponEvent.getId(),
                couponEvent.getName(),
                EventPeriod.of(couponEvent.getEventStartAt(), couponEvent.getEventEndAt()),
                EventStatisticSummary.of(couponEvent, usedCouponCount),
                couponEvent.getCreatedAt(),
                couponEvent.getUpdatedAt()
        );
    }

    public static CouponEventDetailResponse of(CouponEventWithUsedCountProjection eventWithUsedCount) {
        return new CouponEventDetailResponse(
                eventWithUsedCount.id(),
                eventWithUsedCount.eventName(),
                EventPeriod.of(eventWithUsedCount.start(), eventWithUsedCount.end()),
                EventStatisticSummary.of(eventWithUsedCount.totalCount(), eventWithUsedCount.issuedCount(), eventWithUsedCount.usedCouponCount()),
                eventWithUsedCount.createdAt(),
                eventWithUsedCount.updatedAt()
        );
    }
}
