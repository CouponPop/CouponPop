package com.sparta.couponpop.domain.member.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String fcmToken;

    private String deviceType;

    private String deviceIdentifier;

    private boolean notificationEnabled;

    private LocalDateTime lastUsedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberFcmToken(Member member,
                           String fcmToken,
                           String deviceType,
                           String deviceIdentifier,
                           boolean notificationEnabled,
                           LocalDateTime lastUsedAt) {
        this.member = member;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.deviceIdentifier = deviceIdentifier;
        this.notificationEnabled = notificationEnabled;
        this.lastUsedAt = lastUsedAt;
    }

    public static MemberFcmToken of(Member member,
                                    String fcmToken,
                                    String deviceType,
                                    String deviceIdentifier,
                                    boolean notificationEnabled,
                                    LocalDateTime lastUsedAt) {
        return MemberFcmToken.builder()
                .member(member)
                .fcmToken(fcmToken)
                .deviceType(deviceType)
                .deviceIdentifier(deviceIdentifier)
                .notificationEnabled(notificationEnabled)
                .lastUsedAt(lastUsedAt)
                .build();
    }

    public void updateFcmToken(String fcmToken, LocalDateTime lastUsedAt) {
        this.fcmToken = fcmToken;
        this.lastUsedAt = lastUsedAt;
    }

    public void updateMemberAndDeviceIdentifier(Member member, String deviceIdentifier, LocalDateTime lastUsedAt) {
        this.member = member;
        this.deviceIdentifier = deviceIdentifier;
        this.lastUsedAt = lastUsedAt;
    }

}
