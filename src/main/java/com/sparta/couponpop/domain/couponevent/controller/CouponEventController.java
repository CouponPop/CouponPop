package com.sparta.couponpop.domain.couponevent.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.StoreCouponEventListResponse;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.service.CouponEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    // TODO : 나중에 Store 도메인으로 옮기는 거 고려하기
    @GetMapping("/owner/stores/{storeId}/coupons/events")
    public ResponseEntity<ApiResponse<StoreCouponEventListResponse>> getCouponEventsByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "IN_PROGRESS", required = false) CouponEventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastStartAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastEndAt,
            @RequestParam(required = false) Long lastEventId,
            @RequestParam(defaultValue = "10") int size,
            @CurrentMember AuthMember authMember
    ) {
        StoreCouponEventsCursor cursor = StoreCouponEventsCursor.ofNullable(lastStartAt, lastEndAt, lastEventId);
        LocalDateTime now = LocalDateTime.now();
        StoreCouponEventListResponse response = couponEventService.getCouponEventsByStore(authMember.id(), storeId, status, now, cursor, size);
        return ApiResponse.success(response);
    }


}
