package com.jwyoo.api.repository;

import com.jwyoo.api.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Refresh Token 저장소
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 RefreshToken 찾기
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자명으로 RefreshToken 찾기
     */
    Optional<RefreshToken> findByUsername(String username);

    /**
     * 사용자명으로 RefreshToken 삭제 (로그아웃 시)
     */
    void deleteByUsername(String username);

    /**
     * 토큰으로 RefreshToken 존재 여부 확인
     */
    boolean existsByToken(String token);
}
