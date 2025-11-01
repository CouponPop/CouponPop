package com.sparta.couponpop.domain.fcmtoken.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.fcmtoken.dto.request.FcmTokenRequest;
import com.sparta.couponpop.domain.fcmtoken.entity.FcmToken;
import com.sparta.couponpop.domain.fcmtoken.repository.FcmTokenRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void upsertTokenForMember(FcmTokenRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        final LocalDateTime now = LocalDateTime.now();

        // 중복 토큰 조회
        Optional<FcmToken> duplicatedToken = fcmTokenRepository.findByFcmToken(request.fcmToken());
        // 중복 토큰이 있다면 memberId, deviceIdentifier 갱신 후 return
        if (duplicatedToken.isPresent()) {
            duplicatedToken.get().updateMemberAndDeviceIdentifier(
                    member,
                    request.deviceIdentifier(),
                    now
            );

            log.debug("[FCM TOKEN] 중복 토큰 갱신: memberId={}, deviceIdentifier={}",
                    member.getId(), request.deviceIdentifier());

            return;
        }

        // 중복 토큰이 없다면 기존 토큰 조회 후 UPSERT
        fcmTokenRepository.findByMemberAndDeviceIdentifier(member, request.deviceIdentifier())
                .ifPresentOrElse(
                        activeToken -> {
                            activeToken.updateFcmToken(request.fcmToken(), now);
                            log.debug("[FCM TOKEN] 기존 토큰 갱신: memberId={}, deviceIdentifier={}, fcmToken={}",
                                    member.getId(), request.deviceIdentifier(), request.fcmToken());
                        },
                        () -> {
                            fcmTokenRepository.save(
                                    FcmToken.of(
                                            member,
                                            request.fcmToken(),
                                            request.deviceType(),
                                            request.deviceIdentifier(),
                                            true,
                                            now
                                    )
                            );
                            log.debug("[FCM TOKEN] 신규 토큰 저장: memberId={}, deviceIdentifier={}, fcmToken={}",
                                    member.getId(), request.deviceIdentifier(), request.fcmToken());
                        }
                );
    }

    // 단일 토큰 최근 사용 날짜(lastUsedAt) UPDATE
    @Transactional
    public void updateLastUsedAt(String fcmToken) {
        FcmToken fcmtoken = fcmTokenRepository.findByFcmToken(fcmToken)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_FCM_TOKEN_NOT_FOUND));

        fcmtoken.updateLastUsedAt(LocalDateTime.now());
    }

    // 단일 토큰 삭제
    @Transactional
    public void deleteToken(String token) {
        FcmToken fcmToken = fcmTokenRepository.findByFcmToken(token)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_FCM_TOKEN_NOT_FOUND));

        fcmTokenRepository.delete(fcmToken);
    }

    /**
     * FCM 전송 결과에 따라 성공 토큰은 업데이트, 실패 토큰은 삭제를 원자적으로 처리한다.
     */
    @Transactional
    public void updateTokensAfterSend(Set<String> successTokens, Set<String> failureTokens) {
        LocalDateTime now = LocalDateTime.now();

        if (successTokens != null && !successTokens.isEmpty()) {
            updateLastUsedAtInternal(successTokens, now);
        }

        if (failureTokens != null && !failureTokens.isEmpty()) {
            deleteTokensInternal(failureTokens);
        }
    }

    private void updateLastUsedAtInternal(Set<String> fcmTokens, LocalDateTime now) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        int updatedCount = fcmTokenRepository.updateLastUsedAtByFcmTokenIn(fcmTokens, now);
        log.info("FCM 성공 토큰 {}개 최근 사용 날짜 업데이트 완료", updatedCount);
    }

    private void deleteTokensInternal(Set<String> fcmTokens) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        int deletedCount = fcmTokenRepository.deleteByFcmTokenIn(fcmTokens);
        log.info("FCM 실패 토큰 {}개 삭제 완료", deletedCount);
    }
}
