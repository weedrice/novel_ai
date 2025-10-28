package com.jwyoo.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtTokenProvider 테스트
 * 토큰 생성, 검증, 사용자명 추출 기능 테스트
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    // HS512는 최소 512비트 (64바이트) 필요
    private final String testSecret = "testSecretKeyForJwtTokenProviderWithMinimum512BitsForHS512Algorithm";
    private final long accessTokenExpiration = 900000L; // 15분
    private final long refreshTokenExpiration = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret, accessTokenExpiration, refreshTokenExpiration);
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken_Success() {
        // given
        String username = "testuser";

        // when
        String token = jwtTokenProvider.generateAccessToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성 (header.payload.signature)
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void generateRefreshToken_Success() {
        // given
        String username = "testuser";

        // when
        String token = jwtTokenProvider.generateRefreshToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출 성공")
    void getUsernameFromToken_Success() {
        // given
        String username = "testuser";
        String token = jwtTokenProvider.generateAccessToken(username);

        // when
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        String username = "testuser";
        String token = jwtTokenProvider.generateAccessToken(username);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void validateToken_InvalidToken_ReturnsFalse() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된 시크릿으로 생성한 토큰 검증 실패")
    void validateToken_WrongSecret_ReturnsFalse() {
        // given
        String username = "testuser";
        String wrongSecret = "wrongSecretKeyForJwtTokenProviderWithMinimum512BitsForHS512Algorithm";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

        // 잘못된 시크릿으로 토큰 생성
        String token = Jwts.builder()
                .subject(username)
                .signWith(wrongKey, Jwts.SIG.HS512)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Access Token과 Refresh Token 만료시간이 다름")
    void tokenExpiration_Different() throws Exception {
        // given
        String username = "testuser";

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        // then
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        Claims accessClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        Claims refreshClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        long accessExpiration = accessClaims.getExpiration().getTime() - accessClaims.getIssuedAt().getTime();
        long refreshExpiration = refreshClaims.getExpiration().getTime() - refreshClaims.getIssuedAt().getTime();

        assertThat(refreshExpiration).isGreaterThan(accessExpiration);
    }

    @Test
    @DisplayName("레거시 generateToken 메서드 동작 확인")
    void generateToken_Legacy_Works() {
        // given
        String username = "testuser";

        // when
        @SuppressWarnings("deprecation")
        String token = jwtTokenProvider.generateToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_EmptyToken_ReturnsFalse() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 실패")
    void validateToken_NullToken_ReturnsFalse() {
        // given
        String nullToken = null;

        // when
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // then
        assertThat(isValid).isFalse();
    }
}
