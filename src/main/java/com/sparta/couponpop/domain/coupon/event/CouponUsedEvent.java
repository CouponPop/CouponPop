package com.sparta.couponpop.domain.coupon.event;

import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.couponhistory.service.dto.CouponUsedDto;

public record CouponUsedEvent(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        CouponStatus couponStatus
) {

    public static CouponUsedEvent of(Long couponId, Long memberId, Long storeId, Long eventId) {
        return new CouponUsedEvent(couponId, memberId, storeId, eventId, CouponStatus.USED);
    }

    public CouponUsedDto toCouponHistory() {
        return new CouponUsedDto(couponId, memberId, storeId, eventId, couponStatus);
    }
}
