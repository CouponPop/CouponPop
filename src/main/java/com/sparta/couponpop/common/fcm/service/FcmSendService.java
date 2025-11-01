package com.sparta.couponpop.common.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.sparta.couponpop.common.fcm.factory.FcmMessageFactory;
import com.sparta.couponpop.domain.member.service.MemberFcmTokenService;
import com.sparta.couponpop.domain.notificationhistory.dto.payload.NotificationHistoryPayload;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryStatus;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryType;
import com.sparta.couponpop.domain.notificationhistory.service.NotificationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    private static final NotificationHistoryType NOTIFICATION_HISTORY_TYPE = NotificationHistoryType.FCM;

    private final FcmMessageFactory fcmMessageFactory;
    private final NotificationHistoryService notificationHistoryService;
    private final MemberFcmTokenService memberFcmTokenService;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * 푸시 알림 전송
     *
     * @param memberId 회원 ID
     * @param token    FCM 토큰
     * @param title    알림 제목
     * @param body     알림 내용
     * @throws FirebaseMessagingException FCM 전송 중 오류 발생 시 예외 처리
     */
    public void sendNotification(Long memberId, String token, String title, String body) throws FirebaseMessagingException {
        log.info("푸시 알림 전송 시작: memberId={}, token={}", memberId, token);

        if (!StringUtils.hasText(token)) {
            log.info("FCM 전송 토큰이 유효하지 않아 전송을 건너뜁니다.");
            return;
        }

        NotificationHistoryStatus status = NotificationHistoryStatus.SUCCESS;
        String failureReason = null;

        Message message = fcmMessageFactory.createMessage(token, title, body);
        try {
            firebaseMessaging.send(message);

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
}
