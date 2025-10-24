package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.*;
import com.sparta.couponpop.domain.member.service.MemberFcmTokenService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    // FCM 멀티캐스트 API는 한 번의 요청에 최대 500개의 토큰만 허용한다.
    private static final int FCM_MULTICAST_LIMIT = 500;
    private static final NotificationHistoryType NOTIFICATION_HISTORY_TYPE = NotificationHistoryType.FCM;

    private final FcmMessageFactory fcmMessageFactory;
    private final NotificationHistoryService notificationHistoryService;
    private final MemberFcmTokenService memberFcmTokenService;

    /**
     * 단일 기기 푸시 알림 전송
     *
     * @param memberId 회원 ID
     * @param token    FCM 토큰
     * @param title    알림 제목
     * @param body     알림 내용
     * @throws FirebaseMessagingException FCM 전송 중 오류 발생 시 예외 처리
     */
    public void sendNotification(Long memberId, String token, String title, String body) throws FirebaseMessagingException {
        log.info("단일 기기 푸시 알림 전송 시작: memberId={}, token={}", memberId, token);

        if (!StringUtils.hasText(token)) {
            log.info("FCM 전송 토큰이 유효하지 않아 전송을 건너뜁니다.");
            return;
        }

        NotificationHistoryStatus status = NotificationHistoryStatus.SUCCESS;
        String failureReason = null;

        Message message = fcmMessageFactory.createMessage(token, title, body);
        try {
            FirebaseMessaging.getInstance().send(message);

            memberFcmTokenService.updateLastUsedAt(token);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 중 오류 발생: {}", e.getMessage());

            status = NotificationHistoryStatus.FAILURE;
            failureReason = e.getMessage();

            memberFcmTokenService.deleteToken(token);

            throw e; // 예외 재던지기
        } finally {
            NotificationHistoryPayload notificationHistoryPayload = NotificationHistoryPayload.of(memberId, NOTIFICATION_HISTORY_TYPE, title, body, status, failureReason);
            notificationHistoryService.createNotificationHistory(notificationHistoryPayload);
        }
    }

    /**
     * 멤버별 다중 기기 푸시 알림 전송
     *
     * @param memberId 멤버 ID
     * @param tokens   FCM 토큰 리스트
     * @param title    알림 제목
     * @param body     알림 내용
     * @throws FirebaseMessagingException FCM 전송 중 오류 발생 시 예외 처리
     */
    public void sendNotification(Long memberId, List<String> tokens, String title, String body) throws FirebaseMessagingException {
        log.info("멤버별 다중 기기 푸시 알림 전송 시작: memberId={}, tokensCount={}", memberId, tokens.size());

        if (tokens == null || tokens.isEmpty()) {
            log.info("FCM 전송 토큰이 비어 있어 전송을 건너뜁니다.");
            return;
        }

        List<List<SendResponse>> totalResponses = new ArrayList<>();
        for (int start = 0; start < tokens.size(); start += FCM_MULTICAST_LIMIT) {
            int end = Math.min(start + FCM_MULTICAST_LIMIT, tokens.size());
            List<String> batch = tokens.subList(start, end);

            MulticastMessage message = fcmMessageFactory.createMulticastMessage(batch, title, body);

            log.info("[FCM 다건 전송 요청] tokens={}", batch.size());
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("[FCM 다건 전송 결과] 성공={} 실패={}", response.getSuccessCount(), response.getFailureCount());

            totalResponses.add(response.getResponses());
        }

        createHistoryAfterMulticastSend(totalResponses, memberId, title, body);
        updateTokensAfterMulticastSend(totalResponses, tokens);
    }

    // 다중 기기 푸시 후 알림 히스토리 생성
    private void createHistoryAfterMulticastSend(List<List<SendResponse>> totalResponses,
                                                 Long memberId,
                                                 String title,
                                                 String body) {
        List<NotificationHistoryPayload> historyPayloads = new ArrayList<>();

        for (List<SendResponse> responses : totalResponses) {
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

    // 다중 기기 푸시 후 토큰 업데이트
    private void updateTokensAfterMulticastSend(List<List<SendResponse>> totalResponses,
                                                List<String> tokens) {
        Set<String> successTokens = new HashSet<>();
        Set<String> failureTokens = new HashSet<>();

        // 알림 히스토리 생성 및 성공/실패 토큰 처리
        for (int batchIndex = 0; batchIndex < totalResponses.size(); batchIndex++) {
            List<SendResponse> responses = totalResponses.get(batchIndex);

            for (int i = 0; i < responses.size(); i++) {
                int tokenIndex = batchIndex * FCM_MULTICAST_LIMIT + i;
                SendResponse sendResponse = responses.get(i);

                if (sendResponse.isSuccessful()) {
                    successTokens.add(tokens.get(tokenIndex));
                } else {
                    failureTokens.add(tokens.get(tokenIndex));
                }
            }
        }

        memberFcmTokenService.updateTokensAfterSend(successTokens, failureTokens);
    }
}
