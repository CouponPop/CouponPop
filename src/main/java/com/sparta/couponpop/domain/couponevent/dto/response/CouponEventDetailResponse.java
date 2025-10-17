package com.sparta.couponpop.domain.couponevent.dto.response;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CouponEventDetailResponse(
        Long id,
        String name,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        CouponEventStatus eventStatus,
        int totalCount, // 총 발급 수량
        int unclaimedCount, // 미수령 개수
        int issuedCount, // 수령 개수
        int usedCount, // 사용 개수
        int unusedCount, // 미사용 개수
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponEventDetailResponse of(CouponEvent couponEvent, int usedCouponCount, LocalDateTime now) {
        int totalCount = couponEvent.getTotalCount();
        int issuedCount = couponEvent.getIssuedCount();
        int unusedCount = issuedCount - usedCouponCount;
        int unclaimedCount = totalCount - issuedCount;
        return CouponEventDetailResponse.builder()
                .id(couponEvent.getId())
                .name(couponEvent.getName())
                .eventStartAt(couponEvent.getEventStartAt())
                .eventEndAt(couponEvent.getEventEndAt())
                .eventStatus(couponEvent.getCurrentStatus(now))
                .totalCount(totalCount)
                .unclaimedCount(unclaimedCount)
                .issuedCount(issuedCount)
                .usedCount(usedCouponCount)
                .unusedCount(unusedCount)
                .createdAt(couponEvent.getCreatedAt())
                .updatedAt(couponEvent.getUpdatedAt())
                .build();
    }
}
