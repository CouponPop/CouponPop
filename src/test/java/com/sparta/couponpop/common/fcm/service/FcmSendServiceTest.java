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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmSendServiceTest {

    @Mock
    private FcmMessageFactory fcmMessageFactory;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @Mock
    private MemberFcmTokenService memberFcmTokenService;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FcmSendService fcmSendService;

    @Nested
    @DisplayName("단일 기기 푸시 알림 전송")
    class SendNotificationWithSingleToken {

        @Test
        @DisplayName("전송 성공 시 토큰을 갱신하고 히스토리를 성공으로 저장한다")
        void sendNotification_success() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            String token = "success-token";
            String title = "제목";
            String body = "본문";

            Message message = Message.builder().setToken(token).build();
            when(fcmMessageFactory.createMessage(token, title, body)).thenReturn(message);

            // when
            fcmSendService.sendNotification(memberId, token, title, body);

            // then
            verify(firebaseMessaging).send(message);
            verify(memberFcmTokenService).updateLastUsedAt(token);
            verify(notificationHistoryService).createNotificationHistory(NotificationHistoryPayload.of(
                    memberId,
                    NotificationHistoryType.FCM,
                    title,
                    body,
                    NotificationHistoryStatus.SUCCESS,
                    null
            ));
        }

        @Test
        @DisplayName("FCM 예외가 발생하면 토큰을 삭제하고 실패 히스토리를 저장한다")
        void sendNotification_failure() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            String token = "failure-token";
            String title = "제목";
            String body = "본문";

            Message message = Message.builder().setToken(token).build();
            when(fcmMessageFactory.createMessage(token, title, body)).thenReturn(message);

            FirebaseMessagingException messagingException = mock(FirebaseMessagingException.class);
            when(messagingException.getMessage()).thenReturn("전송 실패");
            when(firebaseMessaging.send(message)).thenThrow(messagingException);

            // when
            try {
                fcmSendService.sendNotification(memberId, token, title, body);
            } catch (FirebaseMessagingException ignored) {
                // 예외는 상위로 전달된다.
            }

            // then
            verify(firebaseMessaging).send(message);
            verify(memberFcmTokenService).deleteToken(token);
            verify(notificationHistoryService).createNotificationHistory(NotificationHistoryPayload.of(
                    memberId,
                    NotificationHistoryType.FCM,
                    title,
                    body,
                    NotificationHistoryStatus.FAILURE,
                    "전송 실패"
            ));
        }

        @Test
        @DisplayName("토큰이 비어 있으면 전송을 건너뛴다")
        void sendNotification_skip_tokenEmpty() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            String token = "";

            // when
            fcmSendService.sendNotification(memberId, token, "제목", "본문");

            // then
            verifyNoInteractions(fcmMessageFactory, memberFcmTokenService, notificationHistoryService);
        }
    }
}
