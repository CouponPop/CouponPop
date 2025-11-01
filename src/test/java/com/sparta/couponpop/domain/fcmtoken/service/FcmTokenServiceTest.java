package com.sparta.couponpop.domain.fcmtoken.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.fcmtoken.dto.request.FcmTokenRequest;
import com.sparta.couponpop.domain.fcmtoken.entity.FcmToken;
import com.sparta.couponpop.domain.fcmtoken.repository.FcmTokenRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private MemberRepository memberRepository;

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
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();
            FcmToken duplicatedFcmToken = mock(FcmToken.class);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.of(duplicatedFcmToken));

            // when
            fcmTokenService.upsertTokenForMember(request, memberId);

            // then
            then(duplicatedFcmToken).should(times(1)).updateMemberAndDeviceIdentifier(eq(member), eq(request.deviceIdentifier()), any(LocalDateTime.class));

            then(fcmTokenRepository).should(times(1)).findByFcmToken(request.fcmToken());
            then(fcmTokenRepository).should(never()).findByMemberAndDeviceIdentifier(any(Member.class), anyString());
            then(fcmTokenRepository).should(never()).save(any(FcmToken.class));
        }

        @Test
        @DisplayName("기존 토큰이 존재하면 FCM 토큰을 갱신한다")
        void upsertTokenForMember_thenExistingToken_updatesFcmToken() {
            // given
            Long memberId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("new-fcm-token")
                    .deviceType("IOS")
                    .deviceIdentifier("device-123")
                    .build();
            FcmToken activeFcmToken = mock(FcmToken.class);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.empty());
            given(fcmTokenRepository.findByMemberAndDeviceIdentifier(member, request.deviceIdentifier())).willReturn(Optional.of(activeFcmToken));

            // when
            fcmTokenService.upsertTokenForMember(request, memberId);

            // then
            then(activeFcmToken).should(times(1)).updateFcmToken(eq(request.fcmToken()), any(LocalDateTime.class));

            then(fcmTokenRepository).should(times(1)).findByFcmToken(request.fcmToken());
            then(fcmTokenRepository).should(times(1)).findByMemberAndDeviceIdentifier(member, request.deviceIdentifier());
            then(fcmTokenRepository).should(never()).save(any(FcmToken.class));
        }

        @Test
        @DisplayName("토큰이 없다면 신규 토큰을 저장한다")
        void upsertTokenForMember_thenTokenNotExists_savesNewToken() {
            // given
            Long memberId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            FcmTokenRequest request = FcmTokenRequest.builder()
                    .fcmToken("fresh-fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(fcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.empty());
            given(fcmTokenRepository.findByMemberAndDeviceIdentifier(member, request.deviceIdentifier())).willReturn(Optional.empty());
            given(fcmTokenRepository.save(any(FcmToken.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            fcmTokenService.upsertTokenForMember(request, memberId);

            // then
            ArgumentCaptor<FcmToken> tokenCaptor = ArgumentCaptor.forClass(FcmToken.class);
            then(fcmTokenRepository).should(times(1)).save(tokenCaptor.capture());

            FcmToken savedFcmToken = tokenCaptor.getValue();
            assertThat(savedFcmToken.getMember()).isEqualTo(member);
            assertThat(savedFcmToken.getFcmToken()).isEqualTo(request.fcmToken());
            assertThat(savedFcmToken.getDeviceType()).isEqualTo(request.deviceType());
            assertThat(savedFcmToken.getDeviceIdentifier()).isEqualTo(request.deviceIdentifier());
            assertThat(savedFcmToken.getLastUsedAt()).isNotNull();
        }

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

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fcmTokenService.upsertTokenForMember(request, memberId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            then(fcmTokenRepository).should(never()).findByFcmToken(anyString());
            then(fcmTokenRepository).should(never()).findByMemberAndDeviceIdentifier(any(Member.class), anyString());
            then(fcmTokenRepository).should(never()).save(any(FcmToken.class));
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
                    .hasMessage(MemberErrorCode.MEMBER_FCM_TOKEN_NOT_FOUND.getMessage());

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
                    .hasMessage(MemberErrorCode.MEMBER_FCM_TOKEN_NOT_FOUND.getMessage());

            then(fcmTokenRepository).should(times(1)).findByFcmToken(token);
            then(fcmTokenRepository).should(never()).delete(any(FcmToken.class));
        }
    }

    @Nested
    @DisplayName("updateTokensAfterSend")
    class UpdateTokensAfterSend {

        @Test
        @DisplayName("성공 토큰은 최근 사용 시간을 갱신하고 실패 토큰은 삭제한다")
        void updateTokensAfterSend_success_updatesAndDeletes() {
            // given
            Set<String> successTokens = Set.of("success-1", "success-2");
            Set<String> failureTokens = Set.of("failure-1");
            given(fcmTokenRepository.updateLastUsedAtByFcmTokenIn(anySet(), any(LocalDateTime.class))).willReturn(successTokens.size());
            given(fcmTokenRepository.deleteByFcmTokenIn(failureTokens)).willReturn(failureTokens.size());

            // when
            fcmTokenService.updateTokensAfterSend(successTokens, failureTokens);

            // then
            then(fcmTokenRepository).should(times(1)).updateLastUsedAtByFcmTokenIn(eq(successTokens), any(LocalDateTime.class));
            then(fcmTokenRepository).should(times(1)).deleteByFcmTokenIn(eq(failureTokens));
        }

        @Test
        @DisplayName("전달된 토큰이 없으면 아무 작업도 수행하지 않는다")
        void updateTokensAfterSend_skip_whenTokensEmpty() {
            // given
            Set<String> successTokens = Set.of();
            Set<String> failureTokens = Set.of();

            // when
            fcmTokenService.updateTokensAfterSend(successTokens, failureTokens);

            // then
            then(fcmTokenRepository).should(never()).updateLastUsedAtByFcmTokenIn(anySet(), any(LocalDateTime.class));
            then(fcmTokenRepository).should(never()).deleteByFcmTokenIn(anySet());
        }
    }
}
