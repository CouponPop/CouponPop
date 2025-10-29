package com.sparta.couponpop.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static com.sparta.couponpop.domain.auth.constants.AuthTemplates.FORMATTER_YYYY_MM_DD_HH_MM_SS;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class RedisTokenBlacklistRepository implements TokenBlacklistRepository {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    private static String tokenPreview(String token) {
        return token.substring(0, Math.min(10, token.length())) + "...";
    }

    @Override
    public void save(String token, long expirationMillis) {

        String key = BLACKLIST_PREFIX + token;
        long remainingMillis = expirationMillis - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            log.debug("[Redis Blacklist Repository] 이미 만료된 토큰 블랙리스트 추가 시도 | token: {}", tokenPreview(token));
            return;
        }

        redisTemplate.opsForValue().set(key, String.valueOf(expirationMillis), remainingMillis, TimeUnit.MILLISECONDS);

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(expirationMillis), ZoneId.of("Asia/Seoul"));
        log.debug("[Redis Blacklist Repository] 토큰 블랙리스트 추가 | token: {}, 만료 시각: {}, TTL: {}ms",
                tokenPreview(token),
                dateTime.format(FORMATTER_YYYY_MM_DD_HH_MM_SS),
                remainingMillis);
    }

    @Override
    public boolean exists(String token) {

        String key = BLACKLIST_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }

        // 만료된 토큰은 TTL로 자동 삭제되나 수동으로 검증도 수행
        long expirationTime = Long.parseLong(value);
        boolean isExpired = expirationTime < System.currentTimeMillis();

        if (isExpired) {
            redisTemplate.delete(key);
            log.debug("[Redis Blacklist Repository] 만료된 토큰 삭제: {}", tokenPreview(token));
        }

        return !isExpired;
    }
}

