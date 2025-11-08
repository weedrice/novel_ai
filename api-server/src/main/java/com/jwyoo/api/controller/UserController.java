package com.jwyoo.api.controller;

import com.jwyoo.api.entity.User;
import com.jwyoo.api.service.UserService;
import com.jwyoo.api.security.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        log.info("GET /users/me - username: {}", username);

        User user = userService.findByUsername(username);

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName() != null ? user.getName() : "",
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt(),
                "updatedAt", user.getUpdatedAt()
        ));
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {
        String username = authentication.getName();
        log.info("PUT /users/me - username: {}, updating profile", username);

        try {
            User user = userService.updateUser(username, request);

            return ResponseEntity.ok(Map.of(
                    "message", "프로필이 업데이트되었습니다",
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "name", user.getName() != null ? user.getName() : "",
                            "email", user.getEmail(),
                            "role", user.getRole().name()
                    )
            ));
        } catch (RuntimeException e) {
            log.error("Failed to update user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        String username = authentication.getName();
        log.info("PUT /users/me/password - username: {}", username);

        try {
            User user = userService.findByUsername(username);

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "현재 비밀번호가 일치하지 않습니다"));
            }

            // 비밀번호 변경
            userService.changePassword(username, request.getNewPassword());

            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다"));
        } catch (RuntimeException e) {
            log.error("Failed to change password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 사용자 정보 수정 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        @Size(max = 100, message = "이름은 100자 이하여야 합니다")
        private String name;

        @Email(message = "유효한 이메일 주소를 입력하세요")
        private String email;
    }

    /**
     * 비밀번호 변경 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @Size(min = 6, max = 100, message = "비밀번호는 6-100자 사이여야 합니다")
        private String currentPassword;

        @Size(min = 6, max = 100, message = "새 비밀번호는 6-100자 사이여야 합니다")
        private String newPassword;
    }
}
