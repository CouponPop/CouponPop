package com.sparta.couponpop.domain.notification.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.notification.dto.request.FcmRequest;
import com.sparta.couponpop.domain.notification.service.FcmTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FcmTestController {

    private final FcmTestService fcmTestService;

    @PostMapping("/fcm/test")
    public ResponseEntity<ApiResponse<Integer>> sendTestNotification(@RequestBody @Valid FcmRequest request) {
        log.debug("[+] 푸시 메세지 전송");
        int response = fcmTestService.sendTestNotification(request);
        return ApiResponse.success(response);
    }

}
