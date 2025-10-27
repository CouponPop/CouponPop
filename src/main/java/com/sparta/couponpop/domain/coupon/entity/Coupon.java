package com.sparta.couponpop.domain.coupon.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
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
    private Coupon(String couponCode, LocalDateTime receivedAt, LocalDateTime expireAt, LocalDateTime usedAt, CouponStatus couponStatus, CouponEvent couponEvent, Member member) {
        this.couponCode = couponCode;
        this.receivedAt = receivedAt;
        this.expireAt = expireAt;
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
                .expireAt(couponEvent.getEventEndAt())
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

    // 쿠폰이 만료되었는지
    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(this.expireAt);
    }

    public boolean isUsed() {
        return this.usedAt != null && CouponStatus.USED.equals(this.couponStatus);
    }

    public void use(LocalDateTime usedAt) {
        // 이미 사용된 쿠폰 방어 로직
        if (isUsed()) {
            throw new GlobalException(CouponErrorCode.COUPON_ALREADY_USED);
        }
        // 쿠폰 사용 가능 상태 검증 - AVAILABLE 이 아닌 USED, EXPIRED, CANCELED 이면 사용 못하는 쿠폰
        if (!isAvailable()) {
            throw new GlobalException(CouponErrorCode.COUPON_NOT_AVAILABLE);
        }
        // 만료 시간 검증
        if (isExpired(usedAt)) {
            throw new GlobalException(CouponErrorCode.COUPON_EXPIRED);
        }

        this.couponStatus = CouponStatus.USED;
        this.usedAt = usedAt;
    }


}
