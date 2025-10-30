package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.RelationshipRepository;
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
class RelationshipIntegrationTest {

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
    private RelationshipRepository relationshipRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Character character1;
    private Character character2;

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

        // 테스트 캐릭터 2개 생성
        character1 = new Character();
        character1.setCharacterId("char_001");
        character1.setName("캐릭터 1");
        character1.setDescription("첫 번째 캐릭터");
        character1.setProject(testProject);
        character1 = characterRepository.save(character1);

        character2 = new Character();
        character2.setCharacterId("char_002");
        character2.setName("캐릭터 2");
        character2.setDescription("두 번째 캐릭터");
        character2.setProject(testProject);
        character2 = characterRepository.save(character2);
    }

    @Test
    @DisplayName("통합 테스트: 관계 생성")
    void createRelationship_Integration_Success() throws Exception {
        Relationship request = new Relationship();
        request.setFromCharacter(character1);
        request.setToCharacter(character2);
        request.setRelationType("friend");
        request.setCloseness(8.5);
        request.setDescription("오랜 친구");

        mockMvc.perform(post("/relationships")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationType").value("friend"))
                .andExpect(jsonPath("$.closeness").value(8.5))
                .andExpect(jsonPath("$.fromCharacter.id").value(character1.getId()))
                .andExpect(jsonPath("$.toCharacter.id").value(character2.getId()));
    }

    @Test
    @DisplayName("통합 테스트: 모든 관계 조회")
    void getAllRelationships_Integration_Success() throws Exception {
        // 테스트 관계 생성
        Relationship rel1 = new Relationship();
        rel1.setFromCharacter(character1);
        rel1.setToCharacter(character2);
        rel1.setRelationType("friend");
        rel1.setCloseness(7.0);
        relationshipRepository.save(rel1);

        mockMvc.perform(get("/relationships")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].relationType").value("friend"));
    }

    @Test
    @DisplayName("통합 테스트: 관계 ID로 조회")
    void getRelationshipById_Integration_Success() throws Exception {
        Relationship relationship = new Relationship();
        relationship.setFromCharacter(character1);
        relationship.setToCharacter(character2);
        relationship.setRelationType("rival");
        relationship.setCloseness(3.0);
        relationship = relationshipRepository.save(relationship);

        mockMvc.perform(get("/relationships/{id}", relationship.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relationType").value("rival"))
                .andExpect(jsonPath("$.closeness").value(3.0));
    }

    @Test
    @DisplayName("통합 테스트: 캐릭터별 관계 조회")
    void getRelationshipsForCharacter_Integration_Success() throws Exception {
        // character1의 관계 생성
        Relationship rel1 = new Relationship();
        rel1.setFromCharacter(character1);
        rel1.setToCharacter(character2);
        rel1.setRelationType("friend");
        rel1.setCloseness(8.0);
        relationshipRepository.save(rel1);

        mockMvc.perform(get("/relationships/character/{characterId}", character1.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("통합 테스트: 관계 업데이트")
    void updateRelationship_Integration_Success() throws Exception {
        // 관계 생성
        Relationship relationship = new Relationship();
        relationship.setFromCharacter(character1);
        relationship.setToCharacter(character2);
        relationship.setRelationType("friend");
        relationship.setCloseness(5.0);
        relationship = relationshipRepository.save(relationship);

        // 업데이트 요청
        Relationship updateRequest = new Relationship();
        updateRequest.setFromCharacter(character1);
        updateRequest.setToCharacter(character2);
        updateRequest.setRelationType("best friend");
        updateRequest.setCloseness(9.5);
        updateRequest.setDescription("가장 친한 친구");

        mockMvc.perform(put("/relationships/{id}", relationship.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relationType").value("best friend"))
                .andExpect(jsonPath("$.closeness").value(9.5))
                .andExpect(jsonPath("$.description").value("가장 친한 친구"));
    }

    @Test
    @DisplayName("통합 테스트: 관계 삭제")
    void deleteRelationship_Integration_Success() throws Exception {
        // 관계 생성
        Relationship relationship = new Relationship();
        relationship.setFromCharacter(character1);
        relationship.setToCharacter(character2);
        relationship.setRelationType("temporary");
        relationship.setCloseness(1.0);
        relationship = relationshipRepository.save(relationship);

        Long relationshipId = relationship.getId();

        // 삭제
        mockMvc.perform(delete("/relationships/{id}", relationshipId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/relationships/{id}", relationshipId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통합 테스트: 그래프 데이터 조회")
    void getGraphData_Integration_Success() throws Exception {
        // 관계 생성
        Relationship rel1 = new Relationship();
        rel1.setFromCharacter(character1);
        rel1.setToCharacter(character2);
        rel1.setRelationType("friend");
        rel1.setCloseness(8.0);
        relationshipRepository.save(rel1);

        mockMvc.perform(get("/relationships/graph")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.nodes", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.edges", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("통합 테스트: 인증 없이 관계 접근 - 실패")
    void accessRelationshipWithoutAuth_Integration_Failure() throws Exception {
        mockMvc.perform(get("/relationships"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("통합 테스트: 존재하지 않는 관계 조회 - 실패")
    void getRelationshipById_NotFound_Failure() throws Exception {
        mockMvc.perform(get("/relationships/{id}", 99999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}
