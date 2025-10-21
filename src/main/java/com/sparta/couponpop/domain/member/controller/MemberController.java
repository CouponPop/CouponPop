package com.sparta.couponpop.domain.member.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.member.dto.request.MemberProfileUpdateRequest;
import com.sparta.couponpop.domain.member.dto.response.MemberProfileResponse;
import com.sparta.couponpop.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(@CurrentMember AuthMember authMember) {

        MemberProfileResponse response = memberService.getMemberProfile(authMember.id());
        return ApiResponse.success(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateProfile(
            @CurrentMember AuthMember authMember, @Valid @RequestBody MemberProfileUpdateRequest request) {

        MemberProfileResponse response = memberService.updateMemberProfile(authMember.id(), request);
        return ApiResponse.success(response);
    }
}
