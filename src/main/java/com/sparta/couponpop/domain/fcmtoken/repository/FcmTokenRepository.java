package com.sparta.couponpop.domain.fcmtoken.repository;

import com.sparta.couponpop.domain.fcmtoken.entity.FcmToken;
import com.sparta.couponpop.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByMemberAndDeviceIdentifier(Member member, String deviceIdentifier);

    Optional<FcmToken> findByFcmToken(String fcmToken);

    List<FcmToken> findByMemberAndNotificationEnabledIsTrue(Member member);

    Optional<FcmToken> findByMemberIdAndFcmToken(Long memberId, String fcmToken);

    /**
     * 다중 FCM 토큰의 lastUsedAt 일괄 업데이트
     *
     * @param fcmTokens 토큰 집합
     * @param now       현재 시간
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE FcmToken t
                SET t.lastUsedAt = :now
                WHERE t.fcmToken IN :fcmTokens
            """)
    int updateLastUsedAtByFcmTokenIn(@Param("fcmTokens") Set<String> fcmTokens,
                                     @Param("now") LocalDateTime now);

    /**
     * 다중 FCM 토큰의 일괄 삭제
     *
     * @param fcmTokens 토큰 집합
     * @return 삭제된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("""
                DELETE FROM FcmToken t
                WHERE t.fcmToken IN :fcmTokens
            """)
    int deleteByFcmTokenIn(@Param("fcmTokens") Set<String> fcmTokens);
}
