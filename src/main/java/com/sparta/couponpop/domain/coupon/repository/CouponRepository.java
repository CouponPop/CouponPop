package com.sparta.couponpop.domain.coupon.repository;

import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("""
            select count(c)
            from Coupon c
            where c.couponEvent.id = :eventId and c.couponStatus = :status
            """)
    int countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") CouponStatus status);

    boolean existsByMemberIdAndCouponEventId(Long memberId, Long eventId);
}
