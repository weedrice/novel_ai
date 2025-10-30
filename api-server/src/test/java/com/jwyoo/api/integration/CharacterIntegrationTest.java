package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.CharacterDto;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.CharacterRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CharacterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Long projectId;
    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        // JWT 토큰 생성
        authToken = jwtTokenProvider.generateToken(testUser.getUsername());

        // 테스트 프로젝트 생성
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setOwner(testUser);
        testProject = projectRepository.save(testProject);
        projectId = testProject.getId();
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터 생성")
    void createCharacter_Integration_Success() throws Exception {
        CharacterDto request = CharacterDto.builder()
                .characterId("char_001")
                .name("테스트 캐릭터")
                .description("테스트 설명")
                .personality("밝고 긍정적")
                .build();

        mockMvc.perform(post("/characters")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트 캐릭터"))
                .andExpect(jsonPath("$.characterId").value("char_001"));
    }

    @Test
    @DisplayName("통합 테스트: 프로젝트별 캐릭터 목록 조회")
    void getCharactersByProject_Integration_Success() throws Exception {
        // 테스트 캐릭터 생성
        Character char1 = new Character();
        char1.setCharacterId("char_001");
        char1.setName("캐릭터 1");
        char1.setProject(testProject);
        characterRepository.save(char1);

        Character char2 = new Character();
        char2.setCharacterId("char_002");
        char2.setName("캐릭터 2");
        char2.setProject(testProject);
        characterRepository.save(char2);

        mockMvc.perform(get("/characters")
                        .header("Authorization", "Bearer " + authToken)
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("캐릭터 1"))
                .andExpect(jsonPath("$[1].name").value("캐릭터 2"));
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터 ID로 조회")
    void getCharacterById_Integration_Success() throws Exception {
        Character character = new Character();
        character.setCharacterId("char_001");
        character.setName("테스트 캐릭터");
        character.setDescription("테스트 설명");
        character.setProject(testProject);
        character = characterRepository.save(character);

        mockMvc.perform(get("/characters/{id}", character.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 캐릭터"))
                .andExpect(jsonPath("$.characterId").value("char_001"));
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터 업데이트")
    void updateCharacter_Integration_Success() throws Exception {
        // 캐릭터 생성
        Character character = new Character();
        character.setCharacterId("char_001");
        character.setName("원본 이름");
        character.setProject(testProject);
        character = characterRepository.save(character);

        // 업데이트 요청
        CharacterDto updateRequest = CharacterDto.builder()
                .characterId("char_001")
                .name("수정된 이름")
                .description("수정된 설명")
                .build();

        mockMvc.perform(put("/characters/{id}", character.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 이름"))
                .andExpect(jsonPath("$.description").value("수정된 설명"));
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터 삭제")
    void deleteCharacter_Integration_Success() throws Exception {
        // 캐릭터 생성
        Character character = new Character();
        character.setCharacterId("char_001");
        character.setName("삭제될 캐릭터");
        character.setProject(testProject);
        character = characterRepository.save(character);

        Long characterId = character.getId();

        // 삭제
        mockMvc.perform(delete("/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통합 테스트: 인증 없이 캐릭터 접근 - 실패")
    void accessCharacterWithoutAuth_Integration_Failure() throws Exception {
        mockMvc.perform(get("/characters"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("통합 테스트: 존재하지 않는 캐릭터 조회 - 실패")
    void getCharacterById_NotFound_Failure() throws Exception {
        mockMvc.perform(get("/characters/{id}", 99999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통합 테스트: 잘못된 요청 데이터로 캐릭터 생성 - 실패")
    void createCharacter_InvalidData_Failure() throws Exception {
        CharacterDto request = CharacterDto.builder().build();
        // name 필드를 누락

        mockMvc.perform(post("/characters")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통합 테스트: 전체 캐릭터 CRUD 플로우")
    void fullCRUDFlow_Integration_Success() throws Exception {
        // 1. Create
        CharacterDto createRequest = CharacterDto.builder()
                .characterId("char_crud")
                .name("CRUD 테스트")
                .build();

        String createResponse = mockMvc.perform(post("/characters")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Character createdCharacter = objectMapper.readValue(createResponse, Character.class);
        Long characterId = createdCharacter.getId();

        // 2. Read
        mockMvc.perform(get("/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD 테스트"));

        // 3. Update
        CharacterDto updateRequest = CharacterDto.builder()
                .characterId("char_crud")
                .name("CRUD 업데이트")
                .build();

        mockMvc.perform(put("/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD 업데이트"));

        // 4. Delete
        mockMvc.perform(delete("/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 5. Verify deletion
        mockMvc.perform(get("/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}
