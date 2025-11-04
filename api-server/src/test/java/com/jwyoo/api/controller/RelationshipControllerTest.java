package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.CharacterService;
import com.jwyoo.api.service.RelationshipService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RelationshipController.class,
        excludeAutoConfiguration = {CacheAutoConfiguration.class, RedisAutoConfiguration.class})
@ActiveProfiles("test")
class RelationshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RelationshipService relationshipService;

    @MockBean
    private CharacterService characterService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("모든 관계 조회 성공")
    @WithMockUser
    void getAllRelationships_Success() throws Exception {
        // given
        Relationship relationship = new Relationship();
        relationship.setId(1L);
        relationship.setRelationType("friend");

        when(relationshipService.getAllRelationships()).thenReturn(Arrays.asList(relationship));

        // when & then
        mockMvc.perform(get("/relationships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].relationType").value("friend"));

        verify(relationshipService).getAllRelationships();
    }

    @Test
    @DisplayName("관계 상세 조회 성공")
    @WithMockUser
    void getRelationshipById_Success() throws Exception {
        // given
        Relationship relationship = new Relationship();
        relationship.setId(1L);
        relationship.setRelationType("friend");

        when(relationshipService.getRelationshipById(1L)).thenReturn(relationship);

        // when & then
        mockMvc.perform(get("/relationships/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.relationType").value("friend"));

        verify(relationshipService).getRelationshipById(1L);
    }

    @Test
    @DisplayName("캐릭터별 관계 조회 성공")
    @WithMockUser
    void getRelationshipsForCharacter_Success() throws Exception {
        // given
        Relationship relationship = new Relationship();
        relationship.setId(1L);

        when(relationshipService.getAllRelationshipsForCharacter(1L)).thenReturn(Arrays.asList(relationship));

        // when & then
        mockMvc.perform(get("/relationships/character/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(relationshipService).getAllRelationshipsForCharacter(1L);
    }

    @Test
    @DisplayName("관계 생성 성공")
    @WithMockUser
    void createRelationship_Success() throws Exception {
        // given
        Relationship relationship = new Relationship();
        relationship.setRelationType("friend");
        relationship.setCloseness(7.5);

        Relationship createdRelationship = new Relationship();
        createdRelationship.setId(1L);
        createdRelationship.setRelationType("friend");
        createdRelationship.setCloseness(7.5);

        when(relationshipService.createRelationship(any())).thenReturn(createdRelationship);

        // when & then
        mockMvc.perform(post("/relationships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationship)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.relationType").value("friend"));

        verify(relationshipService).createRelationship(any());
    }

    @Test
    @DisplayName("관계 수정 성공")
    @WithMockUser
    void updateRelationship_Success() throws Exception {
        // given
        Relationship relationship = new Relationship();
        relationship.setRelationType("rival");
        relationship.setCloseness(3.0);

        Relationship updatedRelationship = new Relationship();
        updatedRelationship.setId(1L);
        updatedRelationship.setRelationType("rival");
        updatedRelationship.setCloseness(3.0);

        when(relationshipService.updateRelationship(eq(1L), any())).thenReturn(updatedRelationship);

        // when & then
        mockMvc.perform(put("/relationships/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationship)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.relationType").value("rival"));

        verify(relationshipService).updateRelationship(eq(1L), any());
    }

    @Test
    @DisplayName("관계 삭제 성공")
    @WithMockUser
    void deleteRelationship_Success() throws Exception {
        // given
        doNothing().when(relationshipService).deleteRelationship(1L);

        // when & then
        mockMvc.perform(delete("/relationships/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(relationshipService).deleteRelationship(1L);
    }

    @Test
    @DisplayName("그래프 데이터 조회 성공")
    @WithMockUser
    void getGraphData_Success() throws Exception {
        // given
        Character char1 = new Character();
        char1.setId(1L);
        char1.setName("Alice");
        char1.setCharacterId("char1");

        Character char2 = new Character();
        char2.setId(2L);
        char2.setName("Bob");
        char2.setCharacterId("char2");

        Relationship relationship = new Relationship();
        relationship.setId(1L);
        relationship.setFromCharacter(char1);
        relationship.setToCharacter(char2);
        relationship.setRelationType("friend");
        relationship.setCloseness(8.0);

        when(characterService.getAllCharacters()).thenReturn(Arrays.asList(char1, char2));
        when(relationshipService.getAllRelationships()).thenReturn(Arrays.asList(relationship));

        // when & then
        mockMvc.perform(get("/relationships/graph")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.edges.length()").value(1))
                .andExpect(jsonPath("$.nodes[0].label").value("Alice"))
                .andExpect(jsonPath("$.nodes[1].label").value("Bob"))
                .andExpect(jsonPath("$.edges[0].label").value("friend"))
                .andExpect(jsonPath("$.edges[0].closeness").value(8.0));

        verify(characterService).getAllCharacters();
        verify(relationshipService).getAllRelationships();
    }

    @Test
    @DisplayName("그래프 데이터 조회 - 빈 데이터")
    @WithMockUser
    void getGraphData_Empty() throws Exception {
        // given
        when(characterService.getAllCharacters()).thenReturn(Arrays.asList());
        when(relationshipService.getAllRelationships()).thenReturn(Arrays.asList());

        // when & then
        mockMvc.perform(get("/relationships/graph")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(0))
                .andExpect(jsonPath("$.edges.length()").value(0));

        verify(characterService).getAllCharacters();
        verify(relationshipService).getAllRelationships();
    }
}
