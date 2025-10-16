package com.sparta.couponpop.domain.couponevent.repository;

import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
}
