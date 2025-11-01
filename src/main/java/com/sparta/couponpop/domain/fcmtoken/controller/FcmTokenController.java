package com.sparta.couponpop.domain.fcmtoken.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.fcmtoken.dto.request.FcmTokenRequest;
import com.sparta.couponpop.domain.fcmtoken.service.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> upsertFcmToken(@RequestBody @Valid FcmTokenRequest request, @CurrentMember AuthMember authMember) {
        fcmTokenService.upsertTokenForMember(request, authMember.id());
        return ApiResponse.noContent();
    }

}
