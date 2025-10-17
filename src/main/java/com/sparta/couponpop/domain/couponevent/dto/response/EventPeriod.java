package com.sparta.couponpop.domain.couponevent.dto.response;

import java.time.LocalDateTime;

public record EventPeriod(
        LocalDateTime start,
        LocalDateTime end
) {
}
