package com.sparta.couponpop.domain.couponevent.dto.response;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;

import java.time.LocalDateTime;

public record CouponEventDetailResponse(
        Long id,
        String eventName,
        EventPeriod eventPeriod,
        CouponEventStatus eventStatus,
        EventStatisticSummary summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponEventDetailResponse of(CouponEvent couponEvent, int usedCouponCount, LocalDateTime now) {
        return new CouponEventDetailResponse(
                couponEvent.getId(),
                couponEvent.getName(),
                EventPeriod.of(couponEvent.getEventStartAt(), couponEvent.getEventEndAt()),
                couponEvent.getCurrentStatus(now),
                EventStatisticSummary.of(couponEvent, usedCouponCount),
                couponEvent.getCreatedAt(),
                couponEvent.getUpdatedAt()
        );
    }
}
