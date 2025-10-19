package com.sparta.couponpop.domain.notification.factory;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class FcmMessageFactory {

    public MulticastMessage createMulticastMessage(List<String> tokens, String title, String body) {
        Notification notification = createNotification(title, body);
        AndroidConfig androidConfig = createAndroidConfig();
        ApnsConfig apnsConfig = createApnsConfig();
        WebpushConfig webpushConfig = createWebpushConfig(title, body);

        return MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .setAndroidConfig(androidConfig)
                .setApnsConfig(apnsConfig)
                .setWebpushConfig(webpushConfig)
                .putData("title", title)
                .putData("body", body)
                .build();
    }

    private Notification createNotification(String title, String body) {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }

    private WebpushNotification createWebpushNotification(String title, String body) {
        return WebpushNotification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }

    private AndroidConfig createAndroidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setTtl(Duration.ofMinutes(5).toMillis())
                .putData("platform", "android")
                .build();
    }

    private ApnsConfig createApnsConfig() {
        return ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .putHeader("apns-push-type", "alert")
                .setAps(Aps.builder()
                        .setSound("default")
                        .putCustomData("platform", "ios")
                        .build())
                .build();
    }

    private WebpushConfig createWebpushConfig(String title, String body) {
        WebpushNotification webpushNotification = createWebpushNotification(title, body);

        return WebpushConfig.builder()
                .setNotification(webpushNotification)
                .putData("platform", "web")
                .build();
    }

}
