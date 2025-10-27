package com.sparta.couponpop.domain.couponevent.repository;

import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsStatisticsCursor;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.repository.dto.CouponEventWithUsedCountProjection;
import com.sparta.couponpop.domain.couponevent.repository.dto.StoreCouponEventStatisticsProjection;
import com.sparta.couponpop.domain.store.entity.Store;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponEventQueryRepository {

    List<CouponEventWithUsedCountProjection> fetchCouponEventsByStore(
            Store store,
            CouponEventStatus eventStatus,
            LocalDateTime now,
            StoreCouponEventsCursor cursor,
            int limit
    );

    List<StoreCouponEventStatisticsProjection> fetchStoreCouponEventStatistics(Long memberId, StoreCouponEventsStatisticsCursor cursor, int limit);
}
