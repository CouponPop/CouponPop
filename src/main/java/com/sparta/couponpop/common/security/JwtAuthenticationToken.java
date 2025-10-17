package com.sparta.couponpop.common.security;

import com.sparta.couponpop.common.security.dto.AuthMember;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthMember authMember;

    public JwtAuthenticationToken(AuthMember authMember) {
        super(List.of(authMember.role()));
        this.authMember = authMember;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authMember;
    }
}
