package com.sparta.couponpop.domain.location.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.location.dto.request.CreateLocationRequest;
import com.sparta.couponpop.domain.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/api/v1/locations/current")
    public ResponseEntity<ApiResponse<Void>> createLocation(@RequestBody @Valid CreateLocationRequest request,
                                                            @CurrentMember AuthMember authMember) {
        locationService.cacheLocation(request, authMember.id());
        return ApiResponse.noContent();
    }

}
