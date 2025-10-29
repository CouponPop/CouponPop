package com.sparta.couponpop.domain.coupon.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.coupon.dto.request.CouponIssueRequest;
import com.sparta.couponpop.domain.coupon.dto.request.MemberIssuedCouponCursor;
import com.sparta.couponpop.domain.coupon.dto.request.UseCouponRequest;
import com.sparta.couponpop.domain.coupon.dto.response.CouponDetailResponse;
import com.sparta.couponpop.domain.coupon.dto.response.IssuedCouponListResponse;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.service.CouponIssueService;
import com.sparta.couponpop.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CouponController {

    private final CouponService couponService;
    private final CouponIssueService couponIssueService;

    @PostMapping("/coupons/issue")
    public ResponseEntity<ApiResponse<Void>> issueCoupon(@Valid @RequestBody CouponIssueRequest request, @CurrentMember AuthMember authMember) {
        LocalDateTime issuedTime = LocalDateTime.now();
        couponIssueService.issueCoupon(authMember.id(), request.eventId(), issuedTime);
        return ApiResponse.noContent();
    }

    @GetMapping("/coupons/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCouponDetail(@PathVariable Long couponId, @CurrentMember AuthMember authMember) {
        CouponDetailResponse response = couponService.getCouponDetail(couponId, authMember.id());
        return ApiResponse.success(response);
    }

    @PostMapping("/coupons/use")
    public ResponseEntity<ApiResponse<Void>> useCoupon(@Valid @RequestBody UseCouponRequest request, @CurrentMember AuthMember authMember) {
        LocalDateTime usedAt = LocalDateTime.now();
        couponService.useCoupon(request.couponId(), request.qrCode(), authMember.id(), usedAt);
        return ApiResponse.noContent();
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<IssuedCouponListResponse>> getIssuedCoupons(
            @RequestParam CouponStatus type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastEventEndAt,
            @RequestParam(required = false) Long lastCouponId,
            @RequestParam(defaultValue = "10") int size,
            @CurrentMember AuthMember authMember
    ) {
        var cursor = MemberIssuedCouponCursor.ofNullable(lastEventEndAt, lastCouponId);
        IssuedCouponListResponse response = couponService.getIssuedCoupons(authMember.id(), type, cursor, size);
        return ApiResponse.success(response);
    }
}
