package com.sparta.couponpop.domain.coupon.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.domain.coupon.enums.CouponEventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private LocalDateTime eventStartDateTime;

    private LocalDateTime eventEndDateTime;

    private LocalDateTime couponExpireDateTime;

    private int totalCount;

    private int issuedCount;

    @Enumerated(EnumType.STRING)
    private CouponEventStatus couponEventStatus;

}
