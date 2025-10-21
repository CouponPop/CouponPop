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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()") // auth는 모두 접근이므로, 로그아웃은 인증된 사용자만 접근 가능
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {

        authService.logout(authorizationHeader);
        return ApiResponse.noContent();
    }

    // TODO: 로그아웃 시 FCM 토큰 삭제 기능 추가 예정
    // TODO: https://github.com/orgs/CouponPop/projects/1/views/2?pane=issue&itemId=134369719&issue=CouponPop%7CCouponPop%7C61
}
