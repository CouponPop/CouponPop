package com.sparta.couponpop.domain.coupon.repository;

import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponQueryRepository {

    @Query("""
            select count(c)
            from Coupon c
            where c.couponEvent.id = :eventId and c.couponStatus = :status
            """)
    int countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") CouponStatus status);

    boolean existsByMemberIdAndCouponEventId(Long memberId, Long eventId);

    @Query("""
            select c
            from Coupon c
                join fetch c.couponEvent ce
                join fetch ce.store s
            where c.id = :couponId
            """)
    Optional<Coupon> findByIdWithCouponEventAndStore(@Param("couponId") Long couponId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from Coupon c
                join fetch c.couponEvent ce
            where c.id = :couponId
            """)
    Optional<Coupon> findByIdWithCouponEventForUpdate(@Param("couponId") Long couponId);

}
