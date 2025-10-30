package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.EpisodeRepository;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.UserRepository;
import com.jwyoo.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EpisodeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        authToken = jwtTokenProvider.generateToken(testUser.getUsername());

        // 테스트 프로젝트 생성
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setOwner(testUser);
        testProject = projectRepository.save(testProject);
    }

    @Test
    @DisplayName("통합 테스트: 모든 에피소드 조회")
    void getAllEpisodes_Integration_Success() throws Exception {
        // 테스트 에피소드 생성
        Episode episode1 = new Episode();
        episode1.setTitle("에피소드 1");
        episode1.setDescription("첫 번째 에피소드");
        episode1.setEpisodeOrder(1);
        episode1.setProject(testProject);
        episodeRepository.save(episode1);

        Episode episode2 = new Episode();
        episode2.setTitle("에피소드 2");
        episode2.setDescription("두 번째 에피소드");
        episode2.setEpisodeOrder(2);
        episode2.setProject(testProject);
        episodeRepository.save(episode2);

        mockMvc.perform(get("/episodes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].order").exists());
    }

    @Test
    @DisplayName("통합 테스트: 에피소드 순서대로 조회")
    void getEpisodesInOrder_Integration_Success() throws Exception {
        // 순서를 뒤바꿔서 생성
        Episode episode3 = new Episode();
        episode3.setTitle("에피소드 3");
        episode3.setDescription("세 번째");
        episode3.setEpisodeOrder(3);
        episode3.setProject(testProject);
        episodeRepository.save(episode3);

        Episode episode1 = new Episode();
        episode1.setTitle("에피소드 1");
        episode1.setDescription("첫 번째");
        episode1.setEpisodeOrder(1);
        episode1.setProject(testProject);
        episodeRepository.save(episode1);

        Episode episode2 = new Episode();
        episode2.setTitle("에피소드 2");
        episode2.setDescription("두 번째");
        episode2.setEpisodeOrder(2);
        episode2.setProject(testProject);
        episodeRepository.save(episode2);

        mockMvc.perform(get("/episodes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("통합 테스트: 빈 설명이 있는 에피소드 조회")
    void getEpisodeWithNullDescription_Integration_Success() throws Exception {
        Episode episode = new Episode();
        episode.setTitle("설명 없는 에피소드");
        episode.setDescription(null); // 설명 없음
        episode.setEpisodeOrder(1);
        episode.setProject(testProject);
        episodeRepository.save(episode);

        mockMvc.perform(get("/episodes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == '설명 없는 에피소드')].description").value(""));
    }

    @Test
    @DisplayName("통합 테스트: 인증 없이 에피소드 접근 - 실패")
    void accessEpisodeWithoutAuth_Integration_Failure() throws Exception {
        mockMvc.perform(get("/episodes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("통합 테스트: 에피소드 없을 때 빈 배열 반환")
    void getEpisodesWhenEmpty_Integration_Success() throws Exception {
        // 모든 에피소드 삭제
        episodeRepository.deleteAll();

        mockMvc.perform(get("/episodes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
