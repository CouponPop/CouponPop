package com.sparta.couponpop.domain.fcmtoken.controller;

import com.sparta.couponpop.domain.fcmtoken.service.FcmTokenInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class FcmTokenInternalController {

    private final FcmTokenInternalService fcmTokenInternalService;

    @PostMapping("/v1/fcm-token/expire")
    public ResponseEntity<Void> expireFcmToken() {

        return ResponseEntity.ok().build();
    }
}
