package com.sparta.couponpop.domain.store.exception;

import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."),
    STORE_NOT_FOUND(HttpStatus.BAD_REQUEST, "매장을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
