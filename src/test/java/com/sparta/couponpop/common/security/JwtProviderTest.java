package com.sparta.couponpop.common.security;

import com.sparta.couponpop.domain.member.enums.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        String testBase64SecretKey = "7Yyd7Y+w7L+g7ZSE7Lqg7JuA67Cw64K07J28Lqg7JuA67Cw64K07J28";
        ReflectionTestUtils.setField(jwtProvider, "base64SecretKey", testBase64SecretKey);

        jwtProvider.init();
    }

    @Test
    @DisplayName("특정 사용자 정보로 토큰을 생성하고, 검증할 수 있다.")
    void createTokenAndValidateTokenSuccess() {

        // given
        Long userId = 1L;
        String username = "테스트이름";
        MemberType memberType = MemberType.OWNER;

        // when
        String accessToken = jwtProvider.createAccessToken(userId, username, memberType);
        Claims claims = jwtProvider.validateToken(accessToken);

        // when & then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("username", String.class)).isEqualTo(username);
        assertThat(claims.get("memberType", String.class)).isEqualTo(memberType.name());
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 예외가 발생한다.")
    void validateTokenFailureExpired() {

        // given
        Date expiration = new Date(System.currentTimeMillis() - 1000); // 1초 전 만료
        SecretKey secretKey = (SecretKey) ReflectionTestUtils.getField(jwtProvider, "secretKey");
        String expiredAccessToken = Jwts.builder()
                .subject("1")
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        // when & then
        assertThrows(ExpiredJwtException.class, () -> {
            jwtProvider.validateToken(expiredAccessToken);
        });
    }
}