package com.sparta.couponpop.domain.fcmtoken.service;

import com.sparta.couponpop.common.dto.member.response.GetMemberIdResponse;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.fcmtoken.dto.request.FcmTokenRequest;
import com.sparta.couponpop.domain.fcmtoken.entity.FcmToken;
import com.sparta.couponpop.domain.fcmtoken.exception.FcmTokenErrorCode;
import com.sparta.couponpop.domain.fcmtoken.repository.FcmTokenRepository;
import com.sparta.couponpop.domain.member.service.MemberInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final MemberInternalService memberInternalService;

    @Transactional
    public void upsertTokenForMember(FcmTokenRequest request, Long memberId) {
        GetMemberIdResponse getMemberIdResponse = memberInternalService.getMemberId(memberId);

        final LocalDateTime now = LocalDateTime.now();

        // 중복 [FCM Token] 조회
        Optional<FcmToken> duplicatedToken = fcmTokenRepository.findByFcmToken(request.fcmToken());

        // 중복 [FCM Token]이 있다면 해당 토큰의 [회원ID(memberId)]와 [기기 식별자(deviceIdentifier)] 갱신
        if (duplicatedToken.isPresent()) {
            duplicatedToken.get().updateMemberIdAndDeviceIdentifier(
                    getMemberIdResponse.memberId(),
                    request.deviceIdentifier(),
                    now
            );

            log.debug("[FCM TOKEN] 중복 토큰 갱신: memberId={}, deviceIdentifier={}",
                    getMemberIdResponse.memberId(), request.deviceIdentifier());

            return;
        }

        // 중복 [FCM Token]이 없다면 [회원ID(memberId) + 기기 식별자(deviceIdentifier)] 조회 후
        // 레코드가 존재한다면 [FCM Token]과 [최근 사용일(lastUsedAt)] 값을 갱신(UPDATE)
        // 레코드가 없다면 신규 저장(INSERT)
        fcmTokenRepository.findByMemberIdAndDeviceIdentifier(getMemberIdResponse.memberId(), request.deviceIdentifier())
                .ifPresentOrElse(
                        activeToken -> {
                            activeToken.updateFcmToken(request.fcmToken(), now);
                            log.debug("[FCM TOKEN] 기존 토큰 갱신: memberId={}, deviceIdentifier={}, fcmToken={}",
                                    getMemberIdResponse.memberId(), request.deviceIdentifier(), request.fcmToken());
                        },
                        () -> {
                            fcmTokenRepository.save(
                                    FcmToken.of(
                                            getMemberIdResponse.memberId(),
                                            request.fcmToken(),
                                            request.deviceType(),
                                            request.deviceIdentifier(),
                                            true,
                                            now
                                    )
                            );
                            log.debug("[FCM TOKEN] 신규 토큰 저장: memberId={}, deviceIdentifier={}, fcmToken={}",
                                    getMemberIdResponse.memberId(), request.deviceIdentifier(), request.fcmToken());
                        }
                );
    }

    // [FCM Token]의 [최근 사용일(lastUsedAt)] UPDATE
    @Transactional
    public void updateLastUsedAt(String fcmToken) {
        FcmToken fcmtoken = fcmTokenRepository.findByFcmToken(fcmToken)
                .orElseThrow(() -> new GlobalException(FcmTokenErrorCode.FCM_TOKEN_NOT_FOUND));

        fcmtoken.updateLastUsedAt(LocalDateTime.now());
    }

    // [FCM Token] 삭제
    @Transactional
    public void deleteToken(String token) {
        FcmToken fcmToken = fcmTokenRepository.findByFcmToken(token)
                .orElseThrow(() -> new GlobalException(FcmTokenErrorCode.FCM_TOKEN_NOT_FOUND));

        fcmTokenRepository.delete(fcmToken);
    }
}
