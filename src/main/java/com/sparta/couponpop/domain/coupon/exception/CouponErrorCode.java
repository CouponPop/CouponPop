package com.sparta.couponpop.domain.coupon.exception;

import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 쿠폰에 접근할 수 없습니다."),

    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "해당 쿠폰은 이미 발급되었습니다."),
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않은 쿠폰입니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
