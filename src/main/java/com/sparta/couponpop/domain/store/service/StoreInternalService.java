package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.common.dto.couponevent.response.StoreOwnershipResponse;
import com.sparta.couponpop.common.dto.store.response.StoreResponse;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsStatisticsCursor;

import java.util.List;

public interface StoreInternalService {

    StoreOwnershipResponse checkOwnership(Long storeId, Long memberId);

    List<StoreResponse> findStoresByOwner(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize);
}
