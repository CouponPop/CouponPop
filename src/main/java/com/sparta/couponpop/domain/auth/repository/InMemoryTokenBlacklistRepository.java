package com.sparta.couponpop.domain.auth.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class InMemoryTokenBlacklistRepository implements TokenBlacklistRepository {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void save(String token, long expirationMillis) {

        blacklist.put(token, expirationMillis);
        log.info("[Blacklist Repository] Blacklist 추가 | token: {}, 잔여 시간: {}", token, expirationMillis);
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

    @Scheduled(fixedRate = 60 * 60 * 1000) // 1시간마다 실행
    public void cleanupExpiredTokens() {

        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
        log.info("[Blacklist Repository] Blacklist 정리 완료 | 남은 개수: {}", blacklist.size());
    }
}
