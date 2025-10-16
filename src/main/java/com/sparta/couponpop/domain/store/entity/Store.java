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

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "store_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreCategory storeCategory;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_number", nullable = false, length = 30)
    private String businessNumber;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "weekday_open_time", nullable = false)
    private LocalTime weekdayOpenTime;

    @Column(name = "weekday_close_time", nullable = false)
    private LocalTime weekdayCloseTime;

    @Column(name = "weekend_open_time", nullable = false)
    private LocalTime weekendOpenTime;

    @Column(name = "weekend_close_time", nullable = false)
    private LocalTime weekendCloseTime;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Store(Long memberId, String name, String phone, String description, String businessNumber,
                 String address, double latitude, double longitude, String imageUrl,
                 StoreCategory storeCategory, LocalTime weekdayOpenTime, LocalTime weekdayCloseTime,
                 LocalTime weekendOpenTime, LocalTime weekendCloseTime) {
        this.memberId = memberId;
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

    public static Store createStore(Long memberId,
                                  String name,
                                  String phone,
                                  String description,
                                  String businessNumber,
                                  String address,
                                  double latitude,
                                  double longitude,
                                  String imageUrl,
                                  StoreCategory storeCategory,
                                  LocalTime weekdayOpenTime,
                                  LocalTime weekdayCloseTime,
                                  LocalTime weekendOpenTime,
                                  LocalTime weekendCloseTime) {

        return Store.builder()
                .memberId(memberId)
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
