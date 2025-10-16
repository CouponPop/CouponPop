package com.sparta.couponpop.domain.couponevent.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponEventServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private CouponEventService couponEventService;

    @Nested
    @DisplayName("쿠폰 이벤트 생성")
    class CreateCouponEvent {
        @Test
        @DisplayName("쿠폰 이벤트 생성 - 성공")
        void createCouponEvent_success() {
            // given
            LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 17, 0);
            LocalDateTime eventEndAt = LocalDateTime.of(2025, 10, 15, 12, 0);

            CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                    .storeId(1L)
                    .name("아이스 아메리카노 1+1")
                    .eventStartAt(eventStartAt)
                    .eventEndAt(eventEndAt)
                    .totalCount(30)
                    .build();

            Long userId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", userId));
            Store store = TestUtils.createEntity(Store.class, Map.of("member", member));
            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));

            CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", 1L,
                    "name", "아이스 아메리카노 1+1",
                    "eventStartAt", eventStartAt,
                    "eventEndAt", eventEndAt,
                    "couponEventStatus", CouponEventStatus.SCHEDULED
            ));
            given(couponEventRepository.save(any(CouponEvent.class))).willReturn(couponEvent);

            // when
            CreateCouponEventResponse response = couponEventService.createCouponEvent(request, userId);

            // then
            assertThat(response)
                    .extracting("eventId", "name", "eventStartAt", "eventEndAt", "eventStatus")
                    .contains(
                            1L, "아이스 아메리카노 1+1", eventStartAt, eventEndAt, CouponEventStatus.SCHEDULED
                    );
        }

        @Test
        @DisplayName("이벤트 기간이 48시간을 초과하면 예외 발생")
        void createCouponEvent_fail_eventDurationExceeded() {
            // given
            LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 12, 0);
            LocalDateTime eventEndAt = eventStartAt.plusHours(49); // 49시간 → 초과

            CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                    .storeId(1L)
                    .name("아이스 아메리카노 1+1")
                    .eventStartAt(eventStartAt)
                    .eventEndAt(eventEndAt)
                    .totalCount(30)
                    .build();

            Long userId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", userId));
            Store store = TestUtils.createEntity(Store.class, Map.of("member", member));

            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> couponEventService.createCouponEvent(request, userId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_DURATION_EXCEEDED.getMessage());
        }

        @Test
        @DisplayName("이벤트 종료 시간이 시작 시간보다 이전이면 예외 발생")
        void createCouponEvent_fail_eventEndBeforeStart() {
            // given
            LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 12, 0);
            LocalDateTime eventEndAt = eventStartAt.minusHours(1); // 종료 < 시작

            CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                    .storeId(1L)
                    .name("아이스 아메리카노 1+1")
                    .eventStartAt(eventStartAt)
                    .eventEndAt(eventEndAt)
                    .totalCount(30)
                    .build();

            Long userId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", userId));
            Store store = TestUtils.createEntity(Store.class, Map.of("member", member));

            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> couponEventService.createCouponEvent(request, userId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_END_BEFORE_START.getMessage());
        }
    }


}