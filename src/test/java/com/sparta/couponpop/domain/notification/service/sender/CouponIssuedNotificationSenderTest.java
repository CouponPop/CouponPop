package com.sparta.couponpop.domain.notification.service.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.dto.command.CouponIssuedNotificationCommand;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.service.FcmSendService;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CouponIssuedNotificationSenderTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberFcmTokenRepository memberFcmTokenRepository;

    @Mock
    private FcmSendService fcmSendService;

    @InjectMocks
    private CouponIssuedNotificationSender sender;

    @Nested
    @DisplayName("손님 쿠폰 수령 알림 전송")
    class Send {

        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void send_fail_memberNotFound() {
            // given
            Long memberId = 1L;
            CouponIssuedNotificationCommand command = CouponIssuedNotificationCommand.of(
                    memberId,
                    CouponIssuedNotificationPayload.of(
                            "아메리카노 1+1",
                            "Coupon-123",
                            LocalDateTime.of(2024, 3, 1, 10, 0)
                    )
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sender.send(command))
                    .isInstanceOf(GlobalException.class)
                    .extracting("errorCode")
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            then(memberFcmTokenRepository).shouldHaveNoInteractions();
            then(fcmSendService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("푸시 알림이 비활성화되면 FCM 전송을 시도하지 않는다")
        void send_skipSend_notificationsDisabled() throws FirebaseMessagingException {
            // given
            Long memberId = 2L;
            Member member = TestUtils.createEntity(Member.class, Map.of(
                    "id", memberId,
                    "email", "customer@test.com",
                    "username", "손님",
                    "password", "hashedPassword",
                    "phoneNumber", "010-0000-0000",
                    "memberType", MemberType.CUSTOMER
            ));
            CouponIssuedNotificationCommand command = CouponIssuedNotificationCommand.of(
                    memberId,
                    CouponIssuedNotificationPayload.of(
                            "아메리카노 1+1",
                            "Coupon-123",
                            LocalDateTime.of(2024, 7, 1, 12, 30)
                    )
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of());

            // when
            sender.send(command);

            // then
            then(memberFcmTokenRepository).should().findByMemberAndNotificationEnabledIsTrue(member);
            then(fcmSendService).should(never()).sendNotification(anyList(), anyString(), anyString());
        }

        @Test
        @DisplayName("알림이 활성화된 모든 기기에 중복 없이 FCM을 전송한다")
        void send_success_notificationsEnabled() throws FirebaseMessagingException {
            // given
            Long memberId = 3L;
            Member member = TestUtils.createEntity(Member.class, Map.of(
                    "id", memberId,
                    "email", "active@test.com",
                    "username", "활성회원",
                    "password", "hashedPassword",
                    "phoneNumber", "010-1111-2222",
                    "memberType", MemberType.CUSTOMER
            ));
            MemberFcmToken token1 = MemberFcmToken.of(
                    member,
                    "token-A",
                    "android",
                    "device-1",
                    true,
                    LocalDateTime.now()
            );
            MemberFcmToken token2 = MemberFcmToken.of(
                    member,
                    "token-B",
                    "ios",
                    "device-2",
                    true,
                    LocalDateTime.now()
            );
            MemberFcmToken duplicateToken = MemberFcmToken.of(
                    member,
                    "token-A",
                    "web",
                    "device-3",
                    true,
                    LocalDateTime.now()
            );
            LocalDateTime expireAt = LocalDateTime.of(2024, 9, 10, 18, 45);
            CouponIssuedNotificationCommand command = CouponIssuedNotificationCommand.of(
                    memberId,
                    CouponIssuedNotificationPayload.of(
                            "아메리카노 1+1",
                            "Coupon-123",
                            expireAt
                    )
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member))
                    .willReturn(List.of(token1, token2, duplicateToken));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<String>> tokensCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            // when
            sender.send(command);

            // then
            then(fcmSendService).should().sendNotification(
                    tokensCaptor.capture(),
                    titleCaptor.capture(),
                    bodyCaptor.capture()
            );

            String expectedBody = """
                    쿠폰명: %s
                    쿠폰코드: %s
                    만료기간: %s
                    """.formatted(
                    command.payload().couponName(),
                    command.payload().couponCode(),
                    expireAt
            );

            assertThat(tokensCaptor.getValue()).containsExactlyInAnyOrder("token-A", "token-B");
            assertThat(titleCaptor.getValue()).isEqualTo("쿠폰 수령이 완료되었습니다!");
            assertThat(bodyCaptor.getValue()).isEqualTo(expectedBody);
        }

        @Test
        @DisplayName("FCM 전송에 실패해도 예외를 전파하지 않는다")
        void send_ignoreException_fcmSendFails() throws FirebaseMessagingException {
            // given
            Long memberId = 4L;
            Member member = TestUtils.createEntity(Member.class, Map.of(
                    "id", memberId,
                    "email", "failure@test.com",
                    "username", "실패회원",
                    "password", "hashedPassword",
                    "phoneNumber", "010-3333-4444",
                    "memberType", MemberType.CUSTOMER
            ));
            MemberFcmToken token = MemberFcmToken.of(
                    member,
                    "token-FAIL",
                    "web",
                    "device-3",
                    true,
                    LocalDateTime.now()
            );
            CouponIssuedNotificationCommand command = CouponIssuedNotificationCommand.of(
                    memberId,
                    CouponIssuedNotificationPayload.of(
                            "아메리카노 1+1",
                            "Coupon-123",
                            LocalDateTime.of(2024, 10, 5, 20, 0)
                    )
            );
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member)).willReturn(List.of(token));

            FirebaseMessagingException messagingException = mock(FirebaseMessagingException.class);
            willThrow(messagingException).given(fcmSendService).sendNotification(anyList(), anyString(), anyString());

            // when & then
            assertThatCode(() -> sender.send(command)).doesNotThrowAnyException();
        }
    }
}
