package com.sparta.couponpop.domain.couponevent.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDateTime eventStartAt;

    @Column(nullable = false)
    private LocalDateTime eventEndAt;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int issuedCount;

    @Enumerated(EnumType.STRING)
    private CouponEventStatus couponEventStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    private CouponEvent(String name, LocalDateTime eventStartAt, LocalDateTime eventEndAt, int totalCount, Store store) {
        this.name = name;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
        this.totalCount = totalCount;
        this.store = store;
    }

    public static CouponEvent create(String name, LocalDateTime eventStartAt, LocalDateTime eventEndAt, int totalCount, Store store) {
        return CouponEvent.builder()
                .name(name)
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .totalCount(totalCount)
                .store(store)
                .build();
    }

    public void validateOwner(Long userId) {
        if (!store.getMember().getId().equals(userId)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_OWNER_MISMATCH);
        }
    }

    // 발급 가능 여부 검증
    public void validateIssuable(LocalDateTime now) {
        validateInProgress(now);

        if (issuedCount >= totalCount) {
            throw new GlobalException(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT);
        }
    }

    public void issueCoupon() {
        this.issuedCount += 1;
    }

    public void validateInProgress(LocalDateTime now) {
        if (now.isBefore(eventStartAt)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_NOT_STARTED);
        }
        if (now.isAfter(eventEndAt)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_ENDED);
        }
    }
}
