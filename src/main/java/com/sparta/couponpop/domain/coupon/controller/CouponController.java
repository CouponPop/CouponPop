package com.sparta.couponpop.domain.coupon.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.coupon.dto.request.CouponIssueRequest;
import com.sparta.couponpop.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons/issue")
    public ResponseEntity<ApiResponse<Void>> issueCoupon(@Valid @RequestBody CouponIssueRequest request, @CurrentMember AuthMember authMember) {
        LocalDateTime issuedTime = LocalDateTime.now();
        couponService.issueEventCoupon(authMember.id(), request.storeId(), request.eventId(), issuedTime);
        return ApiResponse.noContent();
    }
}
