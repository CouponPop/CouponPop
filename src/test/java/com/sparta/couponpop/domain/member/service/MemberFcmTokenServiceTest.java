package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.dto.request.MemberFcmTokenRequest;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberFcmTokenServiceTest {

    @Mock
    private MemberFcmTokenRepository memberFcmTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberFcmTokenService memberFcmTokenService;

    @Nested
    @DisplayName("upsertTokenForMember")
    class UpsertTokenForMember {

        @Test
        @DisplayName("중복 토큰이 존재하면 회원과 기기 식별자를 갱신한다")
        void upsertTokenForMember_thenDuplicatedToken_updatesMemberAndDeviceIdentifier() {
            // given
            Long memberId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            MemberFcmTokenRequest request = MemberFcmTokenRequest.builder()
                    .fcmToken("fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();
            MemberFcmToken duplicatedToken = mock(MemberFcmToken.class);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.of(duplicatedToken));

            // when
            memberFcmTokenService.upsertTokenForMember(request, memberId);

            // then
            then(duplicatedToken).should(times(1)).updateMemberAndDeviceIdentifier(eq(member), eq(request.deviceIdentifier()), any(LocalDateTime.class));

            then(memberFcmTokenRepository).should(times(1)).findByFcmToken(request.fcmToken());
            then(memberFcmTokenRepository).should(never()).findByMemberAndDeviceIdentifier(any(Member.class), anyString());
            then(memberFcmTokenRepository).should(never()).save(any(MemberFcmToken.class));
        }

        @Test
        @DisplayName("기존 토큰이 존재하면 FCM 토큰을 갱신한다")
        void upsertTokenForMember_thenExistingToken_updatesFcmToken() {
            // given
            Long memberId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            MemberFcmTokenRequest request = MemberFcmTokenRequest.builder()
                    .fcmToken("new-fcm-token")
                    .deviceType("IOS")
                    .deviceIdentifier("device-123")
                    .build();
            MemberFcmToken activeToken = mock(MemberFcmToken.class);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.empty());
            given(memberFcmTokenRepository.findByMemberAndDeviceIdentifier(member, request.deviceIdentifier())).willReturn(Optional.of(activeToken));

            // when
            memberFcmTokenService.upsertTokenForMember(request, memberId);

            // then
            then(activeToken).should(times(1)).updateFcmToken(eq(request.fcmToken()), any(LocalDateTime.class));

            then(memberFcmTokenRepository).should(times(1)).findByFcmToken(request.fcmToken());
            then(memberFcmTokenRepository).should(times(1)).findByMemberAndDeviceIdentifier(member, request.deviceIdentifier());
            then(memberFcmTokenRepository).should(never()).save(any(MemberFcmToken.class));
        }

        @Test
        @DisplayName("토큰이 없다면 신규 토큰을 저장한다")
        void upsertTokenForMember_thenTokenNotExists_savesNewToken() {
            // given
            Long memberId = 1L;
            Member member = TestUtils.createEntity(Member.class, Map.of("id", memberId));
            MemberFcmTokenRequest request = MemberFcmTokenRequest.builder()
                    .fcmToken("fresh-fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberFcmTokenRepository.findByFcmToken(request.fcmToken())).willReturn(Optional.empty());
            given(memberFcmTokenRepository.findByMemberAndDeviceIdentifier(member, request.deviceIdentifier())).willReturn(Optional.empty());
            given(memberFcmTokenRepository.save(any(MemberFcmToken.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            memberFcmTokenService.upsertTokenForMember(request, memberId);

            // then
            ArgumentCaptor<MemberFcmToken> tokenCaptor = ArgumentCaptor.forClass(MemberFcmToken.class);
            then(memberFcmTokenRepository).should(times(1)).save(tokenCaptor.capture());

            MemberFcmToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getMember()).isEqualTo(member);
            assertThat(savedToken.getFcmToken()).isEqualTo(request.fcmToken());
            assertThat(savedToken.getDeviceType()).isEqualTo(request.deviceType());
            assertThat(savedToken.getDeviceIdentifier()).isEqualTo(request.deviceIdentifier());
            assertThat(savedToken.getLastUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외를 던진다")
        void upsertTokenForMember_thenMemberNotFound_throwsException() {
            // given
            Long memberId = 99L;
            MemberFcmTokenRequest request = MemberFcmTokenRequest.builder()
                    .fcmToken("sample-fcm-token")
                    .deviceType("ANDROID")
                    .deviceIdentifier("device-123")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberFcmTokenService.upsertTokenForMember(request, memberId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            then(memberFcmTokenRepository).should(never()).findByFcmToken(anyString());
            then(memberFcmTokenRepository).should(never()).findByMemberAndDeviceIdentifier(any(Member.class), anyString());
            then(memberFcmTokenRepository).should(never()).save(any(MemberFcmToken.class));
        }
    }
}
