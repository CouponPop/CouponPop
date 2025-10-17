package com.sparta.couponpop.domain.member.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.dto.request.MemberFcmTokenRequest;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberFcmTokenService {

    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void upsertTokenForMember(MemberFcmTokenRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        final LocalDateTime now = LocalDateTime.now();

        // 중복 토큰 조회
        Optional<MemberFcmToken> duplicatedToken = memberFcmTokenRepository.findByFcmToken(request.fcmToken());
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
        Optional<MemberFcmToken> activeToken = memberFcmTokenRepository.findByMemberAndDeviceIdentifier(member, request.deviceIdentifier());
        if (activeToken.isPresent()) {
            // 존재한다면 기존 토큰 갱신
            activeToken.get().updateFcmToken(
                    request.fcmToken(),
                    now
            );

            log.debug("[FCM TOKEN] 기존 토큰 갱신: memberId={}, deviceIdentifier={}, fcmToken={}",
                    member.getId(), request.deviceIdentifier(), request.fcmToken());

        } else {
            // 존재하지 않는다면 신규 토큰 저장
            memberFcmTokenRepository.save(
                    MemberFcmToken.of(
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
    }
}
