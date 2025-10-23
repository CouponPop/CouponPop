package com.sparta.couponpop.domain.coupon.repository;

import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.repository.dto.CouponSummaryInfoProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponQueryRepository {

    List<CouponSummaryInfoProjection> findAllByMemberIdWithEventAndStore(Long memberId, CouponStatus status, LocalDateTime lastEventEndAt, Long lastCouponId, int limit);
}
