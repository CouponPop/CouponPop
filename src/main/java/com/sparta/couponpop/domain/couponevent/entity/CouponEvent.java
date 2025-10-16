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

    private String name;

    private LocalDateTime eventStartAt;

    private LocalDateTime eventEndAt;

    private int totalCount;

    private int issuedCount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'SCHEDULED'")
    private CouponEventStatus couponEventStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
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
}
