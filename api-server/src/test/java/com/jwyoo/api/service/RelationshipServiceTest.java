package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.RelationshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RelationshipService 단위 테스트
 * 캐릭터 간 관계 CRUD 테스트
 */
@ExtendWith(MockitoExtension.class)
class RelationshipServiceTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @InjectMocks
    private RelationshipService relationshipService;

    private User testUser;
    private Project testProject;
    private Character character1;
    private Character character2;
    private Relationship testRelationship;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .build();

        character1 = Character.builder()
                .id(1L)
                .characterId("char001")
                .name("Character 1")
                .project(testProject)
                .build();

        character2 = Character.builder()
                .id(2L)
                .characterId("char002")
                .name("Character 2")
                .project(testProject)
                .build();

        testRelationship = Relationship.builder()
                .id(1L)
                .fromCharacter(character1)
                .toCharacter(character2)
                .relationType("친구")
                .closeness(0.8)
                .description("친한 친구 사이")
                .build();
    }

    @Test
    @DisplayName("모든 관계 조회 성공")
    void getAllRelationships_Success() {
        // given
        Relationship relationship2 = Relationship.builder()
                .id(2L)
                .fromCharacter(character2)
                .toCharacter(character1)
                .relationType("동료")
                .closeness(0.6)
                .build();
        List<Relationship> relationships = Arrays.asList(testRelationship, relationship2);
        when(relationshipRepository.findAll()).thenReturn(relationships);

        // when
        List<Relationship> result = relationshipService.getAllRelationships();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRelationType()).isEqualTo("친구");
        assertThat(result.get(1).getRelationType()).isEqualTo("동료");

        verify(relationshipRepository).findAll();
    }

    @Test
    @DisplayName("ID로 관계 조회 성공")
    void getRelationshipById_Success() {
        // given
        Long relationshipId = 1L;
        when(relationshipRepository.findById(relationshipId)).thenReturn(Optional.of(testRelationship));

        // when
        Relationship result = relationshipService.getRelationshipById(relationshipId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(relationshipId);
        assertThat(result.getRelationType()).isEqualTo("친구");
        assertThat(result.getCloseness()).isEqualTo(0.8);

        verify(relationshipRepository).findById(relationshipId);
    }

    @Test
    @DisplayName("ID로 관계 조회 실패 - 존재하지 않음")
    void getRelationshipById_Failure_NotFound() {
        // given
        Long relationshipId = 999L;
        when(relationshipRepository.findById(relationshipId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> relationshipService.getRelationshipById(relationshipId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Relationship");

        verify(relationshipRepository).findById(relationshipId);
    }

    @Test
    @DisplayName("특정 캐릭터가 시작점인 관계 조회 성공")
    void getRelationshipsByFromCharacterId_Success() {
        // given
        Long characterId = 1L;
        List<Relationship> relationships = Arrays.asList(testRelationship);
        when(relationshipRepository.findByFromCharacterId(characterId)).thenReturn(relationships);

        // when
        List<Relationship> result = relationshipService.getRelationshipsByFromCharacterId(characterId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromCharacter().getId()).isEqualTo(characterId);

        verify(relationshipRepository).findByFromCharacterId(characterId);
    }

    @Test
    @DisplayName("특정 캐릭터가 대상인 관계 조회 성공")
    void getRelationshipsByToCharacterId_Success() {
        // given
        Long characterId = 2L;
        List<Relationship> relationships = Arrays.asList(testRelationship);
        when(relationshipRepository.findByToCharacterId(characterId)).thenReturn(relationships);

        // when
        List<Relationship> result = relationshipService.getRelationshipsByToCharacterId(characterId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getToCharacter().getId()).isEqualTo(characterId);

        verify(relationshipRepository).findByToCharacterId(characterId);
    }

    @Test
    @DisplayName("특정 캐릭터와 관련된 모든 관계 조회 성공")
    void getAllRelationshipsForCharacter_Success() {
        // given
        Long characterId = 1L;
        Relationship relationship2 = Relationship.builder()
                .id(2L)
                .fromCharacter(character2)
                .toCharacter(character1)
                .relationType("동료")
                .closeness(0.6)
                .build();
        List<Relationship> fromRelationships = new java.util.ArrayList<>(Arrays.asList(testRelationship));
        List<Relationship> toRelationships = Arrays.asList(relationship2);
        when(relationshipRepository.findByFromCharacterId(characterId)).thenReturn(fromRelationships);
        when(relationshipRepository.findByToCharacterId(characterId)).thenReturn(toRelationships);

        // when
        List<Relationship> result = relationshipService.getAllRelationshipsForCharacter(characterId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        verify(relationshipRepository).findByFromCharacterId(characterId);
        verify(relationshipRepository).findByToCharacterId(characterId);
    }

    @Test
    @DisplayName("관계 생성 성공")
    void createRelationship_Success() {
        // given
        Relationship newRelationship = Relationship.builder()
                .fromCharacter(character1)
                .toCharacter(character2)
                .relationType("가족")
                .closeness(0.9)
                .description("형제")
                .build();
        Relationship savedRelationship = Relationship.builder()
                .id(3L)
                .fromCharacter(character1)
                .toCharacter(character2)
                .relationType("가족")
                .closeness(0.9)
                .description("형제")
                .build();
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(savedRelationship);

        // when
        Relationship result = relationshipService.createRelationship(newRelationship);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getRelationType()).isEqualTo("가족");
        assertThat(result.getCloseness()).isEqualTo(0.9);

        verify(relationshipRepository).save(newRelationship);
    }

    @Test
    @DisplayName("관계 수정 성공")
    void updateRelationship_Success() {
        // given
        Long relationshipId = 1L;
        Relationship updateData = Relationship.builder()
                .relationType("절친")
                .closeness(0.95)
                .description("매우 친한 사이")
                .build();
        when(relationshipRepository.findById(relationshipId)).thenReturn(Optional.of(testRelationship));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(testRelationship);

        // when
        Relationship result = relationshipService.updateRelationship(relationshipId, updateData);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationType()).isEqualTo("절친");
        assertThat(result.getCloseness()).isEqualTo(0.95);
        assertThat(result.getDescription()).isEqualTo("매우 친한 사이");

        verify(relationshipRepository).findById(relationshipId);
        verify(relationshipRepository).save(testRelationship);
    }

    @Test
    @DisplayName("관계 삭제 성공")
    void deleteRelationship_Success() {
        // given
        Long relationshipId = 1L;
        when(relationshipRepository.existsById(relationshipId)).thenReturn(true);
        doNothing().when(relationshipRepository).deleteById(relationshipId);

        // when
        relationshipService.deleteRelationship(relationshipId);

        // then
        verify(relationshipRepository).existsById(relationshipId);
        verify(relationshipRepository).deleteById(relationshipId);
    }

    @Test
    @DisplayName("관계 삭제 실패 - 존재하지 않음")
    void deleteRelationship_Failure_NotFound() {
        // given
        Long relationshipId = 999L;
        when(relationshipRepository.existsById(relationshipId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> relationshipService.deleteRelationship(relationshipId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Relationship");

        verify(relationshipRepository).existsById(relationshipId);
        verify(relationshipRepository, never()).deleteById(any());
    }
}