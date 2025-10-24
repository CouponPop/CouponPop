package com.sparta.couponpop.domain.notification.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.notification.dto.command.CouponIssuedNotificationCommand;
import com.sparta.couponpop.domain.notification.dto.command.NotificationCommand;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.enums.NotificationType;
import com.sparta.couponpop.domain.notification.exception.NotificationErrorCode;
import com.sparta.couponpop.domain.notification.service.sender.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationSender<CouponIssuedNotificationCommand> couponIssuedNotificationSender;

    @Nested
    @DisplayName("손님 쿠폰 수령 알림")
    class NotifyCustomerCouponIssued {

        private Map<NotificationType, NotificationSender<? extends NotificationCommand>> registry;
        private NotificationService notificationService;

        @BeforeEach
        void setUp() {
            registry = new EnumMap<>(NotificationType.class);
            notificationService = new NotificationService(registry);
        }

        @Test
        @DisplayName("쿠폰 수령 알림 전송 시 등록된 Sender로 위임한다")
        void notifyCustomerCouponIssued_success_delegateToRegisteredSender() {
            // given
            Long memberId = 1L;
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    "아메리카노 1+1",
                    "Coupon-123",
                    LocalDateTime.of(2024, 7, 1, 12, 30)
            );
            registry.put(NotificationType.COUPON_ISSUED, couponIssuedNotificationSender);
            ArgumentCaptor<CouponIssuedNotificationCommand> commandCaptor = ArgumentCaptor.forClass(CouponIssuedNotificationCommand.class);

            // when
            notificationService.notifyCustomerCouponIssued(memberId, payload);

            // then
            then(couponIssuedNotificationSender).should().send(commandCaptor.capture());
            CouponIssuedNotificationCommand command = commandCaptor.getValue();
            assertThat(command.memberId()).isEqualTo(memberId);
            assertThat(command.payload()).isEqualTo(payload);
        }

        @Test
        @DisplayName("등록되지 않은 알림 유형이면 예외를 던진다")
        void notifyCustomerCouponIssued_fail_senderNotFound() {
            // given
            CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                    "아메리카노 1+1",
                    "Coupon-123",
                    LocalDateTime.of(2024, 7, 1, 12, 30)
            );

            // when & then
            assertThatThrownBy(() -> notificationService.notifyCustomerCouponIssued(1L, payload))
                    .isInstanceOf(GlobalException.class)
                    .extracting("errorCode")
                    .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }
}
