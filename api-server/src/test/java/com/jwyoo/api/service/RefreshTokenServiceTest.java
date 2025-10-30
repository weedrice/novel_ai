package com.jwyoo.api.service;

import com.jwyoo.api.entity.RefreshToken;
import com.jwyoo.api.repository.RefreshTokenRepository;
import com.jwyoo.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RefreshTokenService 단위 테스트
 * Refresh Token 생성, 검증, 갱신, 삭제 테스트
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private String username;
    private String tokenValue;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        username = "testuser";
        tokenValue = "test-refresh-token";
        refreshToken = RefreshToken.builder()
                .id(1L)
                .token(tokenValue)
                .username(username)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void createRefreshToken_Success() {
        // given
        when(jwtTokenProvider.generateRefreshToken(username)).thenReturn(tokenValue);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        doNothing().when(refreshTokenRepository).deleteByUsername(username);

        // when
        String result = refreshTokenService.createRefreshToken(username);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(tokenValue);

        verify(refreshTokenRepository).deleteByUsername(username);
        verify(jwtTokenProvider).generateRefreshToken(username);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh Token 검증 성공 - 유효한 토큰")
    void validateRefreshToken_Success_ValidToken() {
        // given
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.validateToken(tokenValue)).thenReturn(true);

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getToken()).isEqualTo(tokenValue);

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(jwtTokenProvider).validateToken(tokenValue);
    }

    @Test
    @DisplayName("Refresh Token 검증 실패 - DB에 없는 토큰")
    void validateRefreshToken_Failure_NotFoundInDatabase() {
        // given
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertThat(result).isEmpty();

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Refresh Token 검증 실패 - JWT 검증 실패")
    void validateRefreshToken_Failure_InvalidJwt() {
        // given
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.validateToken(tokenValue)).thenReturn(false);

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertThat(result).isEmpty();

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(jwtTokenProvider).validateToken(tokenValue);
    }

    @Test
    @DisplayName("Refresh Token 검증 실패 - 만료된 토큰")
    void validateRefreshToken_Failure_ExpiredToken() {
        // given
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .token(tokenValue)
                .username(username)
                .expiryDate(LocalDateTime.now().minusDays(1)) // 이미 만료
                .build();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));
        when(jwtTokenProvider.validateToken(tokenValue)).thenReturn(true);
        doNothing().when(refreshTokenRepository).delete(expiredToken);

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertThat(result).isEmpty();

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(jwtTokenProvider).validateToken(tokenValue);
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("Access Token 갱신 성공")
    void refreshAccessToken_Success() {
        // given
        String newAccessToken = "new-access-token";
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.validateToken(tokenValue)).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(username)).thenReturn(newAccessToken);

        // when
        String result = refreshTokenService.refreshAccessToken(tokenValue);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(newAccessToken);

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(jwtTokenProvider).validateToken(tokenValue);
        verify(jwtTokenProvider).generateAccessToken(username);
    }

    @Test
    @DisplayName("Access Token 갱신 실패 - 유효하지 않은 Refresh Token")
    void refreshAccessToken_Failure_InvalidRefreshToken() {
        // given
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        // when
        String result = refreshTokenService.refreshAccessToken(tokenValue);

        // then
        assertThat(result).isNull();

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Refresh Token 삭제 성공")
    void deleteRefreshToken_Success() {
        // given
        doNothing().when(refreshTokenRepository).deleteByUsername(username);

        // when
        refreshTokenService.deleteRefreshToken(username);

        // then
        verify(refreshTokenRepository).deleteByUsername(username);
    }
}