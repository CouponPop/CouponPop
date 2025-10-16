package com.sparta.couponpop.domain.couponevent.exception;

import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponEventErrorCode implements ErrorCode {

    EVENT_DURATION_EXCEEDED(HttpStatus.BAD_REQUEST, "쿠폰 이벤트는 최대 48시간까지 생성 가능합니다."),
    EVENT_END_BEFORE_START(HttpStatus.BAD_REQUEST, "이벤트 종료 시간은 시작 시간보다 이후여야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
