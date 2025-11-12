package com.jwyoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token; // Access Token

    private String refreshToken; // Refresh Token

    @Builder.Default
    private String type = "Bearer";

    private String username;
    private String email;

    private UserDTO user;

    /**
     * 사용자 정보 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        private Long id;
        private String username;
        private String name; // 사용자 실명 (선택)
        private String email;
        private String role;
    }
}