package com.sparta.couponpop.domain.location.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.redis.enums.RedisKeyProperties;
import com.sparta.couponpop.common.redis.service.RedisService;
import com.sparta.couponpop.domain.location.dto.cache.LocationCacheValue;
import com.sparta.couponpop.domain.location.dto.request.CreateLocationRequest;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    public void cacheLocation(CreateLocationRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        final LocalDateTime now = LocalDateTime.now();
        final RedisKeyProperties keyProperties = RedisKeyProperties.LOCATION_MEMBER_DEVICE;

        String locationKey = keyProperties.getKey().formatted(member.getId(), request.deviceIdentifier());
        LocationCacheValue locationCacheValue = LocationCacheValue.of(member.getId(), request.fcmToken(), request.deviceIdentifier(), request.latitude(), request.longitude(), now);

        redisService.setKey(locationKey, locationCacheValue, keyProperties.getExpiration());
    }

}
