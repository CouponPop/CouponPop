package com.sparta.couponpop.domain.member.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.member.dto.request.MemberFcmTokenRequest;
import com.sparta.couponpop.domain.member.service.MemberFcmTokenService;
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
public class MemberFcmTokenController {

    private final MemberFcmTokenService memberFcmTokenService;

    @PostMapping("/members/fcm-token")
    public ResponseEntity<ApiResponse<Void>> upsertMemberFcmToken(@RequestBody @Valid MemberFcmTokenRequest request, @CurrentMember AuthMember authMember) {
        memberFcmTokenService.upsertTokenForMember(request, authMember.id());
        return ApiResponse.noContent();
    }

}
