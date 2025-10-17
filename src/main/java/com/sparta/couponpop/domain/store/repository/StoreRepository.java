package com.sparta.couponpop.domain.store.repository;

import com.sparta.couponpop.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    
    List<Store> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}

