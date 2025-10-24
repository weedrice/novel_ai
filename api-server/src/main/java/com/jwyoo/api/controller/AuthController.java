package com.jwyoo.api.controller;

import com.jwyoo.api.dto.LoginRequest;
import com.jwyoo.api.dto.LoginResponse;
import com.jwyoo.api.dto.SignupRequest;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 관련 REST API 컨트롤러
 * 회원가입, 로그인 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        log.info("POST /auth/signup - username: {}", request.getUsername());

        try {
            User user = userService.registerUser(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "회원가입이 완료되었습니다",
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (RuntimeException e) {
            log.error("Signup failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청 DTO
     * @return JWT 토큰 및 사용자 정보
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - username: {}", request.getUsername());

        try {
            // 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // JWT 토큰 생성
            String token = jwtTokenProvider.generateToken(request.getUsername());

            // 사용자 정보 조회
            User user = userService.findByUsername(request.getUsername());

            // UserDTO 생성
            LoginResponse.UserDTO userDTO = LoginResponse.UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

            // 응답 생성
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .user(userDTO)
                    .build();

            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "사용자명 또는 비밀번호가 올바르지 않습니다"
            ));
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "로그인 처리 중 오류가 발생했습니다"
            ));
        }
    }
}