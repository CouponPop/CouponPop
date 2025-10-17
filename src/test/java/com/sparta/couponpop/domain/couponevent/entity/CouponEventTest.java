package com.sparta.couponpop.domain.couponevent.entity;

import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CouponEventTest {

    @Test
    @DisplayName("(현재 시간 < 이벤트 시작 시간)이면 SCHEDULED(이벤트 예약)")
    void getCurrentStatus_whenScheduled() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 10, 17, 12, 59);

        CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "eventStartAt", now.plusMinutes(1),
                "eventEndAt", LocalDateTime.of(2025, 10, 17, 20, 0)
        ));

        // when
        CouponEventStatus currentEventStatus = couponEvent.getCurrentStatus(now);

        // then
        assertThat(currentEventStatus).isEqualTo(CouponEventStatus.SCHEDULED);
    }

    @Test
    @DisplayName("(현재 시간 > 이벤트 종료 시간)이면 COMPLETED(이벤트 완료)")
    void getCurrentStatus_whenCompleted() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 10, 17, 20, 1);

        CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "eventStartAt", LocalDateTime.of(2025, 10, 17, 13, 0),
                "eventEndAt", now.minusMinutes(1)
        ));

        // when
        CouponEventStatus currentEventStatus = couponEvent.getCurrentStatus(now);

        // then
        assertThat(currentEventStatus).isEqualTo(CouponEventStatus.COMPLETED);
    }

    @Test
    @DisplayName("(이벤트 시작 시간 <= 현재 시간 <= 이벤트 종료 시간이)면 IN_PROGRESS(이벤트 진행중)")
    void getCurrentStatus_whenInProgress() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 10, 17, 2, 0);

        CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "eventStartAt", now.minusMinutes(1),
                "eventEndAt", now.plusMinutes(1)
        ));

        // when
        CouponEventStatus currentEventStatus = couponEvent.getCurrentStatus(now);

        // then
        assertThat(currentEventStatus).isEqualTo(CouponEventStatus.IN_PROGRESS);
    }

}