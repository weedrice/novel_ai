package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.LoginRequest;
import com.jwyoo.api.dto.LoginResponse;
import com.jwyoo.api.dto.SignupRequest;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.RefreshTokenService;
import com.jwyoo.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 테스트
 * 회원가입, 로그인, 토큰 갱신 기능 테스트
 */
@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private com.jwyoo.api.security.CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() throws Exception {
        // given
        when(userService.registerUser(any(SignupRequest.class))).thenReturn(testUser);

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).registerUser(any(SignupRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 사용자명")
    void signup_Failure_DuplicateUsername() throws Exception {
        // given
        when(userService.registerUser(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("이미 존재하는 사용자명입니다"));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 존재하는 사용자명입니다"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn("refresh-token");
        when(userService.findByUsername(anyString())).thenReturn(testUser);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenProvider, times(1)).generateAccessToken(anyString());
        verify(refreshTokenService, times(1)).createRefreshToken(anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Failure_BadCredentials() throws Exception {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("사용자명 또는 비밀번호가 올바르지 않습니다"));
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_Success() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";

        when(refreshTokenService.refreshAccessToken(refreshToken)).thenReturn(newAccessToken);
        when(jwtTokenProvider.getUsernameFromToken(newAccessToken)).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(newAccessToken))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("Access Token이 갱신되었습니다"));

        verify(refreshTokenService, times(1)).refreshAccessToken(refreshToken);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 잘못된 Refresh Token")
    void refresh_Failure_InvalidToken() throws Exception {
        // given
        String invalidRefreshToken = "invalid-refresh-token";
        when(refreshTokenService.refreshAccessToken(invalidRefreshToken)).thenReturn(null);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + invalidRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("유효하지 않거나 만료된 Refresh Token입니다"));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Refresh Token 누락")
    void refresh_Failure_MissingToken() throws Exception {
        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Refresh Token이 필요합니다"));
    }
}
