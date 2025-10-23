package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.*;
import com.sparta.couponpop.domain.notification.factory.FcmMessageFactory;
import com.sparta.couponpop.domain.notificationhistory.dto.payload.NotificationHistoryPayload;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryStatus;
import com.sparta.couponpop.domain.notificationhistory.enums.NotificationHistoryType;
import com.sparta.couponpop.domain.notificationhistory.service.NotificationHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmSendServiceTest {

    @Mock
    private FcmMessageFactory fcmMessageFactory;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @InjectMocks
    private FcmSendService fcmSendService;

    @Nested
    @DisplayName("FCM 알림 발송")
    class SendNotification {
        @Test
        @DisplayName("단일 토큰 전송 시 메시지를 생성한다")
        void sendNotification_success_singleToken() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            String token = "test-token";
            String title = "알림 제목";
            String body = "알림 내용";
            given(fcmMessageFactory.createMessage(anyString(), anyString(), anyString()))
                    .willAnswer(invocation -> {
                        String requestToken = invocation.getArgument(0, String.class);
                        String requestTitle = invocation.getArgument(1, String.class);
                        String requestBody = invocation.getArgument(2, String.class);
                        return Message.builder()
                                .setToken(requestToken)
                                .setNotification(Notification.builder()
                                        .setTitle(requestTitle)
                                        .setBody(requestBody)
                                        .build())
                                .putData("title", requestTitle)
                                .putData("body", requestBody)
                                .build();
                    });

            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
            given(firebaseMessaging.send(any(Message.class))).willReturn("mock-message-id");
            NotificationHistoryPayload expectedPayload = NotificationHistoryPayload.of(
                    memberId,
                    NotificationHistoryType.FCM,
                    title,
                    body,
                    NotificationHistoryStatus.SUCCESS,
                    null
            );

            try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
                mockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                // when
                fcmSendService.sendNotification(memberId, token, title, body);

                // then
                ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
                then(firebaseMessaging).should(times(1)).send(messageCaptor.capture());
                Message sentMessage = messageCaptor.getValue();

                assertThat(extractToken(sentMessage)).isEqualTo(token);
                Notification notification = extractNotification(sentMessage);
                assertThat(extractNotificationValue(notification, "title")).isEqualTo(title);
                assertThat(extractNotificationValue(notification, "body")).isEqualTo(body);
                then(notificationHistoryService).should().createNotificationHistory(expectedPayload);
            }
        }

        @Test
        @DisplayName("토큰이 비어 있으면 FCM 전송을 수행하지 않는다")
        void sendNotification_skipSend_tokensEmpty() throws FirebaseMessagingException {
            // given
                Long memberId = 1L;
                List<String> emptyTokens = Collections.emptyList();

            try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
                // when
                fcmSendService.sendNotification(memberId, emptyTokens, "제목", "본문");

                // then
                mockedStatic.verifyNoInteractions();
                verifyNoInteractions(fcmMessageFactory);
            }
        }

        @Test
        @DisplayName("토큰이 500개 초과 시 500개 단위로 배치 전송한다")
        void sendNotification_success_tokensExceedLimit() throws FirebaseMessagingException {
            // given
            Long memberId = 2L;
            List<String> tokens = new ArrayList<>();
            for (int i = 0; i < 750; i++) {
                tokens.add("token-" + i);
            }
            given(fcmMessageFactory.createMulticastMessage(anyList(), anyString(), anyString()))
                    .willAnswer(invocation -> {
                        @SuppressWarnings("unchecked")
                        List<String> requestTokens = new ArrayList<>((List<String>) invocation.getArgument(0, List.class));
                        String requestTitle = invocation.getArgument(1, String.class);
                        String requestBody = invocation.getArgument(2, String.class);
                        return buildMessage(requestTokens, requestTitle, requestBody);
                    });
            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
            willAnswer(invocation -> {
                MulticastMessage multicastMessage = invocation.getArgument(0, MulticastMessage.class);
                List<String> requestTokens = extractTokens(multicastMessage);

                BatchResponse dynamicResponse = mock(BatchResponse.class);
                List<SendResponse> sendResponses = requestTokens.stream()
                        .map(token -> {
                            SendResponse sendResponse = mock(SendResponse.class);
                            given(sendResponse.isSuccessful()).willReturn(true);
                            return sendResponse;
                        })
                        .toList();

                given(dynamicResponse.getSuccessCount()).willReturn(requestTokens.size());
                given(dynamicResponse.getFailureCount()).willReturn(0);
                given(dynamicResponse.getResponses()).willReturn(sendResponses);
                return dynamicResponse;
            }).given(firebaseMessaging).sendEachForMulticast(any(MulticastMessage.class));

            try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
                mockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                // when
                fcmSendService.sendNotification(memberId, tokens, "배치 제목", "배치 본문");

                // then
                ArgumentCaptor<MulticastMessage> messageCaptor = ArgumentCaptor.forClass(MulticastMessage.class);
                then(firebaseMessaging).should(times(2)).sendEachForMulticast(messageCaptor.capture());
                List<MulticastMessage> capturedMessages = messageCaptor.getAllValues();

                assertThat(capturedMessages).hasSize(2);
                assertThat(extractTokens(capturedMessages.get(0))).hasSize(500);
                assertThat(extractTokens(capturedMessages.get(1))).hasSize(250);
                then(fcmMessageFactory).should(times(2)).createMulticastMessage(anyList(), eq("배치 제목"), eq("배치 본문"));
                then(notificationHistoryService).should().bulkInsertNotificationHistories(argThat(payloads -> {
                    assertThat(payloads).hasSize(tokens.size());
                    assertThat(payloads)
                            .allMatch(payload -> payload.memberId().equals(memberId)
                                    && payload.type() == NotificationHistoryType.FCM
                                    && payload.title().equals("배치 제목")
                                    && payload.body().equals("배치 본문")
                                    && payload.status() == NotificationHistoryStatus.SUCCESS);
                    return true;
                }));
            }
        }

        private MulticastMessage buildMessage(List<String> tokens,
                                              String title,
                                              String body) {
            // 팩토리가 생성하는 메시지를 모사해 빌더 기반 구성을 재현한다.
            return MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("title", title)
                    .putData("body", body)
                    .build();
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

        private String extractToken(Message message) {
            // Message는 Getter를 제공하지 않아 리플렉션으로 토큰을 검증한다.
            try {
                Field field = Message.class.getDeclaredField("token");
                field.setAccessible(true);
                return (String) field.get(message);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("토큰 정보를 확인할 수 없습니다.", e);
            }
        }

        private Notification extractNotification(Message message) {
            // 알림 객체 역시 리플렉션으로 확인한다.
            try {
                Field field = Message.class.getDeclaredField("notification");
                field.setAccessible(true);
                return (Notification) field.get(message);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("알림 정보를 확인할 수 없습니다.", e);
            }
        }

        private String extractNotificationValue(Notification notification, String fieldName) {
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
