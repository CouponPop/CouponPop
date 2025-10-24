package com.sparta.couponpop.domain.couponevent.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.couponpop.domain.coupon.entity.QCoupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsStatisticsCursor;
import com.sparta.couponpop.domain.couponevent.entity.QCouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.repository.dto.*;
import com.sparta.couponpop.domain.store.entity.QStore;
import com.sparta.couponpop.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CouponEventQueryRepositoryImpl implements CouponEventQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CouponEventWithUsedCountProjection> fetchCouponEventsByStoreAndStatus(
            Store store,
            CouponEventStatus eventStatus,
            StoreCouponEventsCursor cursor,
            int limit
    ) {

        QCouponEvent couponEvent = QCouponEvent.couponEvent;
        QCoupon coupon = QCoupon.coupon;

        return jpaQueryFactory
                .select(
                        new QCouponEventWithUsedCountProjection(
                                couponEvent.id,
                                couponEvent.name,
                                couponEvent.eventStartAt,
                                couponEvent.eventEndAt,
                                couponEvent.couponEventStatus,
                                couponEvent.totalCount,
                                couponEvent.issuedCount,
                                coupon.count().intValue(),
                                couponEvent.createdAt,
                                couponEvent.updatedAt
                        )
                )
                .from(couponEvent)
                .leftJoin(coupon).on(
                        coupon.couponEvent.eq(couponEvent)
                                .and(coupon.couponStatus.eq(CouponStatus.USED))
                )
                .where(
                        storeEq(couponEvent, store),
                        eventStatusEq(couponEvent, eventStatus),
                        nextEventCondition(couponEvent, cursor.lastStartAt(), cursor.lastEndAt(), cursor.lastEventId())
                )
                .groupBy(couponEvent.id)
                .orderBy(
                        couponEvent.eventStartAt.asc(),
                        couponEvent.eventEndAt.asc(),
                        couponEvent.id.asc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public List<StoreCouponEventStatisticsProjection> fetchStoreCouponEventStatistics(Long memberId, StoreCouponEventsStatisticsCursor cursor, int limit) {
        QStore store = QStore.store;
        QCouponEvent couponEvent = QCouponEvent.couponEvent;
        QCoupon coupon = QCoupon.coupon;

        NumberExpression<Integer> usedCount = Expressions.numberTemplate(Integer.class,
                "sum(case when {0}.usedAt is not null then 1 else 0 end)", coupon);
        return jpaQueryFactory
                .select(
                        new QStoreCouponEventStatisticsProjection(
                                store.id,
                                store.name,
                                new QStoreCouponEventStatisticsProjection_CouponStats(
                                        couponEvent.totalCount.sum().coalesce(0),
                                        couponEvent.issuedCount.sum().coalesce(0),
                                        usedCount.coalesce(0)
                                ),
                                couponEvent.eventEndAt.max()
                        )
                )
                .from(store)
                .leftJoin(couponEvent).on(couponEvent.store.eq(store))
                .leftJoin(coupon).on(coupon.couponEvent.eq(couponEvent))
                .where(
                        store.member.id.eq(memberId),
                        nextStatisticCondition(store, cursor.lastStoreId())
                )
                .groupBy(store.id)
                .orderBy(store.id.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression storeEq(QCouponEvent couponEvent, Store store) {
        if (ObjectUtils.isEmpty(store)) {
            return null;
        }
        return couponEvent.store.eq(store);
    }

    private BooleanExpression eventStatusEq(QCouponEvent couponEvent, CouponEventStatus eventStatus) {
        if (ObjectUtils.isEmpty(eventStatus)) {
            return null;
        }
        return couponEvent.couponEventStatus.eq(eventStatus);
    }

    /**
     * no-offset 페이징 조건:
     * 1) eventStartAt > lastStartAt 이면 다음 페이지
     * 2) eventStartAt 같으면 eventEndAt > lastEndAt
     * 3) 둘 다 같으면 id > lastEventId
     */
    private BooleanExpression nextEventCondition(QCouponEvent couponEvent, LocalDateTime lastStartAt, LocalDateTime lastEndAt, Long lastEventId) {
        if (lastStartAt == null || lastEndAt == null || lastEventId == null) {
            return null;
        }

        return couponEvent.eventStartAt.gt(lastStartAt)
                .or(
                        couponEvent.eventStartAt.eq(lastStartAt)
                                .and(couponEvent.eventEndAt.gt(lastEndAt))
                )
                .or(
                        couponEvent.eventStartAt.eq(lastStartAt)
                                .and(couponEvent.eventEndAt.eq(lastEndAt))
                                .and(couponEvent.id.gt(lastEventId))
                );
    }

    private BooleanExpression nextStatisticCondition(QStore store, Long lastStoreId) {
        if (lastStoreId == null) {
            return null;
        }
        return store.id.lt(lastStoreId);
    }
}
