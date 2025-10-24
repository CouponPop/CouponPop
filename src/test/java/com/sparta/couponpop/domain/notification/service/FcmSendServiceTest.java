package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.*;
import com.sparta.couponpop.domain.member.service.MemberFcmTokenService;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmSendServiceTest {

    @Mock
    private FcmMessageFactory fcmMessageFactory;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @Mock
    private MemberFcmTokenService memberFcmTokenService;

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

            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

            try (MockedStatic<FirebaseMessaging> mockedFirebaseMessaging = mockStatic(FirebaseMessaging.class)) {
                mockedFirebaseMessaging.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

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

            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

            FirebaseMessagingException messagingException = mock(FirebaseMessagingException.class);
            when(messagingException.getMessage()).thenReturn("전송 실패");
            when(firebaseMessaging.send(message)).thenThrow(messagingException);

            try (MockedStatic<FirebaseMessaging> mockedFirebaseMessaging = mockStatic(FirebaseMessaging.class)) {
                mockedFirebaseMessaging.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

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

    @Nested
    @DisplayName("다중 기기 푸시 알림 전송")
    class SendNotificationWithMultipleTokens {

        @Test
        @DisplayName("FCM 응답에 따라 성공 토큰을 갱신하고 실패 토큰을 정리한다")
        void sendNotification_success() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            List<String> tokens = List.of("token-success", "token-failure");
            String title = "제목";
            String body = "본문";

            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .build();
            when(fcmMessageFactory.createMulticastMessage(anyList(), eq(title), eq(body))).thenReturn(multicastMessage);

            SendResponse successResponse = mock(SendResponse.class);
            when(successResponse.isSuccessful()).thenReturn(true);

            SendResponse failureResponse = mock(SendResponse.class);
            when(failureResponse.isSuccessful()).thenReturn(false);

            FirebaseMessagingException messagingException = mock(FirebaseMessagingException.class);
            when(messagingException.getMessage()).thenReturn("FCM 실패");
            when(failureResponse.getException()).thenReturn(messagingException);

            BatchResponse batchResponse = mock(BatchResponse.class);
            when(batchResponse.getSuccessCount()).thenReturn(1);
            when(batchResponse.getFailureCount()).thenReturn(1);
            when(batchResponse.getResponses()).thenReturn(List.of(successResponse, failureResponse));

            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

            try (MockedStatic<FirebaseMessaging> mockedFirebaseMessaging = mockStatic(FirebaseMessaging.class)) {
                mockedFirebaseMessaging.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
                when(firebaseMessaging.sendEachForMulticast(multicastMessage)).thenReturn(batchResponse);

                // when
                fcmSendService.sendNotification(memberId, tokens, title, body);

                // then
                @SuppressWarnings("unchecked")
                ArgumentCaptor<Set<String>> successCaptor = ArgumentCaptor.forClass(Set.class);
                @SuppressWarnings("unchecked")
                ArgumentCaptor<Set<String>> failureCaptor = ArgumentCaptor.forClass(Set.class);
                verify(memberFcmTokenService).updateTokensAfterSend(successCaptor.capture(), failureCaptor.capture());

                assertThat(successCaptor.getValue()).containsExactlyInAnyOrder("token-success");
                assertThat(failureCaptor.getValue()).containsExactlyInAnyOrder("token-failure");

                @SuppressWarnings("unchecked")
                ArgumentCaptor<List<NotificationHistoryPayload>> historyCaptor = ArgumentCaptor.forClass(List.class);
                verify(notificationHistoryService).bulkInsertNotificationHistories(historyCaptor.capture());

                List<NotificationHistoryPayload> payloads = historyCaptor.getValue();
                assertThat(payloads).hasSize(2);
                assertThat(payloads.get(0).status()).isEqualTo(NotificationHistoryStatus.SUCCESS);
                assertThat(payloads.get(1).status()).isEqualTo(NotificationHistoryStatus.FAILURE);
                assertThat(payloads.get(1).failureReason()).isEqualTo("FCM 실패");

                verify(firebaseMessaging).sendEachForMulticast(multicastMessage);
            }
        }

        @Test
        @DisplayName("토큰이 비어 있으면 전송을 건너뛴다")
        void sendNotification_skip_tokensEmpty() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            List<String> tokens = Collections.emptyList();

            // when
            fcmSendService.sendNotification(memberId, tokens, "제목", "본문");

            // then
            verifyNoInteractions(fcmMessageFactory, memberFcmTokenService, notificationHistoryService);
        }

        @Test
        @DisplayName("토큰 개수가 500개가 초과되면 배치로 나누어 전송한다")
        void sendNotification_success_tokensExceedLimit() throws FirebaseMessagingException {
            // given
            Long memberId = 1L;
            List<String> tokens = IntStream.range(0, 600)
                    .mapToObj(i -> "token-" + i)
                    .toList();
            String title = "제목";
            String body = "본문";

            MulticastMessage firstBatchMessage = mock(MulticastMessage.class);
            MulticastMessage secondBatchMessage = mock(MulticastMessage.class);
            when(fcmMessageFactory.createMulticastMessage(anyList(), eq(title), eq(body)))
                    .thenAnswer(invocation -> {
                        List<String> batchTokens = invocation.getArgument(0);
                        if (batchTokens.size() == 500) {
                            return firstBatchMessage;
                        }
                        if (batchTokens.size() == 100) {
                            return secondBatchMessage;
                        }
                        throw new IllegalArgumentException("알 수 없는 배치 크기: " + batchTokens.size());
                    });

            SendResponse successResponse = mock(SendResponse.class);
            when(successResponse.isSuccessful()).thenReturn(true);

            BatchResponse firstBatchResponse = mock(BatchResponse.class);
            when(firstBatchResponse.getSuccessCount()).thenReturn(500);
            when(firstBatchResponse.getFailureCount()).thenReturn(0);
            when(firstBatchResponse.getResponses()).thenReturn(Collections.nCopies(500, successResponse));

            BatchResponse secondBatchResponse = mock(BatchResponse.class);
            when(secondBatchResponse.getSuccessCount()).thenReturn(100);
            when(secondBatchResponse.getFailureCount()).thenReturn(0);
            when(secondBatchResponse.getResponses()).thenReturn(Collections.nCopies(100, successResponse));

            FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

            try (MockedStatic<FirebaseMessaging> mockedFirebaseMessaging = mockStatic(FirebaseMessaging.class)) {
                mockedFirebaseMessaging.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
                when(firebaseMessaging.sendEachForMulticast(firstBatchMessage)).thenReturn(firstBatchResponse);
                when(firebaseMessaging.sendEachForMulticast(secondBatchMessage)).thenReturn(secondBatchResponse);

                // when
                fcmSendService.sendNotification(memberId, tokens, title, body);

                // then
                verify(firebaseMessaging, times(1)).sendEachForMulticast(firstBatchMessage);
                verify(firebaseMessaging, times(1)).sendEachForMulticast(secondBatchMessage);

                @SuppressWarnings("unchecked")
                ArgumentCaptor<Set<String>> successCaptor = ArgumentCaptor.forClass(Set.class);
                @SuppressWarnings("unchecked")
                ArgumentCaptor<Set<String>> failureCaptor = ArgumentCaptor.forClass(Set.class);
                verify(memberFcmTokenService).updateTokensAfterSend(successCaptor.capture(), failureCaptor.capture());

                assertThat(successCaptor.getValue()).containsExactlyInAnyOrderElementsOf(tokens);
                assertThat(failureCaptor.getValue()).isEmpty();

                @SuppressWarnings("unchecked")
                ArgumentCaptor<List<NotificationHistoryPayload>> historyCaptor = ArgumentCaptor.forClass(List.class);
                verify(notificationHistoryService).bulkInsertNotificationHistories(historyCaptor.capture());

                List<NotificationHistoryPayload> payloads = historyCaptor.getValue();
                assertThat(payloads).hasSize(tokens.size());
                assertThat(payloads)
                        .allMatch(payload -> payload.status() == NotificationHistoryStatus.SUCCESS
                                && payload.failureReason() == null);
            }
        }
    }
}
