package com.sparta.couponpop.domain.auth.repository;

public interface TokenBlacklistRepository {

    void save(String token, long expirationMillis);

    boolean exists(String token);
}
