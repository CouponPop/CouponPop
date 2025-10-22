package com.sparta.couponpop.common.redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum RedisKeyProperties {

    LOCATION_MEMBER_DEVICE("fcm:v1:location:", "member:%d:device:%s", Duration.ofDays(1));

    private final String keyPrefix;
    private final String keyPattern;
    private final Duration expiration;

    public String getKey() {
        return keyPrefix + keyPattern;
    }
}
