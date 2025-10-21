package com.sparta.couponpop.domain.auth.scheduler;

import com.sparta.couponpop.domain.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistScheduler {

    private static final long ONE_HOUR_IN_MS = 60 * 60 * 1000; // 1시간
    private final TokenBlacklistService tokenBlacklistService;

    @Scheduled(fixedRate = ONE_HOUR_IN_MS) // 1시간
    public void cleanupExpiredTokens() {

        tokenBlacklistService.clearExpiredTokens();
        int remaining = tokenBlacklistService.countBlacklistedTokens();
        log.info("[TokenCleanupScheduler] 블랙리스트 정리 완료 | 남은 항목 수: {}", remaining);
    }
}