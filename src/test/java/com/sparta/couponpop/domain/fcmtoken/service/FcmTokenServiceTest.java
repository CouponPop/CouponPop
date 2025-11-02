package com.sparta.couponpop.domain.fcmtoken.service;

import com.sparta.couponpop.common.dto.member.response.GetMemberIdResponse;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.fcmtoken.dto.request.FcmTokenRequest;
import com.sparta.couponpop.domain.fcmtoken.entity.FcmToken;
import com.sparta.couponpop.domain.fcmtoken.exception.FcmTokenErrorCode;
import com.sparta.couponpop.domain.fcmtoken.repository.FcmTokenRepository;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.service.MemberInternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private MemberInternalService memberInternalService;

    @InjectMocks
    private FcmTokenService fcmTokenService;

    @Nested
    @DisplayName("upsertTokenForMember")
    class UpsertTokenForMember {

        @Test
        @DisplayName("중복 토큰이 존재하면 회원과 기기 식별자를 갱신한다")
        void upsertTokenForMember_thenDuplicatedToken_updatesMemberAndDeviceIdentifier() {
            // given
            Long memberId = 1L;
            GetMemberIdResponse getMemberIdResponse = GetMemberIdResponse.of(memberId);
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();
            FcmToken duplicatedFcmToken = mock(FcmToken.class);

            given(memberInternalService.getMemberId(memberId)).willReturn(getMemberIdResponse);
            given(fcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.of(duplicatedFcmToken));

            // when
            fcmTokenService.upsertTokenForMember(request, memberId);

            // then
            then(duplicatedFcmToken).should(times(1)).updateMemberIdAndDeviceIdentifier(eq(getMemberIdResponse.memberId()), eq(request.deviceIdentifier()), any(LocalDateTime.class));

            then(fcmTokenRepository).should(times(1)).findByFcmToken(request.fcmToken());
            then(fcmTokenRepository).should(never()).findByMemberIdAndDeviceIdentifier(anyLong(), anyString());
            then(fcmTokenRepository).should(never()).save(any(FcmToken.class));
        }

        @Test
        @DisplayName("기존 토큰이 존재하면 FCM 토큰을 갱신한다")
        void upsertTokenForMember_thenExistingToken_updatesFcmToken() {
            // given
            Long memberId = 1L;
            GetMemberIdResponse getMemberIdResponse = GetMemberIdResponse.of(memberId);
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("new-fcm-token")
                    .deviceType("IOS")
                    .deviceIdentifier("device-123")
                    .build();
            FcmToken activeFcmToken = mock(FcmToken.class);

            given(memberInternalService.getMemberId(memberId)).willReturn(getMemberIdResponse);
            given(fcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.empty());
            given(fcmTokenRepository.findByMemberIdAndDeviceIdentifier(getMemberIdResponse.memberId(), request.deviceIdentifier())).willReturn(Optional.of(activeFcmToken));

            // when
            fcmTokenService.upsertTokenForMember(request, memberId);

            // then
            then(activeFcmToken).should(times(1)).updateFcmToken(eq(request.fcmToken()), any(LocalDateTime.class));

            then(fcmTokenRepository).should(times(1)).findByFcmToken(request.fcmToken());
            then(fcmTokenRepository).should(times(1)).findByMemberIdAndDeviceIdentifier(getMemberIdResponse.memberId(), request.deviceIdentifier());
            then(fcmTokenRepository).should(never()).save(any(FcmToken.class));
        }

        @Test
        @DisplayName("토큰이 없다면 신규 토큰을 저장한다")
        void upsertTokenForMember_thenTokenNotExists_savesNewToken() {
            // given
            Long memberId = 1L;
            GetMemberIdResponse getMemberIdResponse = GetMemberIdResponse.of(memberId);
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("fresh-fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();

            given(memberInternalService.getMemberId(memberId)).willReturn(getMemberIdResponse);
            given(fcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.empty());
            given(fcmTokenRepository.findByMemberIdAndDeviceIdentifier(getMemberIdResponse.memberId(), request.deviceIdentifier())).willReturn(Optional.empty());
            given(fcmTokenRepository.save(any(FcmToken.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            fcmTokenService.upsertTokenForMember(request, memberId);

            // then
            ArgumentCaptor<FcmToken> tokenCaptor = ArgumentCaptor.forClass(FcmToken.class);
            then(fcmTokenRepository).should(times(1)).save(tokenCaptor.capture());

            FcmToken savedFcmToken = tokenCaptor.getValue();
            assertThat(savedFcmToken.getMemberId()).isEqualTo(getMemberIdResponse.memberId());
            assertThat(savedFcmToken.getFcmToken()).isEqualTo(request.fcmToken());
            assertThat(savedFcmToken.getDeviceType()).isEqualTo(request.deviceType());
            assertThat(savedFcmToken.getDeviceIdentifier()).isEqualTo(request.deviceIdentifier());
            assertThat(savedFcmToken.getLastUsedAt()).isNotNull();
        }

        // TODO: MSA 분리 후 해당 테스트 재검토 필요
        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void upsertTokenForMember_thenMemberNotFound_throwsException() {
            // given
            Long memberId = 99L;
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("sample-fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();

            given(memberInternalService.getMemberId(memberId)).willThrow(new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> fcmTokenService.upsertTokenForMember(request, memberId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            verifyNoInteractions(fcmTokenRepository);
        }
    }

    @Nested
    @DisplayName("updateLastUsedAt")
    class UpdateLastUsedAt {

        @Test
        @DisplayName("토큰이 존재하면 최근 사용 시간을 갱신한다")
        void updateLastUsedAt_success_tokenExists() {
            // given
            String token = "existing-token";
            FcmToken fcmToken = mock(FcmToken.class);
            given(fcmTokenRepository.findByFcmToken(token)).willReturn(Optional.of(fcmToken));

            // when
            fcmTokenService.updateLastUsedAt(token);

            // then
            then(fcmTokenRepository).should(times(1)).findByFcmToken(token);
            then(fcmToken).should(times(1)).updateLastUsedAt(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("토큰이 없으면 예외를 던진다")
        void updateLastUsedAt_fail_tokenNotFound() {
            // given
            String token = "missing-token";
            given(fcmTokenRepository.findByFcmToken(token)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fcmTokenService.updateLastUsedAt(token))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(FcmTokenErrorCode.FCM_TOKEN_NOT_FOUND.getMessage());

            then(fcmTokenRepository).should(times(1)).findByFcmToken(token);
        }
    }

    @Nested
    @DisplayName("deleteToken")
    class DeleteToken {

        @Test
        @DisplayName("토큰이 존재하면 삭제한다")
        void deleteToken_success_tokenExists() {
            // given
            String token = "removable-token";
            FcmToken fcmToken = mock(FcmToken.class);
            given(fcmTokenRepository.findByFcmToken(token)).willReturn(Optional.of(fcmToken));

            // when
            fcmTokenService.deleteToken(token);

            // then
            then(fcmTokenRepository).should(times(1)).findByFcmToken(token);
            then(fcmTokenRepository).should(times(1)).delete(fcmToken);
        }

        @Test
        @DisplayName("토큰이 없으면 예외를 던진다")
        void deleteToken_fail_tokenNotFound() {
            // given
            String token = "invalid-token";
            given(fcmTokenRepository.findByFcmToken(token)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fcmTokenService.deleteToken(token))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(FcmTokenErrorCode.FCM_TOKEN_NOT_FOUND.getMessage());

            then(fcmTokenRepository).should(times(1)).findByFcmToken(token);
            then(fcmTokenRepository).should(never()).delete(any(FcmToken.class));
        }
    }
}
