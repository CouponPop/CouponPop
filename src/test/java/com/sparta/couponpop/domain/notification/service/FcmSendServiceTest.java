package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmSendServiceTest {

    private final FcmSendService fcmSendService = new FcmSendService();

    @Nested
    @DisplayName("FCM 알림 발송")
    class SendNotification {
        @Test
        @DisplayName("단일 토큰 전송 시 멀티캐스트 메시지를 생성한다")
        void sendNotification_success_singleToken() throws FirebaseMessagingException {
            // given
            String token = "test-token";
            String title = "알림 제목";
            String body = "알림 내용";
            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);

            given(batchResponse.getSuccessCount()).willReturn(1);
            given(batchResponse.getFailureCount()).willReturn(0);
            given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(batchResponse);

            try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
                mockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                // when
                fcmSendService.sendNotification(token, title, body);

                // then
                ArgumentCaptor<MulticastMessage> messageCaptor = ArgumentCaptor.forClass(MulticastMessage.class);
                then(firebaseMessaging).should(times(1)).sendEachForMulticast(messageCaptor.capture());
                MulticastMessage sentMessage = messageCaptor.getValue();

                assertThat(extractTokens(sentMessage)).containsExactly(token);
                Notification notification = extractNotification(sentMessage);
                assertThat(extractNotificationValue(notification, "title")).isEqualTo(title);
                assertThat(extractNotificationValue(notification, "body")).isEqualTo(body);
            }
        }

        @Test
        @DisplayName("토큰이 비어 있으면 FCM 전송을 수행하지 않는다")
        void sendNotification_skipSend_tokensEmpty() throws FirebaseMessagingException {
            // given
            List<String> emptyTokens = Collections.emptyList();

            try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
                // when
                fcmSendService.sendNotification(emptyTokens, "제목", "본문");

                // then
                mockedStatic.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("토큰이 500개 초과 시 500개 단위로 배치 전송한다")
        void sendNotification_success_tokensExceedLimit() throws FirebaseMessagingException {
            // given
            List<String> tokens = new ArrayList<>();
            for (int i = 0; i < 750; i++) {
                tokens.add("token-" + i);
            }
            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);

            given(batchResponse.getSuccessCount()).willReturn(500);
            given(batchResponse.getFailureCount()).willReturn(0);
            given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(batchResponse);

            try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
                mockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                // when
                fcmSendService.sendNotification(tokens, "배치 제목", "배치 본문");

                // then
                ArgumentCaptor<MulticastMessage> messageCaptor = ArgumentCaptor.forClass(MulticastMessage.class);
                then(firebaseMessaging).should(times(2)).sendEachForMulticast(messageCaptor.capture());
                List<MulticastMessage> capturedMessages = messageCaptor.getAllValues();

                assertThat(capturedMessages).hasSize(2);
                assertThat(extractTokens(capturedMessages.get(0))).hasSize(500);
                assertThat(extractTokens(capturedMessages.get(1))).hasSize(250);
            }
        }

        private List<String> extractTokens(MulticastMessage message) {
            // MulticastMessage는 Getter를 제공하지 않아 리플렉션으로 토큰 수를 검증한다.
            try {
                Field field = MulticastMessage.class.getDeclaredField("tokens");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<String> tokens = (List<String>) field.get(message);
                return tokens;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("토큰 정보를 확인할 수 없습니다.", e);
            }
        }

        private Notification extractNotification(MulticastMessage message) {
            // 알림 객체 역시 리플렉션으로 확인한다.
            try {
                Field field = MulticastMessage.class.getDeclaredField("notification");
                field.setAccessible(true);
                return (Notification) field.get(message);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("알림 정보를 확인할 수 없습니다.", e);
            }
        }

        private String extractNotificationValue(Notification notification,
                                                String fieldName) {
            // Notification은 비공개 필드만 노출하므로 리플렉션으로 값을 읽는다.
            try {
                Field field = Notification.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (String) field.get(notification);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("알림 속성을 확인할 수 없습니다.", e);
            }
        }
    }
}
