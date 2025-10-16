package com.sparta.couponpop.domain.store.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.createStore(request);

        return ApiResponse.success(storeResponse);
    }
}
