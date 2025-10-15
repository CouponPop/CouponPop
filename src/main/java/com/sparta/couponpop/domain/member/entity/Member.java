package com.sparta.couponpop.domain.member.entity;

import com.sparta.couponpop.common.entity.BaseEntity;
import com.sparta.couponpop.domain.member.enums.MemberType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String username;

    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PROTECTED)
    public Member(String email, String username, String password, String phoneNumber, MemberType memberType) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.memberType = memberType;
    }

    public static Member signUp(String email, String username, String password, String phoneNumber, MemberType memberType) {
        return Member.builder()
                .email(email)
                .username(username)
                .password(password)
                .phoneNumber(phoneNumber)
                .memberType(memberType)
                .build();
    }
}
