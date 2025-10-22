package com.sparta.couponpop.domain.notification.service.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.redis.enums.RedisKeyProperties;
import com.sparta.couponpop.common.redis.service.RedisService;
import com.sparta.couponpop.domain.location.dto.cache.LocationCacheValue;
import com.sparta.couponpop.domain.notification.constants.NotificationTemplates;
import com.sparta.couponpop.domain.notification.dto.command.LocationBasedCouponEventNotificationCommand;
import com.sparta.couponpop.domain.notification.enums.NotificationType;
import com.sparta.couponpop.domain.notification.service.FcmSendService;
import com.sparta.couponpop.domain.store.dto.response.StoreAndCouponEventCountProjection;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationBasedCouponEventNotificationSender implements NotificationSender<LocationBasedCouponEventNotificationCommand> {

    // 조회 반경
    private static final double SEARCH_RADIUS_KM = 1.0;

    private final FcmSendService fcmSendService;
    private final RedisService redisService;
    private final StoreRepository storeRepository;

    @Override
    public NotificationType getType() {
        return NotificationType.LOCATION_BASED_EVENT;
    }

    @Override
    public void send(LocationBasedCouponEventNotificationCommand command) {
        String notificationTypeDescription = getType().getDescription();
        LocalDateTime now = LocalDateTime.now();

        // TODO: https://github.com/CouponPop/coupon-pop-api/pull/91#discussion_r2450300987
        // 1. prefix로 위치 기반 캐시 키 조회
        String keyPrefix = RedisKeyProperties.LOCATION_MEMBER_DEVICE.getKeyPrefix();
        Set<String> locationKeys = redisService.scanKeysByPrefix(keyPrefix);
        if (locationKeys.isEmpty()) {
            log.info("위치 기반 캐시 키가 존재하지 않아 알림 전송을 건너뜁니다. keyPrefix={}", keyPrefix);
            return;
        }

        log.info("위치 기반 캐시 키를 조회했습니다. keyPrefix={}, keys={}, count={}", keyPrefix, locationKeys, locationKeys.size());

        // 2. 위치 기반 캐시 값 조회
        List<LocationCacheValue> locationCacheValues = redisService.fetchCacheValues(locationKeys, LocationCacheValue.class);
        if (locationCacheValues.isEmpty()) {
            log.info("위치 기반 캐시 데이터가 비어 있어 알림 전송을 건너뜁니다. keyPrefix={}", keyPrefix);
            return;
        }

        // 3. 캐시 값들을 순회
        for (LocationCacheValue locationCacheValue : locationCacheValues) {
            /*
             * TODO
             * - 비용이 큰 쿼리문을 for문으로 순회하며 실행중
             * - M x N 문제 발생 예상하여 추후 개선 필요
             */
            // 4. 근처 매장수와 쿠폰이벤트수를 조회
            StoreAndCouponEventCountProjection countProjection = storeRepository.findNearbyOpenStoreAndEventCount(
                    locationCacheValue.latitude(),
                    locationCacheValue.longitude(),
                    SEARCH_RADIUS_KM,
                    now
            );

            Long openStoreCount = countProjection.getOpenStoreCount();
            Long activeCouponEventCount = countProjection.getActiveCouponEventCount();
            log.info("근처 매장 및 쿠폰 이벤트 수를 조회했습니다. openStoreCount={}, activeCouponEventCount={}",
                    openStoreCount, activeCouponEventCount);

            if (openStoreCount == 0) {
                log.info("근처에 오픈된 매장이 없어 알림 전송을 건너뜁니다. locationCacheValue={}", locationCacheValue);
                continue;
            }

            if (activeCouponEventCount == 0) {
                log.info("근처 매장에 활성화된 쿠폰 이벤트가 없어 알림 전송을 건너뜁니다. locationCacheValue={}", locationCacheValue);
                continue;
            }

            // 5. FCM 토큰으로 푸시 알림 전송
            String token = locationCacheValue.fcmToken();

            String title = NotificationTemplates.LOCATION_BASED_COUPON_EVENT_TITLE.formatted(openStoreCount);
            String body = NotificationTemplates.LOCATION_BASED_COUPON_EVENT_BODY.formatted(openStoreCount, activeCouponEventCount);

            try {
                fcmSendService.sendNotification(token, title, body);
            } catch (FirebaseMessagingException e) {
                log.error("{} 전송 중 오류가 발생했습니다. token={}, message={}", notificationTypeDescription, token, e.getMessage(), e);
            }
        }

        // 6. Key들을 삭제한다.
        long deletedCount = redisService.deleteKeys(locationKeys);
        log.info("위치 기반 캐시 키들을 삭제했습니다. keyPrefix={}, deletedCount={}", keyPrefix, deletedCount);
    }
}
