package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    // FCM 멀티캐스트 API는 한 번의 요청에 최대 500개의 토큰만 허용한다.
    private static final int FCM_MULTICAST_LIMIT = 500;

    public void sendNotification(String token,
                                 String title,
                                 String body) throws FirebaseMessagingException {
        sendNotification(Collections.singletonList(token), title, body);
    }

    public void sendNotification(List<String> tokens,
                                 String title,
                                 String body) throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            log.info("FCM 전송 토큰이 비어 있어 전송을 건너뜁니다.");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        WebpushNotification webpushNotification = WebpushNotification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // Android 설정
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setTtl(Duration.ofMinutes(5).toMillis())
                .putData("platform", "android")
                .build();

        // iOS 설정
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .putHeader("apns-push-type", "alert")
                .setAps(Aps.builder()
                        .setSound("default")
                        .putCustomData("platform", "ios")
                        .build())
                .build();

        // Web 설정
        WebpushConfig webpushConfig = WebpushConfig.builder()
                .setNotification(webpushNotification)
                .putData("platform", "web")
                .build();

        for (int start = 0; start < tokens.size(); start += FCM_MULTICAST_LIMIT) {
            int end = Math.min(start + FCM_MULTICAST_LIMIT, tokens.size());
            List<String> batch = tokens.subList(start, end);

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .setWebpushConfig(webpushConfig)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            log.info("[FCM 다건 전송 요청] tokens={}", batch.size());
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("[FCM 다건 전송 결과] 성공={} 실패={}", response.getSuccessCount(), response.getFailureCount());
        }
    }
}
