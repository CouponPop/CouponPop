package com.sparta.couponpop.domain.store.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Store(String name, String phone, String description, String businessNumber,
                 String address, double latitude, double longitude, String imageUrl,
                 StoreCategory storeCategory, LocalTime weekdayOpenTime, LocalTime weekdayCloseTime,
                 LocalTime weekendOpenTime, LocalTime weekendCloseTime) {
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.businessNumber = businessNumber;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.storeCategory = storeCategory;
        this.weekdayOpenTime = weekdayOpenTime;
        this.weekdayCloseTime = weekdayCloseTime;
        this.weekendOpenTime = weekendOpenTime;
        this.weekendCloseTime = weekendCloseTime;
    }

    public static Store createStore(String name, String phone, String description, String businessNumber,
                                  String address, double latitude, double longitude, String imageUrl,
                                  StoreCategory storeCategory, LocalTime weekdayOpenTime, LocalTime weekdayCloseTime,
                                  LocalTime weekendOpenTime, LocalTime weekendCloseTime) {
        return Store.builder()
                .name(name)
                .phone(phone)
                .description(description)
                .businessNumber(businessNumber)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .imageUrl(imageUrl)
                .storeCategory(storeCategory)
                .weekdayOpenTime(weekdayOpenTime)
                .weekdayCloseTime(weekdayCloseTime)
                .weekendOpenTime(weekendOpenTime)
                .weekendCloseTime(weekendCloseTime)
                .build();
    }
}
