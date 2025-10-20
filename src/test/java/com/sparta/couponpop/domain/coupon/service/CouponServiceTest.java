package com.sparta.couponpop.domain.coupon.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Member member;
    private Store store;
    private CouponEvent couponEvent;
    private static final LocalDateTime issuedTime = LocalDateTime.of(2025, 10, 20, 16, 20);

    @BeforeEach
    void setUp() {
        member = TestUtils.createEntity(Member.class, Map.of("id", 1L));
        store = TestUtils.createEntity(Store.class, Map.of(
                "id", 1L,
                "member", member
        ));

        couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "id", 1L,
                "name", "이벤트 제목",
                "eventStartAt", issuedTime.minusHours(1),
                "eventEndAt", issuedTime.plusDays(1),
                "totalCount", 10,
                "couponEventStatus", CouponEventStatus.IN_PROGRESS,
                "store", store
        ));
    }

    @Nested
    @DisplayName("쿠폰 발급 (issueCoupon)")
    class IssueCouponTests {

        @Test
        @DisplayName("쿠폰 발급 성공")
        void issuedCoupon_success() {
            // given
            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
            given(couponRepository.existsByMemberIdAndCouponEventId(anyLong(), anyLong())).willReturn(false);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime);

            // then
            assertThat(couponEvent.getIssuedCount()).isEqualTo(1);
            then(couponRepository).should().save(any(Coupon.class));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 이벤트 없음")
        void issueCoupon_eventNotFound() {
            // given
            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 진행 중인 이벤트가 아닐 때")
        void issueCoupon_eventNotInProgress() {
            // given
            couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", 1L,
                    "name", "이벤트 제목",
                    "eventStartAt", issuedTime.minusHours(1),
                    "eventEndAt", issuedTime.plusDays(1),
                    "totalCount", 10,
                    "couponEventStatus", CouponEventStatus.SCHEDULED,
                    "store", store
            ));

            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));

            // when & then
            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS.getMessage());
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 이벤트 시간 벗어남 (이벤트 시간을 현재 시간보다 이전으로 설정)")
        void issueCoupon_eventNotInProgressTime() {
            // given
            couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", 1L,
                    "name", "이벤트 시간 벗어남",
                    "eventStartAt", issuedTime.minusHours(2),
                    "eventEndAt", issuedTime.minusHours(1),
                    "totalCount", 10,
                    "couponEventStatus", CouponEventStatus.IN_PROGRESS,
                    "store", store
            ));
            BDDMockito.given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            BDDMockito.given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));

            // when & then
            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS_TIME.getMessage());
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 쿠폰 모두 소진 (발급 수량과 총 수량 동일하게 설정)")
        void issueCoupon_eventCouponSoldOut() {
            // given
            couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", 1L,
                    "name", "이벤트 시간 벗어남",
                    "eventStartAt", issuedTime.minusHours(2),
                    "eventEndAt", issuedTime.plusDays(1),
                    "totalCount", 10,
                    "issuedCount", 10,
                    "couponEventStatus", CouponEventStatus.IN_PROGRESS,
                    "store", store
            ));

            BDDMockito.given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            BDDMockito.given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));

            // when & then
            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT.getMessage());
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 이미 발급된 쿠폰")
        void issueCoupon_alreadyIssued() {
            // given
            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
            given(couponRepository.existsByMemberIdAndCouponEventId(anyLong(), anyLong())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ALREADY_ISSUED.getMessage());
        }
    }
}
