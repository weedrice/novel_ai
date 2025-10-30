package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.LoginRequest;
import com.jwyoo.api.dto.SignupRequest;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.RefreshTokenRepository;
import com.jwyoo.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 통합 테스트
 * 실제 애플리케이션 컨텍스트를 로드하여 인증 API를 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("통합 테스트: 회원가입 성공")
    void signup_Integration_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // 데이터베이스 검증
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("통합 테스트: 회원가입 실패 - 중복된 사용자명")
    void signup_Integration_Failure_DuplicateUsername() throws Exception {
        // given - 기존 사용자 생성
        User existingUser = User.builder()
                .username("testuser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(existingUser);

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("통합 테스트: 로그인 성공 및 토큰 발급")
    void login_Integration_Success() throws Exception {
        // given - 사용자 등록
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(user);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));

        // 데이터베이스 검증 - Refresh Token이 저장되었는지 확인
        assertThat(refreshTokenRepository.count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 실패 - 잘못된 비밀번호")
    void login_Integration_Failure_WrongPassword() throws Exception {
        // given - 사용자 등록
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(user);

        // when - 잘못된 비밀번호로 로그인 시도
        loginRequest.setPassword("wrongpassword");

        // then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("통합 테스트: 회원가입 → 로그인 전체 플로우")
    void signupAndLogin_Integration_FullFlow() throws Exception {
        // 1. 회원가입
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // 2. 로그인
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        // 3. 데이터베이스 검증
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(refreshTokenRepository.findAll()).isNotEmpty();
    }
}