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

    /**
     * 매장과 회원 정보를 함께 조회합니다.
     * N+1 문제를 방지하기 위해 fetch join을 사용합니다.
     */
    @Query("""
            SELECT s
            FROM Store s
            JOIN FETCH s.member m
            WHERE s.id = :storeId
            """)
    Optional<Store> findByIdWithMember(@Param("storeId") Long storeId);

    /**
     * 위치 기반으로 매장을 조회합니다.
     * 거리순으로 정렬하여 반환합니다.
     */
    @Query(value = """
            SELECT s.*, 
                   (6371 * acos(
                       cos(radians(:lat)) * cos(radians(s.latitude)) *
                       cos(radians(s.longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(s.latitude))
                   )) as distance
            FROM stores s
            HAVING distance <= :radius
            ORDER BY distance ASC
            """, nativeQuery = true)
    List<Object[]> findByLocation(@Param("lat") double latitude, 
                                @Param("lng") double longitude, 
                                @Param("radius") double radiusKm);

    List<Store> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}