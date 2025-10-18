package com.sparta.couponpop.domain.member.repository;

import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.entity.MemberFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    Optional<MemberFcmToken> findByMemberAndDeviceIdentifier(Member member, String deviceIdentifier);

    Optional<MemberFcmToken> findByFcmToken(String fcmToken);

    List<MemberFcmToken> findByMemberAndNotificationEnabledIsTrue(Member member);
}
