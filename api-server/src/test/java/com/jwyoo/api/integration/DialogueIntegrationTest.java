package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.*;
import com.jwyoo.api.repository.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DialogueIntegrationTest {

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
    private CharacterRepository characterRepository;

    @Autowired
    private DialogueRepository dialogueRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Scene testScene;
    private com.jwyoo.api.entity.Character testCharacter;

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
        testProject.setDescription("Test Description");
        testProject.setOwner(testUser);
        testProject = projectRepository.save(testProject);

        // 테스트 에피소드 생성
        Episode testEpisode = new Episode();
        testEpisode.setTitle("Test Episode");
        testEpisode.setDescription("Test Description");
        testEpisode.setEpisodeOrder(1);
        testEpisode.setProject(testProject);
        testEpisode = episodeRepository.save(testEpisode);

        // 테스트 장면 생성
        testScene = new Scene();
        testScene.setSceneNumber(1);
        testScene.setLocation("테스트 장소");
        testScene.setMood("긴장");
        testScene.setEpisode(testEpisode);
        testScene = sceneRepository.save(testScene);

        // 테스트 캐릭터 생성
        testCharacter = new com.jwyoo.api.entity.Character();
        testCharacter.setCharacterId("char_001");
        testCharacter.setName("테스트 캐릭터");
        testCharacter.setDescription("테스트용 캐릭터");
        testCharacter.setProject(testProject);
        testCharacter = characterRepository.save(testCharacter);
    }

    @Test
    @DisplayName("통합 테스트: 대사 생성")
    void createDialogue_Integration_Success() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("sceneId", testScene.getId());
        request.put("characterId", testCharacter.getId());
        request.put("text", "안녕하세요, 반갑습니다!");
        request.put("dialogueOrder", 1);
        request.put("intent", "greeting");
        request.put("honorific", "jondae");
        request.put("emotion", "happy");

        mockMvc.perform(post("/dialogue")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("안녕하세요, 반갑습니다!"))
                .andExpect(jsonPath("$.dialogueOrder").value(1))
                .andExpect(jsonPath("$.intent").value("greeting"))
                .andExpect(jsonPath("$.honorific").value("jondae"))
                .andExpect(jsonPath("$.emotion").value("happy"));
    }

    @Test
    @DisplayName("통합 테스트: 대사 조회")
    void getDialogue_Integration_Success() throws Exception {
        // 테스트 대사 생성
        Dialogue dialogue = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("테스트 대사입니다.")
                .dialogueOrder(1)
                .intent("inform")
                .emotion("neutral")
                .build();
        dialogue = dialogueRepository.save(dialogue);

        mockMvc.perform(get("/dialogue/{id}", dialogue.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("테스트 대사입니다."))
                .andExpect(jsonPath("$.intent").value("inform"))
                .andExpect(jsonPath("$.emotion").value("neutral"));
    }

    @Test
    @DisplayName("통합 테스트: 대사 업데이트")
    void updateDialogue_Integration_Success() throws Exception {
        // 대사 생성
        Dialogue dialogue = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("원래 대사")
                .dialogueOrder(1)
                .intent("inform")
                .build();
        dialogue = dialogueRepository.save(dialogue);

        // 업데이트 요청
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("text", "수정된 대사");
        updateRequest.put("intent", "question");
        updateRequest.put("emotion", "curious");

        mockMvc.perform(put("/dialogue/{id}", dialogue.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("수정된 대사"))
                .andExpect(jsonPath("$.intent").value("question"))
                .andExpect(jsonPath("$.emotion").value("curious"));
    }

    @Test
    @DisplayName("통합 테스트: 대사 삭제")
    void deleteDialogue_Integration_Success() throws Exception {
        // 대사 생성
        Dialogue dialogue = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("삭제될 대사")
                .dialogueOrder(1)
                .build();
        dialogue = dialogueRepository.save(dialogue);

        Long dialogueId = dialogue.getId();

        // 삭제
        mockMvc.perform(delete("/dialogue/{id}", dialogueId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/dialogue/{id}", dialogueId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통합 테스트: 장면별 대사 목록 조회")
    void getDialoguesByScene_Integration_Success() throws Exception {
        // 테스트 대사 여러 개 생성
        Dialogue dialogue1 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("첫 번째 대사")
                .dialogueOrder(1)
                .build();
        dialogueRepository.save(dialogue1);

        Dialogue dialogue2 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("두 번째 대사")
                .dialogueOrder(2)
                .build();
        dialogueRepository.save(dialogue2);

        mockMvc.perform(get("/dialogue/scene/{sceneId}", testScene.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dialogueOrder").value(1))
                .andExpect(jsonPath("$[1].dialogueOrder").value(2));
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터별 대사 목록 조회")
    void getDialoguesByCharacter_Integration_Success() throws Exception {
        // 테스트 대사 생성
        Dialogue dialogue1 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("캐릭터의 대사 1")
                .dialogueOrder(1)
                .build();
        dialogueRepository.save(dialogue1);

        Dialogue dialogue2 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("캐릭터의 대사 2")
                .dialogueOrder(2)
                .build();
        dialogueRepository.save(dialogue2);

        mockMvc.perform(get("/dialogue/character/{characterId}", testCharacter.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].text").exists())
                .andExpect(jsonPath("$[1].text").exists());
    }

    @Test
    @DisplayName("통합 테스트: 인증 없이 대사 접근 - 실패")
    void accessDialogueWithoutAuth_Integration_Failure() throws Exception {
        mockMvc.perform(get("/dialogue/scene/{sceneId}", testScene.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("통합 테스트: 존재하지 않는 대사 조회 - 실패")
    void getDialogueById_NotFound_Failure() throws Exception {
        mockMvc.perform(get("/dialogue/{id}", 99999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터 변경 업데이트")
    void updateDialogueWithCharacterChange_Integration_Success() throws Exception {
        // 두 번째 캐릭터 생성
        com.jwyoo.api.entity.Character character2 = new com.jwyoo.api.entity.Character();
        character2.setCharacterId("char_002");
        character2.setName("다른 캐릭터");
        character2.setProject(testCharacter.getProject());
        character2 = characterRepository.save(character2);

        // 대사 생성
        Dialogue dialogue = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("원래 대사")
                .dialogueOrder(1)
                .build();
        dialogue = dialogueRepository.save(dialogue);

        // 캐릭터 변경 요청
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("characterId", character2.getId());
        updateRequest.put("text", "다른 캐릭터의 대사");

        mockMvc.perform(put("/dialogue/{id}", dialogue.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("다른 캐릭터의 대사"))
                .andExpect(jsonPath("$.character.id").value(character2.getId()));
    }

    @Test
    @DisplayName("통합 테스트: 대사 순서 정렬 확인")
    void getDialoguesSortedByOrder_Integration_Success() throws Exception {
        // 역순으로 대사 생성
        Dialogue dialogue3 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("세 번째 대사")
                .dialogueOrder(3)
                .build();
        dialogueRepository.save(dialogue3);

        Dialogue dialogue1 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("첫 번째 대사")
                .dialogueOrder(1)
                .build();
        dialogueRepository.save(dialogue1);

        Dialogue dialogue2 = Dialogue.builder()
                .scene(testScene)
                .character(testCharacter)
                .text("두 번째 대사")
                .dialogueOrder(2)
                .build();
        dialogueRepository.save(dialogue2);

        // 순서대로 조회되는지 확인
        mockMvc.perform(get("/dialogue/scene/{sceneId}", testScene.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dialogueOrder").value(1))
                .andExpect(jsonPath("$[1].dialogueOrder").value(2))
                .andExpect(jsonPath("$[2].dialogueOrder").value(3));
    }
}
