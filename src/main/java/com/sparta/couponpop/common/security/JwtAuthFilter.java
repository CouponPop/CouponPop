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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            String bearerToken = getToken(authorizationHeader);

            // 토큰 검증
            if (authorizationHeader == null) {
                log.debug("[JwtFilter] 토큰이 존재하지 않는 요청입니다.");
                chain.doFilter(request, response);
                return;
            }

            log.debug("[JwtFilter] 인증 요청 토큰: JWT Authenticated token: " + bearerToken);
            Claims claims = jwtProvider.validateToken(bearerToken);
            String memberId = claims.getSubject();
            log.debug("[JwtFilter] 인증 요청 아이디: JWT Authenticated member: " + memberId);

            // 사용자 정보 추출 및 인증 객체 생성
            setAuthentication(claims);
        } catch (ExpiredJwtException e) {
            log.debug("[JwtFilter] 인증 실패: 만료된 토큰 - username={}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(AuthErrorCode.EXPIRED_TOKEN));
        } catch (
                UsernameNotFoundException e) { // 토큰은 유효하지만 DB에 사용자가 없는 경우 -> 필터체인에서 url에 따라 허용 판단
            log.debug("[JwtFilter] 인증 보류: 회원가입과 로그인은 수행가능합니다. 보류 토큰 - username={}", e.getMessage());
        } catch (MalformedJwtException e) { // 토큰의 형식이 올바르지 않은 경우
            log.debug("[JwtFilter] 인증 실패: 유효하지 않은 토큰 - username={}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(AuthErrorCode.INVALID_TOKEN));
        } catch (
                SignatureException e) { // 토큰의 시그니처가 올바르지 않은 경우
            log.debug("[JwtFilter] 인증 실패: 유효하지 않은 토큰 - username={}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(AuthErrorCode.INVALID_TOKEN));
        } catch (Exception e) {
            log.warn("[JwtFilter] 인증 실패: 토큰 인증 과정 중 다른 오류 발생 - {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null,
                    new GlobalException(CommonErrorCode.INTERNAL_SERVER_ERROR));
        }

        chain.doFilter(request, response);
    }


    // JWT Claims에서 사용자 정보를 추출하여 Spring Security의 인증 정보 설정
    private void setAuthentication(Claims claims) {

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        MemberType memberType = MemberType.valueOf(claims.get("memberType", String.class));

        AuthMember authMember = AuthMember.from(userId, username, memberType);
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getToken(String authHeaderValue) {
        if (StringUtils.hasText(authHeaderValue) && authHeaderValue.startsWith(BEARER_PREFIX)) {
            return authHeaderValue.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
