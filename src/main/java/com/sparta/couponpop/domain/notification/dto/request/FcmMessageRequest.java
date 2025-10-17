package com.sparta.couponpop.domain.notification.dto.request;

import com.google.firebase.messaging.AndroidConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record FcmMessageRequest(
        FcmMessageRequest.Message message
) {

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Notification notification;
        private String token;
        private Android android;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Android {
        private AndroidConfig.Priority priority;
        private String ttl;
    }
}
