package com.sparta.couponpop.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private static RedisTemplate<String, Object> getStringObjectRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                                              StringRedisSerializer stringRedisSerializer,
                                                                              GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(stringRedisSerializer); // 문자열 원본 형태로 저장
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer); // 객체 -> JSON 형태로 저장
        redisTemplate.setHashKeySerializer(stringRedisSerializer); // 문자열 원본 형태로 저장
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer); // 객체 -> JSON 형태로 저장

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        return getStringObjectRedisTemplate(redisConnectionFactory, stringRedisSerializer, genericJackson2JsonRedisSerializer);
    }

}
