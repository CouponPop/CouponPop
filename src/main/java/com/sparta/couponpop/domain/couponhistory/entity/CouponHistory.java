package com.sparta.couponpop.domain.couponhistory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouponHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long couponEventId;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private LocalDateTime eventStartAt;

    @Column(nullable = false)
    private LocalDateTime eventEndAt;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expireAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private String storeAddress;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponHistory(Long memberId, Long storeId, Long couponEventId, String memberName, String eventName, LocalDateTime eventStartAt, LocalDateTime eventEndAt, int totalCount, LocalDateTime issuedAt, LocalDateTime expireAt, LocalDateTime usedAt, String storeName, String storeAddress, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.storeId = storeId;
        this.couponEventId = couponEventId;
        this.memberName = memberName;
        this.eventName = eventName;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
        this.totalCount = totalCount;
        this.issuedAt = issuedAt;
        this.expireAt = expireAt;
        this.usedAt = usedAt;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.createdAt = createdAt;
    }

}
