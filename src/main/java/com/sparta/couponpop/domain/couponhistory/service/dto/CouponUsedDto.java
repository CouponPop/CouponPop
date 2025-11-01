package com.sparta.couponpop.domain.couponhistory.service.dto;

import com.sparta.couponpop.domain.coupon.enums.CouponStatus;

public record CouponUsedDto(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        CouponStatus couponStatus
) {
}
