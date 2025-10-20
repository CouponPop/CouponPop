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
    @ColumnDefault("'SCHEDULED'")
    @Column(nullable = false)
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
        this.couponEventStatus = CouponEventStatus.SCHEDULED;
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

    /**
     * 현재 시간 기준의 동적 상태 계산
     * 단, CANCELED 상태는 그대로 유지
     *
     * @param now
     */
    public CouponEventStatus getCurrentStatus(LocalDateTime now) {
        if (CouponEventStatus.CANCELED.equals(this.couponEventStatus)) {
            return CouponEventStatus.CANCELED;
        }

        if (now.isBefore(eventStartAt)) {
            return CouponEventStatus.SCHEDULED;
        }
        if (now.isAfter(eventEndAt)) {
            return CouponEventStatus.COMPLETED;
        }
        return CouponEventStatus.IN_PROGRESS;
    }

    public void validateOwner(Long userId) {
        if (!store.getMember().getId().equals(userId)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_OWNER_MISMATCH);
        }
    }

    // 발급 가능 여부 검증
    public void validateIssuable(LocalDateTime now) {
        if (couponEventStatus != CouponEventStatus.IN_PROGRESS) {
            throw new GlobalException(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS);
        }
        if (now.isBefore(eventStartAt) || now.isAfter(eventEndAt)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS_TIME);
        }
        if (issuedCount >= totalCount) {
            throw new GlobalException(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT);
        }
    }

    public void issue() {
        this.issuedCount += 1;
    }
}
