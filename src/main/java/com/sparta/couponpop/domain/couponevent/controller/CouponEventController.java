package com.sparta.couponpop.domain.couponevent.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.service.CouponEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouponEventController {

    private final CouponEventService couponEventService;

    @PostMapping("/owner/coupons/events")
    public ResponseEntity<ApiResponse<CreateCouponEventResponse>> createCouponEvent(
            @RequestBody @Valid CreateCouponEventRequest request,
            @AuthenticationPrincipal Long userId // TODO : 인증 세션 구현되면 변경
    ) {
        // TODO : 사장 권한 확인??
        CreateCouponEventResponse response = couponEventService.createCouponEvent(request, userId);
        return ApiResponse.created(response);
    }

    @GetMapping("/owner/coupons/events/{eventId}")
    public ResponseEntity<ApiResponse<CouponEventDetailResponse>> getCouponEvent(@PathVariable Long eventId, @AuthenticationPrincipal Long loginUerId) {
        LocalDateTime now = LocalDateTime.now();
        CouponEventDetailResponse response = couponEventService.getCouponEvent(eventId, loginUerId, now);
        return ApiResponse.success(response);
    }
}
