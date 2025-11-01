package com.sparta.couponpop.common.fcm.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.sparta.couponpop.common.fcm.request.FcmRequest;
import com.sparta.couponpop.common.fcm.service.FcmSendService;
import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

// TODO: 테스트용 컨트롤러, 추후 삭제 예정
@Slf4j
@RestController
@RequiredArgsConstructor
public class FcmTestController {

    private final FcmSendService fcmSendService;
    private final NotificationService notificationService;

    @PostMapping("/fcm/test")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(@RequestBody @Valid FcmRequest request) throws FirebaseMessagingException {
        log.debug("[+] 푸시 메세지 전송");

        // 일반 테스트
        fcmSendService.sendNotification(request.memberId(), request.token(), request.title(), request.body());

        return ApiResponse.noContent();
    }

    @PostMapping("/fcm/test2")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification() {
        log.debug("[+] 손님 쿠폰 수령 알림 푸시 메세지 전송");

        // 쿠폰 수령 알림 테스트
        CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                1L,
                "아이스아메리카노 1+1 쿠폰",
                "test-coupon-code",
                LocalDateTime.now()
        );
        notificationService.notifyCustomerCouponIssued(payload);

        return ApiResponse.noContent();
    }

}
