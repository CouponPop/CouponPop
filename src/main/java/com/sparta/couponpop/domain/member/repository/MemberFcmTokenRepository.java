package com.sparta.couponpop.domain.member.repository;

import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    Optional<MemberFcmToken> findByMemberAndDeviceIdentifier(Member member, String deviceIdentifier);

    Optional<MemberFcmToken> findByFcmToken(String fcmToken);

    List<MemberFcmToken> findByMemberAndNotificationEnabledIsTrue(Member member);

    Optional<MemberFcmToken> findByMemberIdAndFcmToken(Long memberId, String fcmToken);

    /**
     * 다중 FCM 토큰의 lastUsedAt 일괄 업데이트
     *
     * @param fcmTokens 토큰 집합
     * @param now       현재 시간
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE MemberFcmToken t
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
                DELETE FROM MemberFcmToken t
                WHERE t.fcmToken IN :fcmTokens
            """)
    int deleteByFcmTokenIn(@Param("fcmTokens") Set<String> fcmTokens);
}
