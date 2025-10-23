package com.sparta.couponpop.domain.coupon.dto.response;

import com.sparta.couponpop.domain.coupon.dto.request.MemberIssuedCouponCursor;
import com.sparta.couponpop.domain.coupon.repository.dto.CouponSummaryInfoProjection;

import java.util.ArrayList;
import java.util.List;

public record IssuedCouponListResponse(
        List<CouponSummaryInfoProjection> coupons,
        MemberIssuedCouponCursor nextCursor,
        int size,
        boolean hasNext
) {
    public static IssuedCouponListResponse of(List<CouponSummaryInfoProjection> originalCoupons, int pageSize) {
        if (originalCoupons == null || originalCoupons.isEmpty()) {
            return new IssuedCouponListResponse(List.of(), null, 0, false);
        }

        boolean hasNext = originalCoupons.size() > pageSize;
        List<CouponSummaryInfoProjection> coupons = trimToPageSize(originalCoupons, pageSize);
        MemberIssuedCouponCursor nextCursor = hasNext ? buildNextCursor(coupons) : null;

        return new IssuedCouponListResponse(List.copyOf(coupons), nextCursor, coupons.size(), hasNext);
    }

    private static List<CouponSummaryInfoProjection> trimToPageSize(List<CouponSummaryInfoProjection> coupons, int pageSize) {
        if (coupons.size() <= pageSize) {
            return coupons;
        }
        // 원본 리스트 변형 방지를 위해 복사 후 제거
        List<CouponSummaryInfoProjection> trimmed = new ArrayList<>(coupons);
        trimmed.remove(trimmed.size() - 1);
        return trimmed;
    }

    private static MemberIssuedCouponCursor buildNextCursor(List<CouponSummaryInfoProjection> coupons) {
        if (coupons.isEmpty()) {
            return null;
        }

        CouponSummaryInfoProjection lastCoupon = coupons.get(coupons.size() - 1);
        return new MemberIssuedCouponCursor(
                lastCoupon.event().period().end(),
                lastCoupon.id()
        );
    }

}
