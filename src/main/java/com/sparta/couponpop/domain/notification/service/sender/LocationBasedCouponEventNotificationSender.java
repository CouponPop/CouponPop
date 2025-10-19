package com.sparta.couponpop.domain.notification.service.sender;

import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.notification.dto.command.LocationBasedCouponEventNotificationCommand;
import com.sparta.couponpop.domain.notification.enums.NotificationType;
import com.sparta.couponpop.domain.notification.service.FcmSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationBasedCouponEventNotificationSender implements NotificationSender<LocationBasedCouponEventNotificationCommand> {

    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final FcmSendService fcmSendService;

    @Override
    public NotificationType getType() {
        return NotificationType.LOCATION_BASED_EVENT;
    }

    // TODO: 위치 기반 알림이니 Webpush는 불필요
    @Override
    public void send(LocationBasedCouponEventNotificationCommand command) {
        String NotificationTypeDescription = getType().getDescription();

    }
}

