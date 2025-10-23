package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.*;
import com.sparta.couponpop.domain.notification.factory.FcmMessageFactory;
import com.sparta.couponpop.domain.notificationhistory.dto.payload.NotificationHistoryPayload;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryStatus;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryType;
import com.sparta.couponpop.domain.notificationhistory.service.NotificationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    // FCM 멀티캐스트 API는 한 번의 요청에 최대 500개의 토큰만 허용한다.
    private static final int FCM_MULTICAST_LIMIT = 500;
    private static final NotificationHistoryType NOTIFICATION_HISTORY_TYPE = NotificationHistoryType.FCM;

    private final FcmMessageFactory fcmMessageFactory;
    private final NotificationHistoryService notificationHistoryService;

    // TODO: 발송된 토큰의 lastUsedAt 업데이트 로직 추가 예정
    public void sendNotification(Long memberId, String token, String title, String body) throws FirebaseMessagingException {
        if (!StringUtils.hasText(token)) {
            log.info("FCM 전송 토큰이 유효하지 않아 전송을 건너뜁니다.");
            return;
        }

        NotificationHistoryStatus status = NotificationHistoryStatus.SUCCESS;
        String failureReason = null;

        Message message = fcmMessageFactory.createMessage(token, title, body);
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 중 오류 발생: {}", e.getMessage());

            status = NotificationHistoryStatus.FAILURE;
            failureReason = e.getMessage();

            throw e; // 예외 재던지기
        } finally {
            NotificationHistoryPayload notificationHistoryPayload = NotificationHistoryPayload.of(memberId, NOTIFICATION_HISTORY_TYPE, title, body, status, failureReason);
            notificationHistoryService.createNotificationHistory(notificationHistoryPayload);
        }

        // TODO: 성공 토큰 lastUsedAt 업데이트
        // TODO: 실패 토큰 삭제 처리
    }

    // TODO: 발송된 토큰의 lastUsedAt 업데이트 로직 추가 예정
    public void sendNotification(Long memberId, List<String> tokens, String title, String body) throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            log.info("FCM 전송 토큰이 비어 있어 전송을 건너뜁니다.");
            return;
        }

        List<NotificationHistoryPayload> historyPayloads = new ArrayList<>();
        List<List<SendResponse>> allResponses = new ArrayList<>();

        for (int start = 0; start < tokens.size(); start += FCM_MULTICAST_LIMIT) {
            int end = Math.min(start + FCM_MULTICAST_LIMIT, tokens.size());
            List<String> batch = tokens.subList(start, end);

            MulticastMessage message = fcmMessageFactory.createMulticastMessage(batch, title, body);

            log.info("[FCM 다건 전송 요청] tokens={}", batch.size());
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("[FCM 다건 전송 결과] 성공={} 실패={}", response.getSuccessCount(), response.getFailureCount());

            allResponses.add(response.getResponses());

            // TODO: 성공 토큰 lastUsedAt 업데이트
            // TODO: 실패 토큰 삭제 처리
        }

        // 알림 이력 생성
        for (List<SendResponse> responses : allResponses) {
            for (SendResponse sendResponse : responses) {
                NotificationHistoryStatus status;
                String failureReason = null;

                if (sendResponse.isSuccessful()) {
                    status = NotificationHistoryStatus.SUCCESS;
                } else {
                    status = NotificationHistoryStatus.FAILURE;
                    failureReason = sendResponse.getException().getMessage();
                }

                NotificationHistoryPayload payload = NotificationHistoryPayload.of(
                        memberId,
                        NOTIFICATION_HISTORY_TYPE,
                        title,
                        body,
                        status,
                        failureReason
                );
                historyPayloads.add(payload);
            }
        }

        notificationHistoryService.bulkInsertNotificationHistories(historyPayloads);
    }
}
