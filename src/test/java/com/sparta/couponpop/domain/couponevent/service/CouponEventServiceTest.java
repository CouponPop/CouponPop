package com.sparta.couponpop.domain.couponevent.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.StoreCouponEventListResponse;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.couponevent.repository.dto.CouponEventWithUsedCountProjection;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponEventServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CouponRepository couponRepository;

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

    @Nested
    @DisplayName("쿠폰 이벤트 상세 조회")
    class GetCouponEvent {

        @Test
        @DisplayName("쿠폰 이벤트 조회 - 성공")
        void getCouponEvent_success() {
            // given
            Long eventId = 1L;
            Long loginUserId = 1L;
            LocalDateTime now = LocalDateTime.of(2025, 10, 17, 14, 0);
            LocalDateTime eventStartAt = now.minusDays(1);
            LocalDateTime eventEndAt = now.plusDays(1);

            Member member = TestUtils.createEntity(Member.class, Map.of("id", loginUserId));
            Store store = TestUtils.createEntity(Store.class, Map.of(
                    "id", 1L,
                    "member", member
            ));
            CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", eventId,
                    "name", "아이스 아메리카노 1+1",
                    "eventStartAt", eventStartAt,
                    "eventEndAt", eventEndAt,
                    "totalCount", 30,
                    "issuedCount", 10,
                    "couponEventStatus", CouponEventStatus.IN_PROGRESS,
                    "store", store
            ));
            int usedCouponCount = 5;

            given(couponEventRepository.findByIdWithStoreAndMember(anyLong())).willReturn(Optional.of(couponEvent));
            given(couponRepository.countByEventIdAndStatus(anyLong(), any(CouponStatus.class))).willReturn(usedCouponCount);

            // when
            CouponEventDetailResponse response = couponEventService.getCouponEvent(eventId, loginUserId, now);

            // then
            assertThat(response.summary())
                    .extracting("total", "unclaimed", "issued", "used", "unused")
                    .containsExactly(30, 20, 10, 5, 5);

            assertThat(response)
                    .extracting("eventName", "eventStatus", "eventPeriod.start", "eventPeriod.end")
                    .containsExactly("아이스 아메리카노 1+1", CouponEventStatus.IN_PROGRESS, eventStartAt, eventEndAt);
        }

        @Test
        @DisplayName("소유자 불일치 시 예외 발생 - 실패")
        void getCouponEvent_thenOwnerMismatch_throwsException() {
            // given
            Long eventId = 1L;
            Long loginUserId = 1L;
            LocalDateTime now = LocalDateTime.of(2025, 10, 17, 14, 0);

            Member member = TestUtils.createEntity(Member.class, Map.of("id", 2L));
            Store store = TestUtils.createEntity(Store.class, Map.of(
                    "id", 1L,
                    "member", member
            ));
            CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", eventId,
                    "store", store
            ));

            given(couponEventRepository.findByIdWithStoreAndMember(anyLong())).willReturn(Optional.of(couponEvent));

            // when & then
            assertThatThrownBy(() -> couponEventService.getCouponEvent(eventId, loginUserId, now))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_OWNER_MISMATCH.getMessage());
        }
    }

    @Nested
    @DisplayName("매장 별 이벤트 목록 조회")
    class GetCouponEventsByStoreTests {

        private static final LocalDateTime now = LocalDateTime.of(2025, 10, 20, 16, 0);

        private CouponEventWithUsedCountProjection createMockCoupon(Long id, String name, LocalDateTime start, LocalDateTime end) {
            return new CouponEventWithUsedCountProjection(
                    id,
                    name,
                    start,
                    end,
                    CouponEventStatus.IN_PROGRESS,
                    100,
                    50,
                    25,
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusDays(1)
            );
        }

        @Test
        @DisplayName("첫 페이지 조회 - hasNext true")
        void getCouponEventsByStore_firstPage_hasNextTrue() {
            // given
            var cursor = StoreCouponEventsCursor.first();
            List<CouponEventWithUsedCountProjection> mockedProjections = List.of(
                    createMockCoupon(1L, "이벤트1", now.minusDays(1), now.plusDays(1)),
                    createMockCoupon(2L, "이벤트2", now.minusDays(1), now.plusDays(1)),
                    createMockCoupon(3L, "이벤트3", now.minusDays(1), now.plusDays(1))
            );
            int pageSize = 2;

            Member member = TestUtils.createEntity(Member.class, Map.of("id", 1L));
            Store store = TestUtils.createEntity(Store.class, Map.of(
                    "id", 1L,
                    "name", "매장",
                    "member", member
            ));

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(couponEventRepository.fetchCouponEventsByStoreAndStatus(any(Store.class), any(CouponEventStatus.class), any(StoreCouponEventsCursor.class), eq(pageSize + 1)))
                    .willReturn(mockedProjections);

            // when
            StoreCouponEventListResponse response = couponEventService.getCouponEventsByStore(member.getId(), store.getId(), CouponEventStatus.IN_PROGRESS, cursor, pageSize);

            // then
            assertThat(response).isNotNull();
            assertThat(response.storeId()).isEqualTo(store.getId());
            assertThat(response.storeName()).isEqualTo(store.getName());
            assertThat(response.events()).hasSizeLessThanOrEqualTo(2); // pageSize
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextCursor().lastEventId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("마지막 페이지 조회 - hasNext false")
        void getCouponEventsByStore_lastPage_hasNextFalse() {
            // given
            LocalDateTime lastStartAt = now.minusDays(1);
            LocalDateTime lastEndAt = now.plusDays(1);
            Long lastEventId = 3L;
            var cursor = StoreCouponEventsCursor.ofNullable(lastStartAt, lastEndAt, lastEventId);
            int pageSize = 3;

            List<CouponEventWithUsedCountProjection> mockedProjections = List.of(
                    createMockCoupon(1L, "이벤트1", now.minusDays(1), now.plusDays(1)),
                    createMockCoupon(2L, "이벤트2", now.minusDays(1), now.plusDays(1)),
                    createMockCoupon(lastEventId, "이벤트3", lastStartAt, lastEndAt)
            );

            Member member = TestUtils.createEntity(Member.class, Map.of("id", 1L));
            Store store = TestUtils.createEntity(Store.class, Map.of(
                    "id", 1L,
                    "name", "매장",
                    "member", member
            ));

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(couponEventRepository.fetchCouponEventsByStoreAndStatus(any(Store.class), any(CouponEventStatus.class), any(StoreCouponEventsCursor.class), eq(pageSize + 1)))
                    .willReturn(mockedProjections);

            // when
            StoreCouponEventListResponse response = couponEventService.getCouponEventsByStore(member.getId(), store.getId(), CouponEventStatus.IN_PROGRESS, cursor, pageSize);

            // then
            assertThat(response).isNotNull();
            assertThat(response.events()).hasSizeLessThanOrEqualTo(3); // pageSize
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("커서 이후 조회")
        void getCouponEventsByStore_nextCursorPage() {
            // given
            var cursor = StoreCouponEventsCursor.ofNullable(now.minusDays(1), now.plusDays(1), 2L);
            int pageSize = 2;

            List<CouponEventWithUsedCountProjection> mockedProjections = List.of(
                    createMockCoupon(3L, "이벤트3", now.minusDays(1), now.plusDays(1)),
                    createMockCoupon(4L, "이벤트4", now.minusDays(1), now.plusDays(1)),
                    createMockCoupon(5L, "이벤트5", now.minusDays(1), now.plusDays(1))
            );

            Member member = TestUtils.createEntity(Member.class, Map.of("id", 1L));
            Store store = TestUtils.createEntity(Store.class, Map.of(
                    "id", 1L,
                    "name", "매장",
                    "member", member
            ));

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(couponEventRepository.fetchCouponEventsByStoreAndStatus(any(Store.class), any(CouponEventStatus.class), any(StoreCouponEventsCursor.class), eq(pageSize + 1)))
                    .willReturn(mockedProjections);

            // when
            StoreCouponEventListResponse response = couponEventService.getCouponEventsByStore(
                    member.getId(), store.getId(), CouponEventStatus.IN_PROGRESS, cursor, pageSize
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.events()).hasSizeLessThanOrEqualTo(2); // pageSize
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextCursor().lastEventId()).isEqualTo(4L);
        }

    }
}