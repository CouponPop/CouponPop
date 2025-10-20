package com.sparta.couponpop.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;

public record CouponIssueRequest(

        @NotNull(message = "쿠폰 수령을 위한 매장 ID 는 필수입니다.")
        Long storeId,

        @NotNull(message = "쿠폰 수령을 위한 이벤트 ID 는 필수입니다.")
        Long eventId
) {
}
