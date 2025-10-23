package com.sparta.couponpop.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(

        @NotBlank
        String fcmToken
) {
}
