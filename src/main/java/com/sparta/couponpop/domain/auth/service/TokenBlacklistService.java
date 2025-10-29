package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.domain.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
