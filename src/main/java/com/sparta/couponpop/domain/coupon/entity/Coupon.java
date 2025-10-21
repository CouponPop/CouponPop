package com.sparta.couponpop.domain.coupon.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupons_member_coupon_event", columnNames = {"member_id", "coupon_event_id"}),
                @UniqueConstraint(name = "uk_coupons_coupon_code", columnNames = "coupon_code")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String couponCode;

    private LocalDateTime receivedAt;

    private LocalDateTime expireAt;

    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Builder(access = AccessLevel.PRIVATE)
    private Coupon(String couponCode, LocalDateTime receivedAt, LocalDateTime usedAt, CouponStatus couponStatus, CouponEvent couponEvent, Member member) {
        this.couponCode = couponCode;
        this.receivedAt = receivedAt;
        this.usedAt = usedAt;
        this.couponStatus = couponStatus;
        this.couponEvent = couponEvent;
        this.member = member;
    }

    public static Coupon createIssuedCoupon(Member member, CouponEvent couponEvent, LocalDateTime issuedTime) {
        String couponCode = generateCouponCode();
        return Coupon.builder()
                .couponCode(couponCode)
                .receivedAt(issuedTime)
                .couponStatus(CouponStatus.AVAILABLE)
                .couponEvent(couponEvent)
                .member(member)
                .build();
    }

    private static String generateCouponCode() {
        return "CPN-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public boolean isAvailable() {
        return CouponStatus.AVAILABLE.equals(this.couponStatus);
    }

    public boolean isUsed() {
        return CouponStatus.USED.equals(this.couponStatus);
    }
}
