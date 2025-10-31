package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.fcm.service.FcmSendService;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.constants.NotificationTemplates;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberFcmTokenRepository memberFcmTokenRepository;

    @Mock
    private FcmSendService fcmSendService;

    @InjectMocks
    private NotificationService notificationService;

    private Member member;

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

    @Nested
    @DisplayName("손님 쿠폰 수령 알림")
    class NotifyCustomerCouponIssued {

        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void notifyCustomerCouponIssued_fail_memberNotFound() {
            // given
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    999L,
                    "오픈 기념 쿠폰",
                    "CPN-20241025",
                    LocalDateTime.of(2024, 10, 25, 12, 0)
            );
            given(memberRepository.findById(payload.memberId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.notifyCustomerCouponIssued(payload))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            verifyNoInteractions(memberFcmTokenRepository, fcmSendService);
        }

        @Test
        @DisplayName("활성화된 토큰이 없으면 알림 전송을 건너뛴다")
        void notifyCustomerCouponIssued_skip_whenTokensEmpty() {
            // given
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    1L,
                    "오픈 기념 쿠폰",
                    "CPN-20241025",
                    LocalDateTime.of(2024, 10, 25, 12, 0)
            );

            given(memberRepository.findById(payload.memberId())).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of());

            // when
            notificationService.notifyCustomerCouponIssued(payload);

            // then
            verifyNoInteractions(fcmSendService);
        }

        @Test
        @DisplayName("활성화된 토큰에 알림을 전송한다")
        void notifyCustomerCouponIssued_success_sendNotification() throws FirebaseMessagingException {
            // given
            MemberFcmToken token1 = MemberFcmToken.of(member, "token-1", "ANDROID", "device-1", true, LocalDateTime.now());
            MemberFcmToken token2 = MemberFcmToken.of(member, "token-2", "IOS", "device-2", true, LocalDateTime.now());
            MemberFcmToken token3 = MemberFcmToken.of(member, "token-3", "ANDROID", "device-3", true, LocalDateTime.now());

            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    1L,
                    "오픈 기념 쿠폰",
                    "CPN-20241025",
                    LocalDateTime.of(2024, 10, 25, 12, 0)
            );

            given(memberRepository.findById(payload.memberId())).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token1, token2, token3));

            String expectedTitle = NotificationTemplates.COUPON_ISSUED_TITLE;
            String expectedBody = NotificationTemplates.COUPON_ISSUED_BODY.formatted(
                    payload.couponName(),
                    payload.couponCode(),
                    payload.expireAt()
            );

            // when
            notificationService.notifyCustomerCouponIssued(payload);

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
        void notifyCustomerCouponIssued_success_ignoreMessagingException() throws FirebaseMessagingException {
            // given
            MemberFcmToken token = MemberFcmToken.of(member, "token-1", "ANDROID", "device-1", true, LocalDateTime.now());
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    1L,
                    "오픈 기념 쿠폰",
                    "CPN-20241025",
                    LocalDateTime.of(2024, 10, 25, 12, 0)
            );

            given(memberRepository.findById(payload.memberId())).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token));

            doThrow(mock(FirebaseMessagingException.class))
                    .when(fcmSendService)
                    .sendNotification(anyLong(), anyString(), anyString(), anyString());

            // when & then
            assertThatCode(() -> notificationService.notifyCustomerCouponIssued(payload)).doesNotThrowAnyException();
        }
    }
}
