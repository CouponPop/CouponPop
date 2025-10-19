package com.sparta.couponpop.domain.member.exception;

import com.sparta.couponpop.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 사용중인 이메일입니다."),
    MEMBER_FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 FCM 토큰을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
