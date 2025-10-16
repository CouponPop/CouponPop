package com.sparta.couponpop.domain.auth.exception;


import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    PASSWORDS_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인하세요"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 다시 로그인하세요."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "잘못된 이메일 또는 비밀번호입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
