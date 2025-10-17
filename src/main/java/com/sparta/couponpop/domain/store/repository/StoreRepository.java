package com.sparta.couponpop.domain.store.repository;

import com.sparta.couponpop.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 삭제된 매장을 포함하여 ID로 매장을 조회합니다.
     * 매장 삭제 시 권한 검증을 위해 사용됩니다.
     * @SQLRestriction을 무시하고 모든 매장을 조회합니다.
     */
    @Query(value = "SELECT * FROM stores WHERE id = :storeId", nativeQuery = true)
    Optional<Store> findByIdIncludingDeleted(@Param("storeId") Long storeId);

    List<Store> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}

