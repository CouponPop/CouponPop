package com.sparta.couponpop.common.security;

import com.sparta.couponpop.domain.member.enums.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtProvider {

    private static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L; // 1시간

    @Value("${jwt.secret.key}")
    private String base64SecretKey;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(base64SecretKey);
        this.secretKey = Keys.hmacShaKeyFor(bytes); // 미리 암호화
    }

    public String createToken(Long userId, String username, MemberType memberType) {

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
}
