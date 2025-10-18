package com.sparta.couponpop.domain.auth.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.auth.dto.request.LoginRequest;
import com.sparta.couponpop.domain.auth.dto.request.SignUpRequest;
import com.sparta.couponpop.domain.auth.dto.response.LoginResponse;
import com.sparta.couponpop.domain.auth.dto.response.SignUpResponse;
import com.sparta.couponpop.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {

        SignUpResponse signUpResponse = authService.signUp(signUpRequest);
        return ApiResponse.created(signUpResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse loginResponse = authService.login(loginRequest);
        return ApiResponse.success(loginResponse);
    }

    // TODO: 로그아웃 시 FCM 토큰 삭제 기능 추가 예정
    // TODO: https://github.com/orgs/CouponPop/projects/1/views/2?pane=issue&itemId=134369719&issue=CouponPop%7CCouponPop%7C61
}
