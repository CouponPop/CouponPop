package com.sparta.couponpop.domain.notification.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.notification.dto.command.CouponIssuedNotificationCommand;
import com.sparta.couponpop.domain.notification.dto.command.NotificationCommand;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.enums.NotificationType;
import com.sparta.couponpop.domain.notification.exception.NotificationErrorCode;
import com.sparta.couponpop.domain.notification.service.sender.NotificationSender;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class NotificationService {

    private final Map<NotificationType, NotificationSender<? extends NotificationCommand>> senderRegistry;

    public void notifyCustomerCouponIssued(Long memberId, @Valid CouponIssuedNotificationPayload payload) {
        CouponIssuedNotificationCommand command = CouponIssuedNotificationCommand.of(memberId, payload);
        execute(NotificationType.COUPON_ISSUED, command);
    }

    private <C extends NotificationCommand> void execute(NotificationType type, C command) {
        NotificationSender<C> sender = findSender(type);
        sender.send(command);
    }

    @SuppressWarnings("unchecked")
    private <C extends NotificationCommand> NotificationSender<C> findSender(NotificationType type) {
        NotificationSender<? extends NotificationCommand> sender = senderRegistry.get(type);
        if (sender == null) {
            log.error("등록되지 않은 알림 유형입니다. type={}, description={}", type, type.getDescription());
            throw new GlobalException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
        return (NotificationSender<C>) sender;
    }
}
