package com.sparta.couponpop.domain.store.repository;

import com.sparta.couponpop.domain.store.dto.response.StoreLocationProjection;
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
     * Native Query와 서브쿼리를 사용하여 거리 계산을 한 번만 수행합니다.
     * MySQL의 POINT 자료형과 ST_Distance_Sphere 함수를 사용하여 정확한 거리 계산을 수행합니다.
     */
    @Query(value = """
            SELECT 
                sub.id AS id,
                sub.name AS name,
                sub.address AS address,
                sub.store_category AS storeCategory,
                sub.latitude AS latitude,
                sub.longitude AS longitude,
                sub.image_url AS imageUrl,
                sub.distance AS distance
            FROM (
                SELECT 
                    s.id,
                    s.name,
                    s.address,
                    s.store_category,
                    s.latitude,
                    s.longitude,
                    s.image_url,
                    ST_Distance_Sphere(s.location, ST_SRID(POINT(:lng, :lat), 4326)) / 1000 AS distance
                FROM stores s
                WHERE s.deleted_at IS NULL
            ) AS sub
            WHERE sub.distance <= :radius
            ORDER BY sub.distance ASC
            """, nativeQuery = true)
    List<StoreLocationProjection> findByLocation(@Param("lat") double latitude,
                                              @Param("lng") double longitude,
                                              @Param("radius") double radiusKm);

    List<Store> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 매장명으로 매장을 검색합니다.
     * 삭제되지 않은 매장 중에서 매장명에 키워드가 포함된 매장을 조회합니다
     */
    @Query("SELECT s FROM Store s WHERE s.name LIKE %:keyword% ORDER BY s.name ASC")
    List<Store> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
}