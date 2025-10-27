package com.sparta.couponpop.domain.auth.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class InMemoryTokenBlacklistRepository implements TokenBlacklistRepository {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void save(String token, long expirationMillis) {

        blacklist.put(token, expirationMillis);

        Instant instant = Instant.ofEpochMilli(expirationMillis);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("[Blacklist Repository] Blacklist 추가 | token: {}, 만료 시각: {}", token, formatted);
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

    @Override
    public void deleteAllExpired(long now) {

        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    @Override
    public int count() {

        return blacklist.size();
    }
}
