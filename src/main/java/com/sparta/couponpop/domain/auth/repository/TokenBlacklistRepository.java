package com.sparta.couponpop.domain.auth.repository;

import java.time.format.DateTimeFormatter;

public interface TokenBlacklistRepository {

    DateTimeFormatter FORMATTER_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    void save(String token, long expirationMillis);

    boolean exists(String token);
}
