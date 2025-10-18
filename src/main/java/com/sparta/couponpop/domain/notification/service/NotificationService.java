package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.util.DateTimeFormatters;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final FcmSendService fcmSendService;

    public void notifyCustomerCouponIssued(Long memberId, CouponIssuedNotificationPayload payload) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<MemberFcmToken> enabledTokens = memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member);
        if (enabledTokens.isEmpty()) {
            log.info("푸시 알림이 활성화된 FCM 토큰이 없어 알림을 건너뜁니다. memberId={}", memberId);
            return;
        }

        List<String> tokens = enabledTokens.stream()
                .map(MemberFcmToken::getFcmToken)
                .collect(Collectors.toList());

        String title = "쿠폰 수령이 완료되었습니다!";
        String body = """
                쿠폰명: %s
                쿠폰코드: %s
                만료기간: %s
                """.formatted(
                payload.couponName(),
                payload.couponCode(),
                payload.expireAt().format(DateTimeFormatters.KOREAN_DATE_TIME)
        );

        try {
            fcmSendService.sendNotification(tokens, title, body);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 전송 중 오류가 발생했습니다. tokens={}, message={}", tokens, e.getMessage(), e);
        }
    }

}
