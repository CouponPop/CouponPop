package com.sparta.couponpop.domain.location.dto.cache;

import java.time.LocalDateTime;

public record LocationCacheValue(
        Long memberId,
        String fcmToken,
        String deviceIdentifier,
        Double latitude,
        Double longitude,
        LocalDateTime cachedAt
) {

    public static LocationCacheValue of(Long memberId, String fcmToken, String deviceIdentifier, Double latitude, Double longitude, LocalDateTime cachedAt) {
        return new LocationCacheValue(memberId, fcmToken, deviceIdentifier, latitude, longitude, cachedAt);
    }
}
