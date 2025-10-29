package com.sparta.couponpop.domain.store.controller;

import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.common.security.annotation.CurrentMember;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreDetailResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreMapResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.service.StoreIndexInitService;
import com.sparta.couponpop.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StoreIndexInitService storeIndexInitService;

    @GetMapping("/owner/stores")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores(@CurrentMember AuthMember authMember) {

        List<StoreResponse> storeResponses = storeService.getStoresByOwner(authMember.id());

        return ApiResponse.success(storeResponses);
    }

    @PostMapping("/owner/stores")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@CurrentMember AuthMember authMember, @RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.createStore(authMember.id(), request);

        return ApiResponse.created(storeResponse);
    }

    @PutMapping("/owner/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(@CurrentMember AuthMember authMember, @PathVariable Long storeId, @RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.updateStore(storeId, authMember.id(), request);

        return ApiResponse.success(storeResponse);
    }

    @DeleteMapping("/owner/stores/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@CurrentMember AuthMember authMember, @PathVariable Long storeId) {

        storeService.deleteStore(storeId, authMember.id());

        return ApiResponse.noContent();
    }

    @GetMapping("/owner/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(@CurrentMember AuthMember authMember, @PathVariable Long storeId) {

        StoreDetailResponse response = storeService.getStoreDetail(storeId, authMember.id());

        return ApiResponse.success(response);
    }

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<List<StoreMapResponse>>> getStoresByLocation(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {

        List<StoreMapResponse> stores = storeService.getStoresByLocation(lat, lng, radius);

        return ApiResponse.success(stores);
    }

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetailForCustomer(@PathVariable Long storeId) {

        StoreDetailResponse response = storeService.getStoreDetailForCustomer(storeId);

        return ApiResponse.success(response);
    }

    @GetMapping("/stores/search")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> searchStores(@RequestParam String keyword) {

        List<StoreResponse> stores = storeService.searchStoresByName(keyword);

        return ApiResponse.success(stores);
    }

    /**
     * Elasticsearch 재색인 (관리자 전용 - 실제로는 권한 체크 필요)
     * 개발/배포 시 기존 데이터를 ES에 동기화할 때 사용
     */
    @PostMapping("/admin/stores/reindex")
    public ResponseEntity<ApiResponse<String>> reindexStores() {
        storeIndexInitService.fullReindex();
        return ApiResponse.success("Store reindexing completed successfully");
    }
}
