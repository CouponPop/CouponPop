package com.sparta.couponpop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer()); // 문자열 원본 형태로 저장
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 객체 -> JSON 형태로 저장
        redisTemplate.setHashKeySerializer(new StringRedisSerializer()); // 문자열 원본 형태로 저장
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // 객체 -> JSON 형태로 저장

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
