package com.sparta.couponpop.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.fcm.service.FcmSendService;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.constants.NotificationTemplates;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.enums.NotificationType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class NotificationService {

    private final MemberRepository memberRepository;
    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final FcmSendService fcmSendService;

    public void notifyCustomerCouponIssued(Long memberId, @Valid CouponIssuedNotificationPayload payload) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<MemberFcmToken> enabledTokens = memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member);
        if (enabledTokens.isEmpty()) {
            log.info("푸시 알림이 활성화된 FCM 토큰이 없어 알림을 건너뜁니다. memberId={}", memberId);
            return;
        }

        List<String> tokens = enabledTokens.stream()
                .map(MemberFcmToken::getFcmToken)
                .distinct()
                .collect(Collectors.toList());

        String title = NotificationTemplates.COUPON_ISSUED_TITLE;
        String body = NotificationTemplates.COUPON_ISSUED_BODY.formatted(
                payload.couponName(),
                payload.couponCode(),
                payload.expireAt()
        );

        try {
            fcmSendService.sendNotification(member.getId(), tokens, title, body);
        } catch (FirebaseMessagingException e) {
            log.error("{} 전송 중 오류가 발생했습니다. tokens={}, message={}", NotificationType.COUPON_ISSUED, tokens, e.getMessage(), e);
        }
    }
}
