package com.sparta.couponpop.domain.notification.service.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.redis.enums.RedisKeyProperties;
import com.sparta.couponpop.common.redis.service.RedisService;
import com.sparta.couponpop.domain.location.dto.cache.LocationCacheValue;
import com.sparta.couponpop.domain.notification.constants.NotificationTemplates;
import com.sparta.couponpop.domain.notification.dto.command.LocationBasedCouponEventNotificationCommand;
import com.sparta.couponpop.domain.notification.service.FcmSendService;
import com.sparta.couponpop.domain.store.dto.response.StoreAndCouponEventCountProjection;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LocationBasedCouponEventNotificationSenderTest {

    @Mock
    private FcmSendService fcmSendService;

    @Mock
    private RedisService redisService;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private LocationBasedCouponEventNotificationSender sender;

    @Nested
    @DisplayName("위치 기반 쿠폰 이벤트 알림 전송")
    class Send {

        @Test
        @DisplayName("위치 기반 캐시 키가 없으면 전송을 건너뛴다")
        void send_skip_locationCacheKeyEmpty() {
            // given
            String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
            given(redisService.scanKeysByPrefix(keyPrefix)).willReturn(Set.of());

            // when
            sender.send(LocationBasedCouponEventNotificationCommand.of());

            // then
            then(redisService).should().scanKeysByPrefix(keyPrefix);
            then(redisService).should(never()).fetchCacheValues(anySet(), eq(LocationCacheValue.class));
            then(storeRepository).shouldHaveNoInteractions();
            then(fcmSendService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("캐시 값이 비어 있으면 전송을 건너뛴다")
        void send_skip_locationCacheValueEmpty() {
            // given
            String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
            Set<String> locationKeys = Set.of("fcm:v1:location:member:1:device:abc");
            given(redisService.scanKeysByPrefix(keyPrefix)).willReturn(locationKeys);
            given(redisService.fetchCacheValues(locationKeys, LocationCacheValue.class)).willReturn(List.of());

            // when
            sender.send(LocationBasedCouponEventNotificationCommand.of());

            // then
            then(redisService).should().scanKeysByPrefix(keyPrefix);
            then(redisService).should().fetchCacheValues(locationKeys, LocationCacheValue.class);
            then(storeRepository).shouldHaveNoInteractions();
            then(fcmSendService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("근처 매장이 없으면 전송을 건너뛴다")
        void send_skip_noOpenStore() throws FirebaseMessagingException {
            // given
            String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
            Set<String> locationKeys = Set.of("fcm:v1:location:member:2:device:def");
            LocationCacheValue cacheValue = LocationCacheValue.of(
                    1L,
                    "token-no-store",
                    "device-def",
                    37.5665,
                    126.9780,
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            given(redisService.scanKeysByPrefix(keyPrefix)).willReturn(locationKeys);
            given(redisService.fetchCacheValues(locationKeys, LocationCacheValue.class)).willReturn(List.of(cacheValue));
            given(storeRepository.findNearbyOpenStoreAndEventCount(
                    eq(cacheValue.latitude()),
                    eq(cacheValue.longitude()),
                    eq(1.0),
                    any(LocalDateTime.class)
            )).willReturn(projection(0L, 3L));

            // when
            sender.send(LocationBasedCouponEventNotificationCommand.of());

            // then
            then(storeRepository).should().findNearbyOpenStoreAndEventCount(
                    eq(cacheValue.latitude()),
                    eq(cacheValue.longitude()),
                    eq(1.0),
                    any(LocalDateTime.class)
            );
            then(fcmSendService).should(never()).sendNotification(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("활성 쿠폰 이벤트가 없으면 전송을 건너뛴다")
        void send_skip_noActiveCouponEvent() throws FirebaseMessagingException {
            // given
            String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
            Set<String> locationKeys = Set.of("fcm:v1:location:member:3:device:ghi");
            LocationCacheValue cacheValue = LocationCacheValue.of(
                    2L,
                    "token-no-event",
                    "device-ghi",
                    35.1796,
                    129.0756,
                    LocalDateTime.of(2024, 2, 1, 9, 0)
            );
            given(redisService.scanKeysByPrefix(keyPrefix)).willReturn(locationKeys);
            given(redisService.fetchCacheValues(locationKeys, LocationCacheValue.class)).willReturn(List.of(cacheValue));
            given(storeRepository.findNearbyOpenStoreAndEventCount(
                    eq(cacheValue.latitude()),
                    eq(cacheValue.longitude()),
                    eq(1.0),
                    any(LocalDateTime.class)
            )).willReturn(projection(5L, 0L));

            // when
            sender.send(LocationBasedCouponEventNotificationCommand.of());

            // then
            then(storeRepository).should().findNearbyOpenStoreAndEventCount(
                    eq(cacheValue.latitude()),
                    eq(cacheValue.longitude()),
                    eq(1.0),
                    any(LocalDateTime.class)
            );
            then(fcmSendService).should(never()).sendNotification(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("조건이 충족되면 FCM 알림을 전송한다")
        void send_success_sendNotification() throws FirebaseMessagingException {
            // given
            String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
            Set<String> locationKeys = Set.of("fcm:v1:location:member:4:device:jkl");
            LocationCacheValue cacheValue = LocationCacheValue.of(
                    3L,
                    "token-success",
                    "device-jkl",
                    37.1234,
                    127.5678,
                    LocalDateTime.of(2024, 3, 1, 15, 30)
            );
            long openStoreCount = 4L;
            long activeEventCount = 6L;
            given(redisService.scanKeysByPrefix(keyPrefix)).willReturn(locationKeys);
            given(redisService.fetchCacheValues(locationKeys, LocationCacheValue.class)).willReturn(List.of(cacheValue));
            given(storeRepository.findNearbyOpenStoreAndEventCount(
                    eq(cacheValue.latitude()),
                    eq(cacheValue.longitude()),
                    eq(1.0),
                    any(LocalDateTime.class)
            )).willReturn(projection(openStoreCount, activeEventCount));

            String expectedTitle = NotificationTemplates.LOCATION_BASED_COUPON_EVENT_TITLE.formatted(openStoreCount);
            String expectedBody = NotificationTemplates.LOCATION_BASED_COUPON_EVENT_BODY.formatted(openStoreCount, activeEventCount);

            // when
            sender.send(LocationBasedCouponEventNotificationCommand.of());

            // then
            then(fcmSendService).should().sendNotification(cacheValue.fcmToken(), expectedTitle, expectedBody);
        }

        @Test
        @DisplayName("FCM 전송 중 예외가 발생해도 예외를 전파하지 않는다")
        void send_ignoreException_fcmSendFails() throws FirebaseMessagingException {
            // given
            String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
            Set<String> locationKeys = Set.of("fcm:v1:location:member:5:device:mno");
            LocationCacheValue cacheValue = LocationCacheValue.of(
                    4L,
                    "token-fail",
                    "device-mno",
                    33.4996,
                    126.5312,
                    LocalDateTime.of(2024, 4, 1, 8, 0)
            );
            given(redisService.scanKeysByPrefix(keyPrefix)).willReturn(locationKeys);
            given(redisService.fetchCacheValues(locationKeys, LocationCacheValue.class)).willReturn(List.of(cacheValue));
            given(storeRepository.findNearbyOpenStoreAndEventCount(
                    eq(cacheValue.latitude()),
                    eq(cacheValue.longitude()),
                    eq(1.0),
                    any(LocalDateTime.class)
            )).willReturn(projection(2L, 1L));

            willThrow(FirebaseMessagingException.class)
                    .given(fcmSendService)
                    .sendNotification(eq(cacheValue.fcmToken()), anyString(), anyString());

            // when & then
            assertThatCode(() -> sender.send(LocationBasedCouponEventNotificationCommand.of()))
                    .doesNotThrowAnyException();
        }

        private StoreAndCouponEventCountProjection projection(long openStoreCount, long activeCouponEventCount) {
            return new StoreAndCouponEventCountProjection() {
                @Override
                public Long getOpenStoreCount() {
                    return openStoreCount;
                }

                @Override
                public Long getActiveCouponEventCount() {
                    return activeCouponEventCount;
                }
            };
        }
    }
}
