package com.sparta.couponpop.common.security;

import com.sparta.couponpop.common.exception.CommonErrorCode;
import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.common.security.dto.AuthMember;
import com.sparta.couponpop.domain.auth.exception.AuthErrorCode;
import com.sparta.couponpop.domain.member.enums.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtProvider jwtProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        try {
            String bearerToken = resolveToken(request);

            // 토큰 존재 여부 확인
            if (!StringUtils.hasText(bearerToken)) {
                log.debug("[JwtFilter] 토큰이 존재하지 않는 요청");
                chain.doFilter(request, response);
                return;
            }

            // 토큰 검증
            Claims claims = jwtProvider.validateToken(bearerToken);
            setAuthentication(claims);
        } catch (ExpiredJwtException e) {
            log.debug("[JwtFilter] 인증 실패: 만료된 토큰 - {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(AuthErrorCode.EXPIRED_TOKEN));
        } catch (MalformedJwtException | SignatureException e) { // 토큰의 형식이 올바르지 않은 경우, 토큰의 시그니처가 올바르지 않은 경우
            log.debug("[JwtFilter] 인증 실패: 유효하지 않은 토큰 - {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(AuthErrorCode.INVALID_TOKEN));
        } catch (Exception e) {
            log.debug("[JwtFilter] 인증 실패: 토큰 인증 과정 중 다른 오류 발생 - {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(CommonErrorCode.INTERNAL_SERVER_ERROR));
        }

        chain.doFilter(request, response);
    }

    private void setAuthentication(Claims claims) {

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        MemberType memberType = MemberType.valueOf(claims.get("memberType", String.class));

        AuthMember authMember = AuthMember.from(userId, username, memberType);
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String resolveToken(HttpServletRequest request) {

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
