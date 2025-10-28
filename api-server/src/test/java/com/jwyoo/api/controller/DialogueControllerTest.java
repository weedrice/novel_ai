package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.SuggestRequest;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.DialogueRepository;
import com.jwyoo.api.repository.SceneRepository;
import com.jwyoo.api.service.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DialogueController 테스트
 * 대사 제안 및 CRUD 기능 테스트
 */
@WebMvcTest(controllers = DialogueController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class DialogueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LlmClient llmClient;

    @MockBean
    private DialogueRepository dialogueRepository;

    @MockBean
    private SceneRepository sceneRepository;

    @MockBean
    private CharacterRepository characterRepository;

    @MockBean
    private com.jwyoo.api.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.jwyoo.api.security.JwtTokenProvider jwtTokenProvider;

    private Scene testScene;
    private Character testCharacter;
    private Dialogue testDialogue;

    @BeforeEach
    void setUp() {
        testScene = Scene.builder()
                .id(1L)
                .sceneNumber(1)
                .location("학교 운동장")
                .mood("밝고 경쾌함")
                .build();

        testCharacter = Character.builder()
                .id(1L)
                .characterId("char.seha")
                .name("세하")
                .description("밝고 긍정적인 성격")
                .build();

        testDialogue = Dialogue.builder()
                .id(1L)
                .scene(testScene)
                .character(testCharacter)
                .text("야 지호야! 여기서 뭐해?")
                .dialogueOrder(1)
                .honorific("banmal")
                .build();
    }

    @Test
    @DisplayName("대사 제안 성공")
    void suggest_Success() throws Exception {
        // given
        SuggestRequest request = new SuggestRequest(
                "char.seha",
                List.of("char.jiho"),
                "reconcile",
                "banmal",
                80,
                3,
                "openai"
        );

        Map<String, Object> response = new HashMap<>();
        response.put("candidates", List.of(
                Map.of("text", "지호야, 미안해...", "score", 0.95),
                Map.of("text", "우리 다시 친하게 지내자", "score", 0.90),
                Map.of("text", "내가 잘못했어", "score", 0.85)
        ));

        when(llmClient.suggest(any(SuggestRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/dialogue/suggest")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates").isArray())
                .andExpect(jsonPath("$.candidates[0].text").value("지호야, 미안해..."))
                .andExpect(jsonPath("$.candidates[0].score").value(0.95));

        verify(llmClient, times(1)).suggest(any(SuggestRequest.class));
    }

    @Test
    @DisplayName("대사 생성 성공")
    void createDialogue_Success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("sceneId", 1L);
        request.put("characterId", 1L);
        request.put("text", "야 지호야! 여기서 뭐해?");
        request.put("dialogueOrder", 1);
        request.put("honorific", "banmal");

        when(sceneRepository.findById(anyLong())).thenReturn(Optional.of(testScene));
        when(characterRepository.findById(anyLong())).thenReturn(Optional.of(testCharacter));
        when(dialogueRepository.save(any(Dialogue.class))).thenReturn(testDialogue);

        // when & then
        mockMvc.perform(post("/dialogue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("야 지호야! 여기서 뭐해?"))
                .andExpect(jsonPath("$.dialogueOrder").value(1));

        verify(dialogueRepository, times(1)).save(any(Dialogue.class));
    }

    @Test
    @DisplayName("대사 생성 실패 - 존재하지 않는 장면")
    void createDialogue_Failure_SceneNotFound() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("sceneId", 999L);
        request.put("characterId", 1L);
        request.put("text", "테스트 대사");
        request.put("dialogueOrder", 1);

        when(sceneRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/dialogue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("대사 수정 성공")
    void updateDialogue_Success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("text", "수정된 대사");
        request.put("dialogueOrder", 2);

        Dialogue updatedDialogue = Dialogue.builder()
                .id(1L)
                .scene(testScene)
                .character(testCharacter)
                .text("수정된 대사")
                .dialogueOrder(2)
                .honorific("banmal")
                .build();

        when(dialogueRepository.findById(1L)).thenReturn(Optional.of(testDialogue));
        when(dialogueRepository.save(any(Dialogue.class))).thenReturn(updatedDialogue);

        // when & then
        mockMvc.perform(put("/dialogue/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("수정된 대사"))
                .andExpect(jsonPath("$.dialogueOrder").value(2));

        verify(dialogueRepository, times(1)).save(any(Dialogue.class));
    }

    @Test
    @DisplayName("대사 조회 성공")
    void getDialogue_Success() throws Exception {
        // given
        when(dialogueRepository.findById(1L)).thenReturn(Optional.of(testDialogue));

        // when & then
        mockMvc.perform(get("/dialogue/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("야 지호야! 여기서 뭐해?"));

        verify(dialogueRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("대사 조회 실패 - 존재하지 않는 대사")
    void getDialogue_Failure_NotFound() throws Exception {
        // given
        when(dialogueRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/dialogue/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("대사 삭제 성공")
    void deleteDialogue_Success() throws Exception {
        // given
        when(dialogueRepository.findById(1L)).thenReturn(Optional.of(testDialogue));
        doNothing().when(dialogueRepository).delete(any(Dialogue.class));

        // when & then
        mockMvc.perform(delete("/dialogue/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(dialogueRepository, times(1)).delete(any(Dialogue.class));
    }

    // Note: findBySceneIdOrderByDialogueOrder 메서드가 실제로 구현되어 있지 않아 테스트 제외
    // 실제 구현 후 테스트 추가 예정
}
