package com.sparta.couponpop.domain.couponevent.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.service.CouponEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
