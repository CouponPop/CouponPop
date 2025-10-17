package com.sparta.couponpop.domain.couponevent.dto.response;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
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
        return CouponEventDetailResponse.builder()
                .id(couponEvent.getId())
                .eventName(couponEvent.getName())
                .eventPeriod(
                        new EventPeriod(couponEvent.getEventStartAt(), couponEvent.getEventEndAt())
                )
                .eventStatus(couponEvent.getCurrentStatus(now))
                .summary(
                        new EventStatisticSummary(
                                couponEvent, usedCouponCount
                        )
                )
                .createdAt(couponEvent.getCreatedAt())
                .updatedAt(couponEvent.getUpdatedAt())
                .build();
    }
}
