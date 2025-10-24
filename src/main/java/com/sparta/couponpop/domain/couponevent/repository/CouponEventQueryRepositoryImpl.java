package com.sparta.couponpop.domain.couponevent.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.couponpop.domain.coupon.entity.QCoupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.sparta.couponpop.domain.couponevent.entity.QCouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.repository.dto.CouponEventWithUsedCountProjection;
import com.sparta.couponpop.domain.couponevent.repository.dto.QCouponEventWithUsedCountProjection;
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
    public List<CouponEventWithUsedCountProjection> fetchCouponEventsByStore(
            Store store,
            CouponEventStatus eventStatus,
            LocalDateTime now,
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
                        eventPeriodCondition(couponEvent, now, eventStatus),
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

    private BooleanExpression storeEq(QCouponEvent couponEvent, Store store) {
        if (ObjectUtils.isEmpty(store)) {
            return null;
        }
        return couponEvent.store.eq(store);
    }

    private BooleanExpression eventPeriodCondition(QCouponEvent event, LocalDateTime now, CouponEventStatus eventStatus) {
        if (eventStatus == null) {
            return null;
        }

        return switch (eventStatus) {
            case IN_PROGRESS -> event.eventStartAt.loe(now).and(event.eventEndAt.goe(now));
            case COMPLETED -> event.eventEndAt.lt(now);
            default -> throw new IllegalStateException("Unexpected value: " + eventStatus);
        };
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
}
