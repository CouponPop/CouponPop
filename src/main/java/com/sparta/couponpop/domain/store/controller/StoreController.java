package com.sparta.couponpop.domain.store.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores(@CurrentMember AuthMember authMember) {

        List<StoreResponse> storeResponses = storeService.getStoresByOwner(authMember.id());

        return ApiResponse.success(storeResponses);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@CurrentMember AuthMember authMember, @RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.createStore(authMember.id(), request);

        return ApiResponse.created(storeResponse);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(@CurrentMember AuthMember authMember, @PathVariable Long storeId, @RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.updateStore(storeId, authMember.id(), request);

        return ApiResponse.success(storeResponse);
    }
}
