package com.sparta.couponpop.domain.notification.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.fcm.service.FcmSendService;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberFcmTokenRepository;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.notification.constants.NotificationTemplates;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.dto.payload.CouponUsedNotificationPayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class NotificationService {

    private final MemberRepository memberRepository;
    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final FcmSendService fcmSendService;

    /**
     * 쿠폰 사용한 손님에게 푸시 알림 전송
     */
    public void send(@Valid CouponUsedNotificationPayload payload) {

        List<String> tokens = getTokensForMember(payload.customerId());

        String title = NotificationTemplates.COUPON_USED_TITLE.formatted(payload.couponName());
        String body = NotificationTemplates.COUPON_USED_BODY.formatted(
                payload.couponName(),
                payload.storeName()
        );

        for (String token : tokens) {
            fcmSendService.sendNotification(payload.customerId(), token, title, body)
                    .exceptionally(throwable -> {
                        log.error("쿠폰 사용 알림 전송 중 오류가 발생했습니다. token={}, message={}", token, throwable.getMessage(), throwable);
                        return null;
                    });
        }
    }

    /**
     * 손님 쿠폰 수령 시 사장님 푸시 알림 전송
     */
    public void send(@Valid CouponIssuedNotificationPayload payload) {

        List<String> tokens = getTokensForMember(payload.ownerId());

        String title = NotificationTemplates.COUPON_ISSUED_TITLE.formatted(payload.couponName());
        String body = NotificationTemplates.COUPON_ISSUED_BODY.formatted(
                payload.couponName(),
                payload.totalCount(),
                payload.issuedCount()
        );

        for (String token : tokens) {
            fcmSendService.sendNotification(payload.ownerId(), token, title, body)
                    .exceptionally(throwable -> {
                        log.error("쿠폰 수령 알림 전송 중 오류가 발생했습니다. token={}, message={}", token, throwable.getMessage(), throwable);
                        return null;
                    });
        }
    }

    private List<String> getTokensForMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<MemberFcmToken> enabledTokens = memberFcmTokenRepository.findByMemberAndNotificationEnabledIsTrue(member);
        if (enabledTokens.isEmpty()) {
            log.info("푸시 알림이 활성화된 FCM 토큰이 없어 알림을 건너뜁니다. ownerId={}", member.getId());
            return List.of();
        }

        // 토큰 중복 제거
        return enabledTokens.stream()
                .map(MemberFcmToken::getFcmToken)
                .distinct()
                .toList();
    }
}
