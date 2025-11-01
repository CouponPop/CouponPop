package com.sparta.couponpop.domain.notification.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.fcm.service.FcmSendService;
import com.sparta.couponpop.domain.fcmtoken.entity.FcmToken;
import com.sparta.couponpop.domain.fcmtoken.repository.FcmTokenRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.constants.NotificationTemplates;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.dto.payload.CouponUsedNotificationPayload;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private FcmSendService fcmSendService;

    @InjectMocks
    private NotificationService notificationService;

    private Member member;

    @Nested
    @DisplayName("쿠폰 사용한 손님에게 푸시 알림 전송")
    class SendCustomerCouponUsedNotification {

        @BeforeEach
        void setUp() {
            member = TestUtils.createEntity(Member.class, Map.of(
                    "id", 1L,
                    "email", "customer@test.com",
                    "username", "손님",
                    "password", "encrypted",
                    "phoneNumber", "01012345678",
                    "memberType", MemberType.CUSTOMER
            ));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void notifyCustomerCouponUsed_fail_memberNotFound() {
            // given
            CouponUsedNotificationPayload payload = CouponUsedNotificationPayload.of(
                    999L,
                    "오픈 기념 쿠폰",
                    "스타벅스 강남역점"
            );
            given(memberRepository.findById(payload.customerId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.send(payload))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            verifyNoInteractions(fcmTokenRepository, fcmSendService);
        }

        @Test
        @DisplayName("활성화된 토큰이 없으면 알림 전송을 건너뛴다")
        void notifyCustomerCouponUsed_skip_whenTokensEmpty() {
            // given
            CouponUsedNotificationPayload payload = CouponUsedNotificationPayload.of(1L, "오픈 기념 쿠폰", "스타벅스 강남역점");

            given(memberRepository.findById(payload.customerId())).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of());

            // when
            notificationService.send(payload);

            // then
            verifyNoInteractions(fcmSendService);
        }

        @Test
        @DisplayName("활성화된 토큰에 알림을 전송한다")
        void notifyCustomerCouponUsed_success_sendNotification() {
            // given
            FcmToken token1 = FcmToken.of(member, "token-1", "ANDROID", "device-1", true, LocalDateTime.now());
            FcmToken token2 = FcmToken.of(member, "token-2", "IOS", "device-2", true, LocalDateTime.now());
            FcmToken token3 = FcmToken.of(member, "token-3", "ANDROID", "device-3", true, LocalDateTime.now());
            FcmToken duplicateToken = FcmToken.of(member, "token-1", "ANDROID", "device-4", true, LocalDateTime.now()); // 중복 토큰

            CouponUsedNotificationPayload payload = CouponUsedNotificationPayload.of(1L, "오픈 기념 쿠폰", "스타벅스 강남역점");

            given(memberRepository.findById(payload.customerId())).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token1, token2, token3, duplicateToken));
            given(fcmSendService.sendNotification(anyLong(), anyString(), anyString(), anyString())).willReturn(CompletableFuture.completedFuture(null));

            String expectedTitle = NotificationTemplates.COUPON_USED_TITLE.formatted(payload.couponName());
            String expectedBody = NotificationTemplates.COUPON_USED_BODY.formatted(
                    payload.couponName(),
                    payload.storeName()
            );

            // when
            notificationService.send(payload);

            // then
            ArgumentCaptor<String> fcmTokenCaptor = ArgumentCaptor.forClass(String.class);

            then(fcmSendService).should(times(3)).sendNotification(
                    eq(member.getId()),
                    fcmTokenCaptor.capture(),
                    eq(expectedTitle),
                    eq(expectedBody)
            );

            assertThat(fcmTokenCaptor.getAllValues()).containsExactly(
                    token1.getFcmToken(),
                    token2.getFcmToken(),
                    token3.getFcmToken()
            );
        }
    }

    @Nested
    @DisplayName("손님 쿠폰 수령 시 사장님 푸시 알림 전송")
    class SendOwnerCouponIssued {

        @BeforeEach
        void setUp() {
            member = TestUtils.createEntity(Member.class, Map.of(
                    "id", 2L,
                    "email", "owner@test.com",
                    "username", "사장",
                    "password", "encrypted",
                    "phoneNumber", "01012345678",
                    "memberType", MemberType.OWNER
            ));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void notifyOwnerCouponIssued_fail_memberNotFound() {
            // given
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    999L,
                    "오픈 기념 쿠폰",
                    30,
                    15
            );
            given(memberRepository.findById(payload.ownerId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.send(payload))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            verifyNoInteractions(fcmTokenRepository, fcmSendService);
        }

        @Test
        @DisplayName("활성화된 토큰이 없으면 알림 전송을 건너뛴다")
        void notifyOwnerCouponIssued_skip_whenTokensEmpty() {
            // given
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(2L, "오픈 기념 쿠폰", 30, 15);

            given(memberRepository.findById(payload.ownerId())).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of());

            // when
            notificationService.send(payload);

            // then
            verifyNoInteractions(fcmSendService);
        }

        @Test
        @DisplayName("활성화된 토큰에 알림을 전송한다")
        void notifyOwnerCouponIssued_success_sendNotification() {
            // given
            FcmToken token1 = FcmToken.of(member, "token-1", "ANDROID", "device-1", true, LocalDateTime.now());
            FcmToken token2 = FcmToken.of(member, "token-2", "IOS", "device-2", true, LocalDateTime.now());
            FcmToken token3 = FcmToken.of(member, "token-3", "ANDROID", "device-3", true, LocalDateTime.now());
            FcmToken duplicateToken = FcmToken.of(member, "token-1", "ANDROID", "device-4", true, LocalDateTime.now()); // 중복 토큰

            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(2L, "오픈 기념 쿠폰", 30, 15);

            given(memberRepository.findById(payload.ownerId())).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token1, token2, token3, duplicateToken));
            given(fcmSendService.sendNotification(anyLong(), anyString(), anyString(), anyString())).willReturn(CompletableFuture.completedFuture(null));

            String expectedTitle = NotificationTemplates.COUPON_ISSUED_TITLE.formatted(payload.couponName());
            String expectedBody = NotificationTemplates.COUPON_ISSUED_BODY.formatted(
                    payload.couponName(),
                    payload.totalCount(),
                    payload.issuedCount()
            );

            // when
            notificationService.send(payload);

            // then
            ArgumentCaptor<String> fcmTokenCaptor = ArgumentCaptor.forClass(String.class);

            then(fcmSendService).should(times(3)).sendNotification(
                    eq(member.getId()),
                    fcmTokenCaptor.capture(),
                    eq(expectedTitle),
                    eq(expectedBody)
            );

            assertThat(fcmTokenCaptor.getAllValues()).containsExactly(
                    token1.getFcmToken(),
                    token2.getFcmToken(),
                    token3.getFcmToken()
            );
        }

        @Test
        @DisplayName("FCM 전송 중 예외가 발생해도 예외를 전파하지 않는다")
        void notifyOwnerCouponIssued_success_ignoreMessagingException() {
            // given
            FcmToken token = FcmToken.of(member, "token-1", "ANDROID", "device-1", true, LocalDateTime.now());
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(2L, "오픈 기념 쿠폰", 30, 15);

            given(memberRepository.findById(payload.ownerId())).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token));

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("전송 실패"));
            given(fcmSendService.sendNotification(anyLong(), anyString(), anyString(), anyString())).willReturn(failedFuture);

            // when & then
            assertThatCode(() -> notificationService.send(payload)).doesNotThrowAnyException();
        }
    }
}
