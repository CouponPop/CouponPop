package com.sparta.couponpop.domain.notificationhistory.dto.payload;

import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryStatus;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryType;

public record NotificationHistoryPayload(
        Long memberId,
        NotificationHistoryType type,
        String title,
        String body,
        NotificationHistoryStatus status,
        String failureReason
) {

    public static NotificationHistoryPayload of(Long memberId, NotificationHistoryType type, String title, String body, NotificationHistoryStatus status, String failureReason) {
        return new NotificationHistoryPayload(memberId, type, title, body, status, failureReason);
    }
}
