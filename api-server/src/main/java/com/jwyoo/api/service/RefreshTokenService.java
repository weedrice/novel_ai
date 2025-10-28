package com.jwyoo.api.service;

import com.jwyoo.api.entity.RefreshToken;
import com.jwyoo.api.repository.RefreshTokenRepository;
import com.jwyoo.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

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

        // DB에 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .username(username)
                .expiryDate(LocalDateTime.now().plusDays(7)) // 7일
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token created for user: {}", username);

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
