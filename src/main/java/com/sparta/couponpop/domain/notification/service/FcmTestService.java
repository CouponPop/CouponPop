package com.sparta.couponpop.domain.notification.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.AndroidConfig;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.notification.dto.request.FcmMessageRequest;
import com.sparta.couponpop.domain.notification.dto.request.FcmRequest;
import com.sparta.couponpop.domain.notification.exception.NotificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTestService {

    private final ObjectMapper objectMapper;

    @Value("${fcm.firebase-config-path}")
    private String FCM_CONFIG_PATH;
    @Value("${fcm.api-url}")
    private String FCM_API_URL;
    @Value("${fcm.scope}")
    private String FCM_SCOPE;
    @Value("${fcm.ttl}")
    private String FCM_TTL;

    public int sendTestNotification(FcmRequest request) {
        try {
            FcmMessageRequest message = createFcmMessage(request);
            String body = objectMapper.writeValueAsString(message);
            log.debug("[FCM 전송 Body] {}", body);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> exchange = restTemplate.exchange(FCM_API_URL, HttpMethod.POST, entity, String.class);

            log.debug("[전송 결과] {}", exchange.getStatusCode());

            return exchange.getStatusCode() == HttpStatus.OK ? 1 : 0;
        } catch (IOException e) {
            log.debug("푸시 메세지 전송 실패", e);
            throw new GlobalException(NotificationErrorCode.FCM_SEND_FAIL);
        }
    }

    private FcmMessageRequest createFcmMessage(FcmRequest request) {
        FcmMessageRequest.Notification notification = FcmMessageRequest.Notification.builder()
                .title(request.title())
                .body(request.body())
                .build();

        FcmMessageRequest.Android android = FcmMessageRequest.Android.builder()
                .priority(AndroidConfig.Priority.HIGH)
                .ttl(FCM_TTL)
                .build();

        FcmMessageRequest.Message message = FcmMessageRequest.Message.builder()
                .notification(notification)
                .token(request.token())
                .android(android)
                .build();

        return FcmMessageRequest.builder()
                .message(message)
                .build();
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(FCM_CONFIG_PATH).getInputStream())
                .createScoped(List.of(FCM_SCOPE));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
