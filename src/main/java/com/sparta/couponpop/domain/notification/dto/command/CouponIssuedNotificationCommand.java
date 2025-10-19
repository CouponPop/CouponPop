package com.sparta.couponpop.domain.notification.dto.command;

import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;

/**
 * 쿠폰 수령 알림 전송 시 필요한 데이터
 *
 * @param memberId 회원 PK
 * @param payload  알림에 포함될 상세 정보
 */
public record CouponIssuedNotificationCommand(
        Long memberId,
        CouponIssuedNotificationPayload payload
) implements NotificationCommand {

    public static CouponIssuedNotificationCommand of(Long memberId, CouponIssuedNotificationPayload payload) {
        return new CouponIssuedNotificationCommand(memberId, payload);
    }
}

