package com.sparta.couponpop.domain.store.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phone;

    private String description;

    private String businessNumber;

    private String address;

    private double latitude;

    private double longitude;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private StoreCategory storeCategory;

    private LocalTime weekdayOpenTime;

    private LocalTime weekdayCloseTime;

    private LocalTime weekendOpenTime;

    private LocalTime weekendCloseTime;

    @Column(updatable = false)
    private LocalDateTime deletedAt;
}
