package com.sparta.couponpop.domain.sample.controller;

import com.sparta.couponpop.common.response.ApiPageResponse;
import com.sparta.couponpop.common.response.ApiResponse;
import com.sparta.couponpop.domain.sample.dto.request.SampleRequest;
import com.sparta.couponpop.domain.sample.dto.response.SampleResponse;
import com.sparta.couponpop.domain.sample.exception.SampleErrorCode;
import com.sparta.couponpop.domain.sample.exception.SampleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class SampleController {

    @GetMapping("/page")
    public ResponseEntity<ApiPageResponse<SampleResponse>> getTestPage() {
        PageRequest pageRequest = PageRequest.of(0, 3);
        PageImpl<SampleResponse> userResponses = new PageImpl<>(
                List.of(
                        new SampleResponse("message", "data"),
                        new SampleResponse("message", "data"),
                        new SampleResponse("message", "data")
                ),
                pageRequest,
                pageRequest.getPageNumber()
        );
        return ApiPageResponse.success(userResponses);
    }

    @GetMapping("/api")
    public ResponseEntity<ApiResponse<SampleResponse>> getApi(@RequestBody SampleRequest request) {
        return ApiResponse.success(new SampleResponse("message", "data " + request.test()));
    }

    @GetMapping("/api-empty")
    public ResponseEntity<ApiResponse<SampleResponse>> getApiEmpty() {
        return ApiResponse.success(null);
    }

    @GetMapping("/error")
    public ResponseEntity<ApiResponse<SampleResponse>> getError() {
        throw new SampleException(SampleErrorCode.SAMPLE_ERROR_CODE);
    }
}
