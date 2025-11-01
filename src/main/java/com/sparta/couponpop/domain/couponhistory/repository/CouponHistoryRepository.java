package com.sparta.couponpop.domain.couponhistory.repository;

import com.sparta.couponpop.domain.couponhistory.entity.CouponHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponHistoryRepository extends JpaRepository<CouponHistory, Long> {
}
