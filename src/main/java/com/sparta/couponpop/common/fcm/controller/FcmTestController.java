package com.sparta.couponpop.common.fcm.controller;

import com.sparta.couponpop.common.fcm.request.FcmRequest;
import com.sparta.couponpop.common.fcm.service.FcmSendService;
import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.notification.dto.payload.CouponIssuedNotificationPayload;
import com.sparta.couponpop.domain.notification.dto.payload.CouponUsedNotificationPayload;
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
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(@RequestBody @Valid FcmRequest request) {
        log.info("[+] 푸시 메세지 전송");

        // 일반 테스트
        fcmSendService.sendNotification(request.memberId(), request.token(), request.title(), request.body())
                .exceptionally(throwable -> {
                    log.error("테스트 푸시 전송 중 오류가 발생했습니다. token={}, message={}", request.token(), throwable.getMessage(), throwable);
                    return null;
                });

        return ApiResponse.noContent();
    }

    @PostMapping("/fcm/test2")
    public ResponseEntity<ApiResponse<Void>> sendTestOwnerNotification() {
        log.info("[+] 손님 쿠폰 수령 시 사장님 알림 푸시 메세지 전송");

        CouponIssuedNotificationPayload payload = CouponIssuedNotificationPayload.of(
                1L,
                "아이스아메리카노 1+1",
                30,
                15
        );
        notificationService.send(payload);

        return ApiResponse.noContent();
    }

    @PostMapping("/fcm/test3")
    public ResponseEntity<ApiResponse<Void>> sendTestCustomerNotification() {
        log.info("[+] 쿠폰 사용한 손님에게 푸시 알림 메세지 전송");

        CouponUsedNotificationPayload payload = CouponUsedNotificationPayload.of(
                1L,
                "아이스아메리카노 1+1",
                "스타벅스 강남역점",
                LocalDateTime.now()
        );
        notificationService.send(payload);

        return ApiResponse.noContent();
    }

}
