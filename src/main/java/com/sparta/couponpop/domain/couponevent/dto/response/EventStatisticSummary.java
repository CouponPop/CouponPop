package com.sparta.couponpop.domain.couponevent.dto.response;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EventStatisticSummary {
    private final int total; // 총 발급 수량
    private final int unclaimed; // 미수령 개수
    private final int issued; // 수령 개수
    private final int used; // 사용 개수
    private final int unused; // 미사용 개수

    @Builder
    public EventStatisticSummary(int total, int unclaimed, int issued, int used, int unused) {
        this.total = total;
        this.unclaimed = unclaimed;
        this.issued = issued;
        this.used = used;
        this.unused = unused;
    }

    public EventStatisticSummary(CouponEvent couponEvent, int usedCouponCount) {
        int totalCount = couponEvent.getTotalCount();
        int issuedCount = couponEvent.getIssuedCount();
        int unusedCount = issuedCount - usedCouponCount;
        int unclaimedCount = totalCount - issuedCount;

        this.total = totalCount;
        this.unclaimed = unclaimedCount;
        this.issued = couponEvent.getIssuedCount();
        this.used = usedCouponCount;
        this.unused = unusedCount;
    }
}
