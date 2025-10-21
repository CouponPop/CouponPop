package com.sparta.couponpop.domain.location.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.redis.enums.RedisKeyProperties;
import com.sparta.couponpop.common.redis.service.RedisService;
import com.sparta.couponpop.domain.location.dto.cache.LocationCacheValue;
import com.sparta.couponpop.domain.location.dto.request.CreateLocationRequest;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisService redisService;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(memberRepository, redisService);
    }

    @Nested
    @DisplayName("회원 위치 캐시 저장")
    class CacheLocation {

        @Test
        @DisplayName("회원이 존재하면 위치 정보를 Redis에 저장한다")
        void cacheLocation_success_memberExists() {
            // given
            Long memberId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            CreateLocationRequest request = new CreateLocationRequest("sample-fcm-token", "device-123", 37.5665, 126.9780);
            LocalDateTime before = LocalDateTime.now();
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<LocationCacheValue> valueCaptor = ArgumentCaptor.forClass(LocationCacheValue.class);
            ArgumentCaptor<Duration> expirationCaptor = ArgumentCaptor.forClass(Duration.class);

            // when
            locationService.cacheLocation(request, memberId);
            LocalDateTime after = LocalDateTime.now();

            // then
            then(redisService).should().setKey(keyCaptor.capture(), valueCaptor.capture(), expirationCaptor.capture());
            String expectedKey = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKey().formatted(memberId, request.deviceIdentifier());
            assertThat(keyCaptor.getValue()).isEqualTo(expectedKey);
            LocationCacheValue cacheValue = valueCaptor.getValue();
            assertThat(cacheValue.memberId()).isEqualTo(memberId);
            assertThat(cacheValue.fcmToken()).isEqualTo(request.fcmToken());
            assertThat(cacheValue.deviceIdentifier()).isEqualTo(request.deviceIdentifier());
            assertThat(cacheValue.latitude()).isEqualTo(request.latitude());
            assertThat(cacheValue.longitude()).isEqualTo(request.longitude());
            assertThat(cacheValue.cachedAt()).isBetween(before, after);
            assertThat(expirationCaptor.getValue()).isEqualTo(RedisKeyProperties.LOCATION_MEMBER_DEVICE.getExpiration());
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void cacheLocation_fail_memberNotFound() {
            // given
            Long memberId = 99L;
            CreateLocationRequest request = new CreateLocationRequest("sample-fcm-token", "device-123", 37.5665, 126.9780);
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> locationService.cacheLocation(request, memberId))
                    .isInstanceOf(GlobalException.class)
                    .extracting("errorCode")
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            then(redisService).shouldHaveNoInteractions();
        }
    }
}
