package com.sparta.couponpop.domain.notification.exception;

import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    FCM_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 푸시 알림 전송에 실패했습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 알림 유형입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
