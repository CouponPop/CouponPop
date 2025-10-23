package com.sparta.couponpop.domain.store.repository;

import com.sparta.couponpop.domain.store.dto.response.StoreAndCouponEventCountProjection;
import com.sparta.couponpop.domain.store.dto.response.StoreLocationProjection;
import com.sparta.couponpop.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 삭제된 매장을 포함하여 ID로 매장을 조회합니다.
     * 매장 삭제 시 권한 검증을 위해 사용됩니다.
     *
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

    // TODO: 추후 개선 필요 (ES 등 외부 검색엔진 도입 검토)
    @Query(value = """
            SELECT 
                COUNT(DISTINCT subStore.id) AS openStoreCount,
                COUNT(ce.id) AS activeCouponEventCount
            FROM (
                SELECT 
                    s.id,
                    ST_Distance_Sphere(s.location, ST_SRID(POINT(:lng, :lat), 4326)) / 1000 AS distance,
                    CASE WHEN DAYOFWEEK(:nowTs) IN (1, 7) THEN s.weekend_open_time ELSE s.weekday_open_time END AS open_time,
                    CASE WHEN DAYOFWEEK(:nowTs) IN (1, 7) THEN s.weekend_close_time ELSE s.weekday_close_time END AS close_time
                FROM stores s
                WHERE s.deleted_at IS NULL
            ) AS subStore
            LEFT JOIN coupon_events ce 
                ON ce.store_id = subStore.id
                AND ce.coupon_event_status IN ('SCHEDULED', 'IN_PROGRESS')
                AND ce.total_count > ce.issued_count
            WHERE subStore.distance <= :radius
                AND subStore.open_time IS NOT NULL
                AND subStore.close_time IS NOT NULL
                AND (
                        -- 정상 구간(당일 마감)
                        (
                            subStore.open_time <= subStore.close_time
                            AND (
                                TIME(:nowTs) >= subStore.open_time
                                AND TIME(:nowTs) <  subStore.close_time
                            )
                        )
                        -- 심야 구간(익일 마감)
                        OR (
                            subStore.open_time > subStore.close_time
                            AND (
                                TIME(:nowTs) >= subStore.open_time
                                OR TIME(:nowTs) < subStore.close_time
                            )
                        )
                )
            """, nativeQuery = true)
    StoreAndCouponEventCountProjection findNearbyOpenStoreAndEventCount(@Param("lat") double latitude,
                                                                        @Param("lng") double longitude,
                                                                        @Param("radius") double radiusKm,
                                                                        @Param("nowTs") LocalDateTime nowTs);

    List<Store> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 매장명으로 매장을 검색합니다.
     * 삭제되지 않은 매장 중에서 매장명에 키워드가 포함된 매장을 조회합니다
     */
    @Query("SELECT s FROM Store s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.name ASC")
    List<Store> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
}