package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.UserRepository;
import com.jwyoo.api.security.JwtTokenProvider;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProjectController 통합 테스트
 * 프로젝트 CRUD 및 권한 관리를 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(testUser);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.generateAccessToken("testuser");
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트 생성 성공")
    void createProject_Integration_Success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Project");
        request.put("description", "Test Description");

        // when & then
        mockMvc.perform(post("/projects")
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        // 데이터베이스 검증
        assertThat(projectRepository.findAll()).hasSize(1);
        Project savedProject = projectRepository.findAll().get(0);
        assertThat(savedProject.getName()).isEqualTo("Test Project");
        assertThat(savedProject.getOwner().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트 목록 조회 성공")
    void getMyProjects_Integration_Success() throws Exception {
        // given - 여러 프로젝트 생성
        Project project1 = Project.builder()
                .name("Project 1")
                .description("Description 1")
                .owner(testUser)
                .build();
        Project project2 = Project.builder()
                .name("Project 2")
                .description("Description 2")
                .owner(testUser)
                .build();
        projectRepository.save(project1);
        projectRepository.save(project2);

        // when & then
        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트 상세 조회 성공")
    void getProject_Integration_Success() throws Exception {
        // given
        Project project = Project.builder()
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .build();
        Project savedProject = projectRepository.save(project);

        // when & then
        mockMvc.perform(get("/projects/" + savedProject.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProject.getId()))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트 수정 성공")
    void updateProject_Integration_Success() throws Exception {
        // given
        Project project = Project.builder()
                .name("Original Name")
                .description("Original Description")
                .owner(testUser)
                .build();
        Project savedProject = projectRepository.save(project);

        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Name");
        updateRequest.put("description", "Updated Description");

        // when & then
        mockMvc.perform(put("/projects/" + savedProject.getId())
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // 데이터베이스 검증
        Project updatedProject = projectRepository.findById(savedProject.getId()).orElseThrow();
        assertThat(updatedProject.getName()).isEqualTo("Updated Name");
        assertThat(updatedProject.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트 삭제 성공")
    void deleteProject_Integration_Success() throws Exception {
        // given
        Project project = Project.builder()
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .build();
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();

        // when & then
        mockMvc.perform(delete("/projects/" + projectId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // 데이터베이스 검증
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    @DisplayName("통합 테스트: 인증 없이 프로젝트 접근 시도 - 실패")
    void accessProjectWithoutAuth_Integration_Failure() throws Exception {
        // when & then
        mockMvc.perform(get("/projects"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for anonymous access
    }

    @Test
    @DisplayName("통합 테스트: 다른 사용자의 프로젝트 접근 시도 - 실패")
    void accessOtherUserProject_Integration_Failure() throws Exception {
        // given - 다른 사용자와 그 사용자의 프로젝트 생성
        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password(passwordEncoder.encode("password"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(otherUser);

        Project otherUserProject = Project.builder()
                .name("Other User's Project")
                .description("Should not be accessible")
                .owner(otherUser)
                .build();
        Project savedProject = projectRepository.save(otherUserProject);

        // when & then - testuser의 토큰으로 otheruser의 프로젝트 접근 시도
        mockMvc.perform(get("/projects/" + savedProject.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound()); // 또는 403 Forbidden
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트 전체 플로우 - 생성, 조회, 수정, 삭제")
    void projectFullFlow_Integration() throws Exception {
        // 1. 프로젝트 생성
        Map<String, String> createRequest = new HashMap<>();
        createRequest.put("name", "Full Flow Project");
        createRequest.put("description", "Testing full flow");

        String createResponse = mockMvc.perform(post("/projects")
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. 프로젝트 조회
        mockMvc.perform(get("/projects/" + projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Full Flow Project"));

        // 3. 프로젝트 수정
        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Flow Project");
        updateRequest.put("description", "Updated description");

        mockMvc.perform(put("/projects/" + projectId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Flow Project"));

        // 4. 프로젝트 삭제
        mockMvc.perform(delete("/projects/" + projectId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // 5. 삭제 확인
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }
}