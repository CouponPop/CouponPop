package com.sparta.couponpop.common.dto.fcmtoken.request;

public record FcmTokenExpireRequest(
        String fcmToken
) {
    public static FcmTokenExpireRequest from(String fcmToken) {
        return new FcmTokenExpireRequest(fcmToken);
    }
}
