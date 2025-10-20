package com.sparta.couponpop.domain.notification.dto.request;

// TODO: 테스트용 Request, 추후 삭제 예정
public record FcmRequest(
        String token,
        String title,
        String body
) {
}
