package com.jwyoo.api.service;

import com.jwyoo.api.dto.SignupRequest;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스
 * 사용자 등록, 조회 등의 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 등록 (회원가입)
     *
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자
     * @throws RuntimeException 사용자명 또는 이메일이 이미 존재하는 경우
     */
    @Transactional
    public User registerUser(SignupRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new RuntimeException("사용자명이 이미 존재합니다: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new RuntimeException("이메일이 이미 존재합니다: " + request.getEmail());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(User.UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return savedUser;
    }

    /**
     * 사용자명으로 사용자 조회
     *
     * @param username 사용자명
     * @return 사용자
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 사용자 정보 수정
     *
     * @param username 사용자명
     * @param request 수정 요청 (이름, 이메일)
     * @return 수정된 사용자
     * @throws RuntimeException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public User updateUser(String username, com.jwyoo.api.controller.UserController.UpdateUserRequest request) {
        log.info("Updating user: {}", username);

        User user = findByUsername(username);

        // 이메일 변경 시 중복 체크
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                throw new RuntimeException("이메일이 이미 존재합니다: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // 이름 수정
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());

        return updatedUser;
    }

    /**
     * 비밀번호 변경
     *
     * @param username 사용자명
     * @param newPassword 새 비밀번호
     */
    @Transactional
    public void changePassword(String username, String newPassword) {
        log.info("Changing password for user: {}", username);

        User user = findByUsername(username);
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
    }
}