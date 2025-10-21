package com.sparta.couponpop.common.security;

import com.sparta.couponpop.domain.member.enums.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtProvider {

    private static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L; // 1시간
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret.key}")
    private String base64SecretKey;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(base64SecretKey);
        this.secretKey = Keys.hmacShaKeyFor(bytes); // 미리 암호화
    }

    public String createAccessToken(Long userId, String username, MemberType memberType) {

        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("memberType", memberType.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String resolveToken(String authorizationHeader) {

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public long getExpirationMillis(String token) {
        return validateToken(token).getExpiration().getTime();
    }
}
