package com.sparta.couponpop.common.security.dto;

import com.sparta.couponpop.domain.member.enums.MemberType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthMember(
        Long id,
        String username,
        MemberType memberType,
        GrantedAuthority role
) {
    public static AuthMember from(Long userId, String username, MemberType memberType) {

        // "ROLE_" 이라는 Prefix가 추가된 roleName 필드 사용
        GrantedAuthority role = new SimpleGrantedAuthority(memberType.roleName());
        return new AuthMember(userId, username, memberType, role);
    }
}
