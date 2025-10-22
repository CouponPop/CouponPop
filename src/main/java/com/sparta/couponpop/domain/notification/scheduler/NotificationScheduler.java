package com.sparta.couponpop.domain.notification.scheduler;

import com.sparta.couponpop.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";

    private final NotificationService notificationService;

    /**
     * 위치 기반 쿠폰 이벤트 알림을 정해진 시각에 전송한다.
     */
    @Scheduled(cron = "0 0 12,18 * * *", zone = TIME_ZONE)
    public void executeLocationBasedCouponEventNotification() {
        log.info("위치 기반 쿠폰 이벤트 알림 배치를 시작합니다.");
        notificationService.notifyLocationBasedCouponEvent();
    }
}
