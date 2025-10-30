package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.EpisodeRepository;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.SceneRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SceneIntegrationTest {

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
    private SceneRepository sceneRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Long projectId;
    private Long episodeId;
    private Episode testEpisode;

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
        Project testProject = new Project();
        testProject.setName("Test Project");
        testProject.setOwner(testUser);
        testProject = projectRepository.save(testProject);
        projectId = testProject.getId();

        // 테스트 에피소드 생성
        testEpisode = new Episode();
        testEpisode.setTitle("Test Episode");
        testEpisode.setDescription("Test Description");
        testEpisode.setEpisodeOrder(1);
        testEpisode.setProject(testProject);
        testEpisode = episodeRepository.save(testEpisode);
        episodeId = testEpisode.getId();
    }

    @Test
    @DisplayName("통합 테스트: 씬 생성")
    void createScene_Integration_Success() throws Exception {
        Scene request = new Scene();
        request.setSceneNumber(1);
        request.setLocation("학교 복도");
        request.setMood("긴장감");
        request.setDescription("주인공이 복도를 걷는다");
        request.setParticipants("주인공, 친구");
        request.setEpisode(testEpisode);

        mockMvc.perform(post("/scenes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sceneNumber").value(1))
                .andExpect(jsonPath("$.location").value("학교 복도"))
                .andExpect(jsonPath("$.episode.id").value(episodeId));
    }

    @Test
    @DisplayName("통합 테스트: 에피소드별 씬 목록 조회")
    void getScenesByEpisode_Integration_Success() throws Exception {
        // 테스트 씬 생성
        Scene scene1 = new Scene();
        scene1.setSceneNumber(1);
        scene1.setLocation("장소 1");
        scene1.setEpisode(testEpisode);
        sceneRepository.save(scene1);

        Scene scene2 = new Scene();
        scene2.setSceneNumber(2);
        scene2.setLocation("장소 2");
        scene2.setEpisode(testEpisode);
        sceneRepository.save(scene2);

        mockMvc.perform(get("/scenes/episode/{episodeId}", episodeId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sceneNumber").value(1))
                .andExpect(jsonPath("$[1].sceneNumber").value(2));
    }

    @Test
    @DisplayName("통합 테스트: 씬 ID로 조회")
    void getSceneById_Integration_Success() throws Exception {
        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("테스트 장소");
        scene.setEpisode(testEpisode);
        scene = sceneRepository.save(scene);

        mockMvc.perform(get("/scenes/{id}", scene.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scene.sceneNumber").value(1))
                .andExpect(jsonPath("$.scene.location").value("테스트 장소"));
    }

    @Test
    @DisplayName("통합 테스트: 씬 업데이트")
    void updateScene_Integration_Success() throws Exception {
        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("원본 장소");
        scene.setEpisode(testEpisode);
        scene = sceneRepository.save(scene);

        Scene updateRequest = new Scene();
        updateRequest.setSceneNumber(1);
        updateRequest.setLocation("수정된 장소");
        updateRequest.setMood("수정된 분위기");
        updateRequest.setEpisode(testEpisode);

        mockMvc.perform(put("/scenes/{id}", scene.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("수정된 장소"))
                .andExpect(jsonPath("$.mood").value("수정된 분위기"));
    }

    @Test
    @DisplayName("통합 테스트: 씬 삭제")
    void deleteScene_Integration_Success() throws Exception {
        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("삭제될 장소");
        scene.setEpisode(testEpisode);
        scene = sceneRepository.save(scene);

        Long sceneId = scene.getId();

        mockMvc.perform(delete("/scenes/{id}", sceneId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/scenes/{id}", sceneId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통합 테스트: 인증 없이 씬 접근 - 실패")
    void accessSceneWithoutAuth_Integration_Failure() throws Exception {
        mockMvc.perform(get("/scenes/episode/{episodeId}", episodeId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("통합 테스트: 씬 번호 순서대로 정렬되는지 확인")
    void getScenesOrderedBySceneNumber_Integration_Success() throws Exception {
        Scene scene3 = new Scene();
        scene3.setSceneNumber(3);
        scene3.setLocation("장소 3");
        scene3.setEpisode(testEpisode);
        sceneRepository.save(scene3);

        Scene scene1 = new Scene();
        scene1.setSceneNumber(1);
        scene1.setLocation("장소 1");
        scene1.setEpisode(testEpisode);
        sceneRepository.save(scene1);

        Scene scene2 = new Scene();
        scene2.setSceneNumber(2);
        scene2.setLocation("장소 2");
        scene2.setEpisode(testEpisode);
        sceneRepository.save(scene2);

        mockMvc.perform(get("/scenes/episode/{episodeId}", episodeId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sceneNumber").value(1))
                .andExpect(jsonPath("$[1].sceneNumber").value(2))
                .andExpect(jsonPath("$[2].sceneNumber").value(3));
    }

    @Test
    @DisplayName("통합 테스트: 장면의 대사 목록 조회")
    void getDialoguesByScene_Integration_Success() throws Exception {
        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("테스트 장소");
        scene.setEpisode(testEpisode);
        scene = sceneRepository.save(scene);

        mockMvc.perform(get("/scenes/{id}/dialogues", scene.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("통합 테스트: 시나리오 버전 저장")
    void saveScenarioVersion_Integration_Success() throws Exception {
        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("테스트 장소");
        scene.setEpisode(testEpisode);
        scene = sceneRepository.save(scene);

        java.util.Map<String, String> request = new java.util.HashMap<>();
        request.put("title", "버전 1");
        request.put("content", "시나리오 내용입니다.");
        request.put("createdBy", "testuser");

        mockMvc.perform(post("/scenes/{sceneId}/scenarios", scene.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("버전 1"))
                .andExpect(jsonPath("$.content").value("시나리오 내용입니다."));
    }

    @Test
    @DisplayName("통합 테스트: 장면의 시나리오 버전 목록 조회")
    void getScenarioVersions_Integration_Success() throws Exception {
        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("테스트 장소");
        scene.setEpisode(testEpisode);
        scene = sceneRepository.save(scene);

        mockMvc.perform(get("/scenes/{sceneId}/scenarios", scene.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("통합 테스트: 모든 장면 조회")
    void getAllScenes_Integration_Success() throws Exception {
        Scene scene1 = new Scene();
        scene1.setSceneNumber(1);
        scene1.setLocation("장소 1");
        scene1.setEpisode(testEpisode);
        sceneRepository.save(scene1);

        Scene scene2 = new Scene();
        scene2.setSceneNumber(2);
        scene2.setLocation("장소 2");
        scene2.setEpisode(testEpisode);
        sceneRepository.save(scene2);

        mockMvc.perform(get("/scenes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }
}
