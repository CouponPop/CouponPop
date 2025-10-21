package com.sparta.couponpop.common.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.couponpop.common.exception.CommonErrorCode;
import com.sparta.couponpop.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 키-값을 저장
    public void setKey(String key, Object value, Duration expirationInSeconds) {
        redisTemplate.opsForValue().set(key, value, expirationInSeconds);
    }

    // 키가 존재하는지 확인
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 키 삭제
    public boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 주어진 prefix로 시작하는 모든 키들을 스캔하여 집합으로 반환
     *
     * @param prefix 키 접두사
     * @return 접두사로 시작하는 모든 키들의 집합
     */
    public Set<String> scanKeysByPrefix(String prefix) {
        final String pattern = prefix + "*";
        final int scanCount = 1_000;

        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> results = new LinkedHashSet<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(scanCount)
                    .build();

            RedisKeyCommands redisKeyCommands = connection.keyCommands();
            try (Cursor<byte[]> cursor = redisKeyCommands.scan(options)) {
                while (cursor.hasNext()) {
                    String key = new String(cursor.next(), StandardCharsets.UTF_8);
                    results.add(key);
                }
            } catch (Exception e) {
                throw new GlobalException(CommonErrorCode.REDIS_SCAN_FAILED);
            }

            return results;
        });
    }

    /**
     * 캐시 값들을 key 집합으로 조회하고 지정된 타입으로 변환하여 리스트로 반환
     *
     * @param keys 조회할 키들
     * @param type 조회된 캐시 값들을 변환할 타입
     * @param <T>  리스트로 반환할 캐시 값의 타입
     * @return 조회된 캐시 값들의 리스트
     */
    public <T> List<T> fetchCacheValues(Set<String> keys, Class<T> type) {
        if (keys.isEmpty()) {
            return List.of();
        }

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        log.info("Redis에서 캐시 값을 조회했습니다. keysCount={}, fetchedCount={}, values={}", keys.size(), values != null ? values.size() : 0, values);
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<T> result = new ArrayList<>();
        for (Object value : values) {
            if (value == null) {
                continue;
            }

            T convertedValue = objectMapper.convertValue(value, type);
            result.add(convertedValue);
        }

        return result;
    }

}
