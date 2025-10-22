package com.sparta.couponpop.domain.store.dto.response;

public interface StoreAndCouponEventCountProjection {
    Long getOpenStoreCount();

    Long getActiveCouponEventCount();
}
