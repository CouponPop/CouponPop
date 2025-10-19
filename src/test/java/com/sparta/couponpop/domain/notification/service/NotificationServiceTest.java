package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberFcmTokenRepository memberFcmTokenRepository;

    @Mock
    private FcmSendService fcmSendService;

    @Nested
    @DisplayName("손님 쿠폰 수령 알림 전송")
    class NotifyCustomerCouponIssued {
        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void notifyCustomerCouponIssued_fail_memberNotFound() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    "아메리카노 1+1",
                    "Coupon-123",
                    LocalDateTime.of(2024, 3, 1, 10, 0)
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.notifyCustomerCouponIssued(memberId, payload))
                    .isInstanceOf(GlobalException.class)
                    .extracting("errorCode")
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            then(memberFcmTokenRepository).should(never()).findByMemberAndNotificationEnabledIsTrue(any(Member.class));
            then(fcmSendService).should(never()).sendNotification(anyList(), anyString(), anyString());
        }

        @Test
        @DisplayName("푸시 알림이 비활성화되면 FCM 전송을 시도하지 않는다")
        void notifyCustomerCouponIssued_skipSend_notificationsDisabled() throws FirebaseMessagingException {
            // given
            Long memberId = 2L;
            Member member = Member.signUp(
                    "customer@test.com",
                    "손님",
                    "hashedPassword",
                    "010-0000-0000",
                    MemberType.CUSTOMER
            );
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    "아메리카노 1+1",
                    "Coupon-123",
                    LocalDateTime.of(2024, 7, 1, 12, 30)
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of());

            // when
            notificationService.notifyCustomerCouponIssued(memberId, payload);

            // then
            then(memberFcmTokenRepository).should().findByMemberAndNotificationEnabledIsTrue(member);
            then(fcmSendService).should(never()).sendNotification(anyList(), anyString(), anyString());
        }

        @Test
        @DisplayName("알림이 활성화된 모든 기기에 FCM을 전송한다")
        void notifyCustomerCouponIssued_success_notificationsEnabled() throws FirebaseMessagingException {
            // given
            Long memberId = 3L;
            Member member = Member.signUp(
                    "active@test.com",
                    "활성회원",
                    "hashedPassword",
                    "010-1111-2222",
                    MemberType.CUSTOMER
            );
            MemberFcmToken token1 = MemberFcmToken.of(
                    member,
                    "token-A",
                    "android",
                    "device-1",
                    true,
                    LocalDateTime.now()
            );
            MemberFcmToken token2 = MemberFcmToken.of(
                    member,
                    "token-B",
                    "ios",
                    "device-2",
                    true,
                    LocalDateTime.now()
            );
            LocalDateTime expireAt = LocalDateTime.of(2024, 9, 10, 18, 45);
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    "아메리카노 1+1",
                    "Coupon-123",
                    expireAt
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token1, token2));

            // when
            notificationService.notifyCustomerCouponIssued(memberId, payload);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<String>> tokensCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            then(fcmSendService).should().sendNotification(
                    tokensCaptor.capture(),
                    titleCaptor.capture(),
                    bodyCaptor.capture()
            );

            String expectedBody = """
                    쿠폰명: %s
                    쿠폰코드: %s
                    만료기간: %s
                    """.formatted(
                    payload.couponName(),
                    payload.couponCode(),
                    expireAt
            );

            assertThat(tokensCaptor.getValue()).containsExactly("token-A", "token-B");
            assertThat(titleCaptor.getValue()).isEqualTo("쿠폰 수령이 완료되었습니다!");
            assertThat(bodyCaptor.getValue()).isEqualTo(expectedBody);
        }

        @Test
        @DisplayName("FCM 전송에 실패해도 예외를 전파하지 않는다")
        void notifyCustomerCouponIssued_ignoreException_fcmSendFails() throws FirebaseMessagingException {
            // given
            Long memberId = 4L;
            Member member = Member.signUp(
                    "failure@test.com",
                    "실패회원",
                    "hashedPassword",
                    "010-3333-4444",
                    MemberType.CUSTOMER
            );
            MemberFcmToken token = MemberFcmToken.of(
                    member,
                    "token-FAIL",
                    "web",
                    "device-3",
                    true,
                    LocalDateTime.now()
            );
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    "아메리카노 1+1",
                    "Coupon-123",
                    LocalDateTime.of(2024, 10, 5, 20, 0)
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member))
                    .willReturn(List.of(token));

            FirebaseMessagingException messagingException = mock(FirebaseMessagingException.class);
            willThrow(messagingException).given(fcmSendService)
                    .sendNotification(anyList(), anyString(), anyString());

            // when & then
            assertThatCode(() -> notificationService.notifyCustomerCouponIssued(memberId, payload))
                    .doesNotThrowAnyException();

            then(fcmSendService).should().sendNotification(anyList(), eq("쿠폰 수령이 완료되었습니다!"), anyString());
        }
    }

}
