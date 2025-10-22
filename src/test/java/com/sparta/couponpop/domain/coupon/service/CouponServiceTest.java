package com.sparta.couponpop.domain.coupon.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.dto.response.CouponDetailResponse;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
import com.sparta.couponpop.domain.coupon.repository.TemporaryCouponCodeRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Mock
    private TemporaryCouponCodeRepository temporaryCouponCodeRepository;

    @InjectMocks
    private CouponService couponService;

    private Member member;
    private Store store;
    private CouponEvent couponEvent;
    private Coupon coupon;

    private static final LocalDateTime issuedTime = LocalDateTime.of(2025, 10, 20, 16, 20);
    private static final LocalDateTime eventStartAt = issuedTime.minusHours(1);
    private static final LocalDateTime eventEndAt = issuedTime.plusDays(1);

    private static final LocalDateTime couponIssuedAt = eventStartAt.plusHours(4);

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
                "eventStartAt", eventStartAt,
                "eventEndAt", eventEndAt,
                "totalCount", 10,
                "couponEventStatus", CouponEventStatus.IN_PROGRESS,
                "store", store
        ));

        coupon = TestUtils.createEntity(Coupon.class, Map.of(
                "id", 1L,
                "couponCode", "CPN-9515BD7FE9CD",
                "receivedAt", couponIssuedAt,
                "expireAt", eventEndAt,
                "couponStatus", CouponStatus.AVAILABLE,
                "couponEvent", couponEvent,
                "member", member
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

    @Nested
    @DisplayName("쿠폰 정보 상세 조회 (getCouponDetail)")
    class GetCouponDetailTests {

        @Test
        @DisplayName("사용 가능한(Available) 쿠폰")
        void getCouponDetail_AvailableCoupon() {
            // given
            given(couponRepository.findByIdWithCouponEventAndStore(anyLong())).willReturn(Optional.of(coupon));

            // when
            CouponDetailResponse response = couponService.getCouponDetail(coupon.getId(), member.getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(CouponStatus.AVAILABLE);
            assertThat(response)
                    .extracting("id", "issuedAt", "expireAt")
                    .containsExactly(1L, couponIssuedAt, eventEndAt);
            assertThat(response.usedAt()).isNull();
            assertThat(response.qrCode()).isNotNull();

            verify(temporaryCouponCodeRepository, times(1))
                    .setTemporaryCoupon(anyLong(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("사용된(Used) 쿠폰")
        void getCouponDetail_UnavailableCoupon() {
            // given
            LocalDateTime usedAt = couponIssuedAt.plusHours(5);
            coupon = TestUtils.createEntity(Coupon.class, Map.of(
                    "id", 1L,
                    "receivedAt", couponIssuedAt,
                    "expireAt", eventEndAt,
                    "usedAt", usedAt,
                    "couponStatus", CouponStatus.USED,
                    "couponEvent", couponEvent,
                    "member", member
            ));

            given(couponRepository.findByIdWithCouponEventAndStore(anyLong())).willReturn(Optional.of(coupon));

            // when
            CouponDetailResponse response = couponService.getCouponDetail(coupon.getId(), member.getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(CouponStatus.USED);
            assertThat(response.usedAt()).isNotNull();
            assertThat(response.qrCode()).isNull();

            verify(temporaryCouponCodeRepository, times(0))
                    .setTemporaryCoupon(anyLong(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("쿠폰 상세 조회 실패 - 쿠폰 없음")
        void getCouponDetail_NotFound() {
            // given
            given(couponRepository.findByIdWithCouponEventAndStore(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.getCouponDetail(coupon.getId(), member.getId()))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰 상세 조회 실패 - 접근 권한 없음")
        void getCouponDetail_AccessDenied() {
            // given
            given(couponRepository.findByIdWithCouponEventAndStore(anyLong())).willReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponService.getCouponDetail(coupon.getId(), 999L))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ACCESS_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 (useCoupon)")
    class UseCouponTests {

        @Test
        @DisplayName("쿠폰 사용 - 성공")
        void useCoupon_Success() {
            // given
            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEventForUpdate(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventStartAt.plusHours(10);
            String qrCode = "QR123";

            // when
            couponService.useCoupon(coupon.getId(), qrCode, member.getId(), usedAt);

            // then
            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.USED);
            assertThat(coupon.getUsedAt()).isEqualTo(usedAt);

            verify(temporaryCouponCodeRepository, times(1))
                    .deleteTemporaryCoupon(anyLong(), anyString());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 임시 코드 없을 때 예외치")
        void useCoupon_InvalidQRCode() {
            // given
            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(false);

            LocalDateTime usedAt = eventStartAt.plusHours(10);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), qrCode, member.getId(), usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_INVALID_TEMP_CODE.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 이벤트 시작 전")
        void useCoupon_EventNotStarted() {
            // given
            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEventForUpdate(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventStartAt.minusMinutes(1);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), qrCode, member.getId(), usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_NOT_STARTED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 이벤트 종료 후")
        void useCoupon_EventEnded() {
            // given
            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEventForUpdate(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventEndAt.plusMinutes(1);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), qrCode, member.getId(), usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_ENDED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 쿠폰 소유자 불일치")
        void useCoupon_AccessDenied() {
            // given
            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEventForUpdate(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventStartAt.plusHours(10);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), qrCode, 999L, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ACCESS_DENIED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 쿠폰 이미 사용됨 (usedAt != null)")
        void useCoupon_AlreadyUsed1() {
            // given
            LocalDateTime usedAt = eventStartAt.plusHours(10);
            coupon = TestUtils.createEntity(Coupon.class, Map.of(
                    "id", 1L,
                    "couponCode", "CPN-9515BD7FE9CD",
                    "receivedAt", couponIssuedAt,
                    "expireAt", eventEndAt,
                    "usedAt", usedAt,
                    "couponStatus", CouponStatus.USED,
                    "couponEvent", couponEvent,
                    "member", member
            ));

            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEventForUpdate(anyLong())).willReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), "QR123", member.getId(), usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ALREADY_USED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 쿠폰 이미 사용됨 (isAvailable == false)")
        void useCoupon_AlreadyUsed2() {
            // given
            LocalDateTime usedAt = eventStartAt.plusHours(10);
            coupon = TestUtils.createEntity(Coupon.class, Map.of(
                    "id", 1L,
                    "couponCode", "CPN-9515BD7FE9CD",
                    "receivedAt", couponIssuedAt,
                    "expireAt", eventEndAt,
                    "couponStatus", CouponStatus.USED,
                    "couponEvent", couponEvent,
                    "member", member
            ));

            given(temporaryCouponCodeRepository.validateTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEventForUpdate(anyLong())).willReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), "QR123", member.getId(), usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_NOT_AVAILABLE.getMessage());
        }
    }
}
