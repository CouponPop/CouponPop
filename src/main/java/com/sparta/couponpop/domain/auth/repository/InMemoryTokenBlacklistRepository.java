package com.sparta.couponpop.domain.auth.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@Profile("test") // 테스트 환경에서만 사용
public class InMemoryTokenBlacklistRepository implements TokenBlacklistRepository {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void save(String token, long expirationMillis) {

        blacklist.put(token, expirationMillis);

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(expirationMillis), ZoneId.of("Asia/Seoul"));
        log.info("[Blacklist Repository] Blacklist 추가 | token: {}, 만료 시각: {}", token, dateTime.format(FORMATTER_YYYY_MM_DD_HH_MM_SS));
    }

    @Override
    public boolean exists(String token) {

        Long expirationTime = blacklist.get(token);

        if (expirationTime == null) {
            return false;
        }

        if (expirationTime < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }

    public void deleteAllExpired(long now) {

        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    public int count() {

        return blacklist.size();
    }
}
