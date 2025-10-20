package com.sparta.couponpop.domain.notification.dto.command;

/**
 * 위치 기반 쿠폰 이벤트 알림 전송 시 필요한 데이터
 */
public record LocationBasedCouponEventNotificationCommand(

) implements NotificationCommand {

    public static LocationBasedCouponEventNotificationCommand of() {
        return new LocationBasedCouponEventNotificationCommand();
    }
}

