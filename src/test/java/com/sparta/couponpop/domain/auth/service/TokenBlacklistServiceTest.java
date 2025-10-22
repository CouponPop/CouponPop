package com.sparta.couponpop.domain.auth.service;

import com.sparta.couponpop.domain.auth.repository.TokenBlacklistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("토큰 정보를 받아 토큰을 블랙리스트에 추가한다.")
    void blacklistTokenSuccess() {

        // given
        String token = "testToken";
        long expirationMillis = 12345L;

        // when
        tokenBlacklistService.blacklistToken(token, expirationMillis);

        // then
        verify(tokenBlacklistRepository).save(token, expirationMillis);
    }

    @Test
    @DisplayName("블랙리스트에 토큰이 존재하면 true을 반환한다.")
    void isBlacklistedSuccessWithTrue() {

        // given
        String token = "testToken";
        given(tokenBlacklistRepository.exists(token)).willReturn(true);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("블랙리스트에 토큰이 존재하면 false을 반환한다.")
    void isBlacklistedSuccessWithFalse() {

        // given
        String token = "testToken";
        given(tokenBlacklistRepository.exists(token)).willReturn(false);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(token);

        // then
        assertThat(result).isFalse();
    }
}