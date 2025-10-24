package com.sparta.couponpop.domain.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 유형 ENUM
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    COUPON_ISSUED("쿠폰 수령 알림");

    private final String description;
}

