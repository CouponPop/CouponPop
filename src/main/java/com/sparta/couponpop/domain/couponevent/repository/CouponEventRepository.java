package com.sparta.couponpop.domain.couponevent.repository;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long>, CouponEventQueryRepository {

    @Query("""
            SELECT e
            FROM CouponEvent e
            JOIN FETCH e.store s
            JOIN FETCH s.member m
            WHERE e.id = :eventId
            """)
    Optional<CouponEvent> findByIdWithStoreAndMember(@Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CouponEvent e where e.id = :eventId")
    Optional<CouponEvent> findEventForUpdate(@Param("eventId") Long eventId);
}
