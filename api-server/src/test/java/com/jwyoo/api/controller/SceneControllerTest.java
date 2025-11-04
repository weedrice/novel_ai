package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.entity.ScenarioVersion;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.LlmClient;
import com.jwyoo.api.service.ScenarioVersionService;
import com.jwyoo.api.service.SceneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SceneController.class,
        excludeAutoConfiguration = {CacheAutoConfiguration.class, RedisAutoConfiguration.class})
@ActiveProfiles("test")
class SceneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SceneService sceneService;

    @MockBean
    private LlmClient llmClient;

    @MockBean
    private ScenarioVersionService scenarioVersionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("모든 장면 조회 성공")
    @WithMockUser
    void getAllScenes_Success() throws Exception {
        // given
        Scene scene1 = new Scene();
        scene1.setId(1L);
        scene1.setDescription("Scene 1");

        when(sceneService.getAllScenes()).thenReturn(Arrays.asList(scene1));

        // when & then
        mockMvc.perform(get("/scenes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(sceneService).getAllScenes();
    }

    @Test
    @DisplayName("장면 상세 조회 성공")
    @WithMockUser
    void getScene_Success() throws Exception {
        // given
        Scene scene = new Scene();
        scene.setId(1L);
        scene.setDescription("Test Scene");

        Character character = new Character();
        character.setId(1L);
        character.setName("Test Character");

        when(sceneService.getSceneById(1L)).thenReturn(scene);
        when(sceneService.getParticipants(any())).thenReturn(Arrays.asList(character));

        // when & then
        mockMvc.perform(get("/scenes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scene").exists())
                .andExpect(jsonPath("$.participants").exists())
                .andExpect(jsonPath("$.participants").isArray());

        verify(sceneService).getSceneById(1L);
        verify(sceneService).getParticipants(any());
    }

    @Test
    @DisplayName("에피소드별 장면 조회 성공")
    @WithMockUser
    void getScenesByEpisode_Success() throws Exception {
        // given
        Scene scene = new Scene();
        scene.setId(1L);

        when(sceneService.getScenesByEpisodeId(1L)).thenReturn(Arrays.asList(scene));

        // when & then
        mockMvc.perform(get("/scenes/episode/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(sceneService).getScenesByEpisodeId(1L);
    }

    @Test
    @DisplayName("장면의 대사 목록 조회 성공")
    @WithMockUser
    void getDialogues_Success() throws Exception {
        // given
        Dialogue dialogue = new Dialogue();
        dialogue.setId(1L);
        dialogue.setText("Test line");

        when(sceneService.getDialogues(1L)).thenReturn(Arrays.asList(dialogue));

        // when & then
        mockMvc.perform(get("/scenes/1/dialogues")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(sceneService).getDialogues(1L);
    }

    @Test
    @DisplayName("시나리오 생성 성공")
    @WithMockUser
    void generateScenario_Success() throws Exception {
        // given
        Scene scene = new Scene();
        scene.setId(1L);
        scene.setDescription("Test scene");
        scene.setLocation("Test location");
        scene.setMood("happy");

        Character character = new Character();
        character.setCharacterId("char1");
        character.setName("Alice");
        character.setPersonality("cheerful");
        character.setSpeakingStyle("casual");

        Map<String, Object> llmResponse = new HashMap<>();
        llmResponse.put("dialogues", Arrays.asList());

        when(sceneService.getSceneById(1L)).thenReturn(scene);
        when(sceneService.getParticipants(any())).thenReturn(Arrays.asList(character));
        when(llmClient.generateScenario(any())).thenReturn(llmResponse);

        // when & then
        mockMvc.perform(post("/scenes/1/generate-scenario")
                        .param("provider", "openai")
                        .param("dialogueCount", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sceneService).getSceneById(1L);
        verify(llmClient).generateScenario(any());
    }

    @Test
    @DisplayName("시나리오 생성 실패 - 참가자 없음")
    @WithMockUser
    void generateScenario_NoParticipants() throws Exception {
        // given
        Scene scene = new Scene();
        scene.setId(1L);

        when(sceneService.getSceneById(1L)).thenReturn(scene);
        when(sceneService.getParticipants(any())).thenReturn(Arrays.asList());

        // when & then
        mockMvc.perform(post("/scenes/1/generate-scenario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Scene has no participants."));

        verify(sceneService).getSceneById(1L);
        verify(llmClient, never()).generateScenario(any());
    }

    @Test
    @DisplayName("장면 생성 성공")
    @WithMockUser
    void createScene_Success() throws Exception {
        // given
        Scene scene = new Scene();
        scene.setDescription("New scene");

        Scene createdScene = new Scene();
        createdScene.setId(1L);
        createdScene.setDescription("New scene");

        when(sceneService.createScene(any())).thenReturn(createdScene);

        // when & then
        mockMvc.perform(post("/scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scene)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(sceneService).createScene(any());
    }

    @Test
    @DisplayName("장면 수정 성공")
    @WithMockUser
    void updateScene_Success() throws Exception {
        // given
        Scene scene = new Scene();
        scene.setDescription("Updated scene");

        Scene updatedScene = new Scene();
        updatedScene.setId(1L);
        updatedScene.setDescription("Updated scene");

        when(sceneService.updateScene(eq(1L), any())).thenReturn(updatedScene);

        // when & then
        mockMvc.perform(put("/scenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scene)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(sceneService).updateScene(eq(1L), any());
    }

    @Test
    @DisplayName("장면 삭제 성공")
    @WithMockUser
    void deleteScene_Success() throws Exception {
        // given
        doNothing().when(sceneService).deleteScene(1L);

        // when & then
        mockMvc.perform(delete("/scenes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(sceneService).deleteScene(1L);
    }

    @Test
    @DisplayName("시나리오 버전 저장 성공")
    @WithMockUser
    void saveScenarioVersion_Success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("title", "Version 1");
        request.put("content", "Content");
        request.put("createdBy", "user");

        ScenarioVersion version = new ScenarioVersion();
        version.setId(1L);
        version.setTitle("Version 1");

        when(scenarioVersionService.saveVersion(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(version);

        // when & then
        mockMvc.perform(post("/scenes/1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(scenarioVersionService).saveVersion(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("시나리오 버전 목록 조회 성공")
    @WithMockUser
    void getScenarioVersions_Success() throws Exception {
        // given
        ScenarioVersion version = new ScenarioVersion();
        version.setId(1L);

        when(scenarioVersionService.getVersionsBySceneId(1L)).thenReturn(Arrays.asList(version));

        // when & then
        mockMvc.perform(get("/scenes/1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(scenarioVersionService).getVersionsBySceneId(1L);
    }

    @Test
    @DisplayName("시나리오 버전 상세 조회 성공")
    @WithMockUser
    void getScenarioVersion_Success() throws Exception {
        // given
        ScenarioVersion version = new ScenarioVersion();
        version.setId(1L);

        when(scenarioVersionService.getVersionById(1L)).thenReturn(version);

        // when & then
        mockMvc.perform(get("/scenes/scenarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(scenarioVersionService).getVersionById(1L);
    }

    @Test
    @DisplayName("시나리오 버전 삭제 성공")
    @WithMockUser
    void deleteScenarioVersion_Success() throws Exception {
        // given
        doNothing().when(scenarioVersionService).deleteVersion(1L);

        // when & then
        mockMvc.perform(delete("/scenes/scenarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(scenarioVersionService).deleteVersion(1L);
    }
}
