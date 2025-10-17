package com.sparta.couponpop.domain.couponevent.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.domain.couponevent.enums.CouponEventStatus;
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
}
