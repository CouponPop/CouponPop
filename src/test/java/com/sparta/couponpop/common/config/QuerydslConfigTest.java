package com.sparta.couponpop.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.entity.QCouponEvent;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

@ActiveProfiles("test")
@DataJpaTest
class QuerydslConfigTest {

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Autowired
    CouponEventRepository couponEventRepository;

    @Test
    @DisplayName("")
    void queryDslTest() {
        // given
        CouponEvent savedCouponEvent = couponEventRepository.save(
                TestUtils.createEntity(CouponEvent.class, Map.of(
                        "name", "아메리카노 1+1"
                ))
        );

        // when
        QCouponEvent couponEvent = QCouponEvent.couponEvent;
        CouponEvent couponEvent1 = jpaQueryFactory
                .selectFrom(couponEvent)
                .where(couponEvent.id.eq(savedCouponEvent.getId()))
                .fetchOne();

        // then
    }
}