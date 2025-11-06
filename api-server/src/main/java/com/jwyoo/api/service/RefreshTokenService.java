package com.jwyoo.api.service;

import com.jwyoo.api.entity.RefreshToken;
import com.jwyoo.api.repository.RefreshTokenRepository;
import com.jwyoo.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token 관리 서비스
 */
@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Refresh Token 생성 및 저장
     *
     * @param username 사용자명
     * @return 생성된 Refresh Token 문자열
     */
    @Transactional
    public String createRefreshToken(String username) {
        // 기존 Refresh Token 삭제 (한 사용자당 하나의 Refresh Token만 유지)
        refreshTokenRepository.deleteByUsername(username);

        // 새 Refresh Token 생성
        String tokenValue = jwtTokenProvider.generateRefreshToken(username);

        // 만료 시간 계산 (설정 파일의 jwt.refresh-token-expiration 사용)
        LocalDateTime expiryDate = LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration));

        // DB에 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .username(username)
                .expiryDate(expiryDate)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token created for user: {} (expires: {})", username, expiryDate);

        return tokenValue;
    }

    /**
     * Refresh Token 검증
     *
     * @param token Refresh Token 문자열
     * @return 유효하면 RefreshToken 엔티티, 아니면 Optional.empty()
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> validateRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

        if (refreshToken.isEmpty()) {
            log.warn("Refresh Token not found in database");
            return Optional.empty();
        }

        RefreshToken rt = refreshToken.get();

        // JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid Refresh Token (JWT validation failed)");
            return Optional.empty();
        }

        // 만료 시간 체크
        if (rt.isExpired()) {
            log.warn("Refresh Token expired for user: {}", rt.getUsername());
            refreshTokenRepository.delete(rt); // 만료된 토큰 삭제
            return Optional.empty();
        }

        return refreshToken;
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     *
     * @param refreshTokenValue Refresh Token 문자열
     * @return 새로운 Access Token, 실패 시 null
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenValue) {
        Optional<RefreshToken> refreshToken = validateRefreshToken(refreshTokenValue);

        if (refreshToken.isEmpty()) {
            return null;
        }

        String username = refreshToken.get().getUsername();
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        log.info("Access Token refreshed for user: {}", username);
        return newAccessToken;
    }

    /**
     * Refresh Token 삭제 (로그아웃 시)
     *
     * @param username 사용자명
     */
    @Transactional
    public void deleteRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);
        log.info("Refresh Token deleted for user: {}", username);
    }
}
