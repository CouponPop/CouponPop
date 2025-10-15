package com.sparta.couponpop.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    SAMPLE_ERROR_CODE(HttpStatus.BAD_REQUEST, "테스트 에러 코드");

    private final HttpStatus httpStatus;
    private final String message;
}
