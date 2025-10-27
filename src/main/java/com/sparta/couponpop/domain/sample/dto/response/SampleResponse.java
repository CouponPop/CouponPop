package com.sparta.couponpop.domain.sample.dto.response;

import lombok.Builder;

@Builder
public record SampleResponse(
        String message,
        String data
) {
}
