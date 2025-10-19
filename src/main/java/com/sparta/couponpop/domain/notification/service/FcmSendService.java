package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.sparta.couponpop.domain.notification.factory.FcmMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    // FCM 멀티캐스트 API는 한 번의 요청에 최대 500개의 토큰만 허용한다.
    private static final int FCM_MULTICAST_LIMIT = 500;

    private final FcmMessageFactory fcmMessageFactory;

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

        for (int start = 0; start < tokens.size(); start += FCM_MULTICAST_LIMIT) {
            int end = Math.min(start + FCM_MULTICAST_LIMIT, tokens.size());
            List<String> batch = tokens.subList(start, end);

            MulticastMessage message = fcmMessageFactory.createMulticastMessage(batch, title, body);

            log.info("[FCM 다건 전송 요청] tokens={}", batch.size());
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("[FCM 다건 전송 결과] 성공={} 실패={}", response.getSuccessCount(), response.getFailureCount());
        }
    }
}
