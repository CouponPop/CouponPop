package com.sparta.couponpop.common.fcm.request;

// TODO: 테스트용 Request, 추후 삭제 예정
public record FcmRequest(
        Long memberId,
        String token,
        String title,
        String body
) {
}
