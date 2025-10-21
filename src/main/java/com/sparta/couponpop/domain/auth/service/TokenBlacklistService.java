package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.domain.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public void blacklistToken(String token, long expirationMillis) {

        tokenBlacklistRepository.save(token, expirationMillis);
    }

    public boolean isBlacklisted(String token) {

        return tokenBlacklistRepository.exists(token);
    }

    public void clearExpiredTokens() {

        long now = Instant.now().toEpochMilli();
        tokenBlacklistRepository.deleteAllExpired(now);
    }

    public int countBlacklistedTokens() {
        return tokenBlacklistRepository.count();
    }
}
