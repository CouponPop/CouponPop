package com.sparta.couponpop.domain.auth.exception;


import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 비밀번호와 비밀번호 확인 불일치
    PASSWORDS_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
