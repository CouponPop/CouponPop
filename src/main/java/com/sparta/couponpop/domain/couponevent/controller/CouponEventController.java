package com.sparta.couponpop.domain.couponevent.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
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
            @CurrentMember AuthMember authMember // TODO : 인증 세션 구현되면 변경
    ) {
        // TODO : 사장 권한 확인??
        CreateCouponEventResponse response = couponEventService.createCouponEvent(request, authMember.id());
        return ApiResponse.created(response);
    }

    @GetMapping("/owner/coupons/events/{eventId}")
    public ResponseEntity<ApiResponse<CouponEventDetailResponse>> getCouponEvent(@PathVariable Long eventId, @CurrentMember AuthMember authMember) {
        LocalDateTime now = LocalDateTime.now();
        CouponEventDetailResponse response = couponEventService.getCouponEvent(eventId, authMember.id(), now);
        return ApiResponse.success(response);
    }
}
