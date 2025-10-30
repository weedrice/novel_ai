package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.DialogueRepository;
import com.jwyoo.api.repository.SceneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SceneService 단위 테스트
 * 장면 CRUD 및 대사 관리 테스트
 */
@ExtendWith(MockitoExtension.class)
class SceneServiceTest {

    @Mock
    private SceneRepository sceneRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private DialogueRepository dialogueRepository;

    @InjectMocks
    private SceneService sceneService;

    private User testUser;
    private Project testProject;
    private Episode testEpisode;
    private Scene testScene;
    private Character testCharacter;

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

        testEpisode = Episode.builder()
                .id(1L)
                .title("Episode 1")
                .description("First episode")
                .episodeOrder(1)
                .project(testProject)
                .build();

        testCharacter = Character.builder()
                .id(1L)
                .characterId("char001")
                .name("Test Character")
                .project(testProject)
                .build();

        testScene = Scene.builder()
                .id(1L)
                .sceneNumber(1)
                .location("Classroom")
                .mood("Tense")
                .description("A tense scene in the classroom")
                .participants("char001,char002")
                .episode(testEpisode)
                .dialogues(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("모든 장면 조회 성공")
    void getAllScenes_Success() {
        // given
        Scene scene2 = Scene.builder()
                .id(2L)
                .sceneNumber(2)
                .location("Library")
                .episode(testEpisode)
                .build();
        List<Scene> scenes = Arrays.asList(testScene, scene2);
        when(sceneRepository.findAll()).thenReturn(scenes);

        // when
        List<Scene> result = sceneService.getAllScenes();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocation()).isEqualTo("Classroom");
        assertThat(result.get(1).getLocation()).isEqualTo("Library");

        verify(sceneRepository).findAll();
    }

    @Test
    @DisplayName("ID로 장면 조회 성공")
    void getSceneById_Success() {
        // given
        Long sceneId = 1L;
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(testScene));

        // when
        Scene result = sceneService.getSceneById(sceneId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sceneId);
        assertThat(result.getLocation()).isEqualTo("Classroom");

        verify(sceneRepository).findById(sceneId);
    }

    @Test
    @DisplayName("ID로 장면 조회 실패 - 존재하지 않음")
    void getSceneById_Failure_NotFound() {
        // given
        Long sceneId = 999L;
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sceneService.getSceneById(sceneId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sceneRepository).findById(sceneId);
    }

    @Test
    @DisplayName("에피소드별 장면 목록 조회 성공")
    void getScenesByEpisodeId_Success() {
        // given
        Long episodeId = 1L;
        List<Scene> scenes = Arrays.asList(testScene);
        when(sceneRepository.findByEpisodeIdOrderBySceneNumberAsc(episodeId)).thenReturn(scenes);

        // when
        List<Scene> result = sceneService.getScenesByEpisodeId(episodeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEpisode().getId()).isEqualTo(episodeId);

        verify(sceneRepository).findByEpisodeIdOrderBySceneNumberAsc(episodeId);
    }

    @Test
    @DisplayName("참여 캐릭터 목록 조회 성공")
    void getParticipants_Success() {
        // given
        Character character2 = Character.builder()
                .id(2L)
                .characterId("char002")
                .name("Character 2")
                .project(testProject)
                .build();
        when(characterRepository.findByCharacterId("char001")).thenReturn(Optional.of(testCharacter));
        when(characterRepository.findByCharacterId("char002")).thenReturn(Optional.of(character2));

        // when
        List<Character> result = sceneService.getParticipants(testScene);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCharacterId()).isEqualTo("char001");
        assertThat(result.get(1).getCharacterId()).isEqualTo("char002");

        verify(characterRepository).findByCharacterId("char001");
        verify(characterRepository).findByCharacterId("char002");
    }

    @Test
    @DisplayName("참여 캐릭터 목록 조회 - 참여자 없음")
    void getParticipants_EmptyParticipants() {
        // given
        Scene sceneWithoutParticipants = Scene.builder()
                .id(2L)
                .sceneNumber(2)
                .location("Library")
                .participants(null)
                .episode(testEpisode)
                .build();

        // when
        List<Character> result = sceneService.getParticipants(sceneWithoutParticipants);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(characterRepository, never()).findByCharacterId(any());
    }

    @Test
    @DisplayName("장면 생성 성공")
    void createScene_Success() {
        // given
        Scene newScene = Scene.builder()
                .sceneNumber(3)
                .location("Cafeteria")
                .mood("Relaxed")
                .episode(testEpisode)
                .build();
        when(sceneRepository.save(any(Scene.class))).thenReturn(newScene);

        // when
        Scene result = sceneService.createScene(newScene);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLocation()).isEqualTo("Cafeteria");
        assertThat(result.getMood()).isEqualTo("Relaxed");

        verify(sceneRepository).save(newScene);
    }

    @Test
    @DisplayName("장면 수정 성공")
    void updateScene_Success() {
        // given
        Long sceneId = 1L;
        Scene updateData = Scene.builder()
                .sceneNumber(5)
                .location("Updated Location")
                .mood("Updated Mood")
                .description("Updated description")
                .participants("char001")
                .build();
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(testScene));
        when(sceneRepository.save(any(Scene.class))).thenReturn(testScene);

        // when
        Scene result = sceneService.updateScene(sceneId, updateData);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSceneNumber()).isEqualTo(5);
        assertThat(result.getLocation()).isEqualTo("Updated Location");
        assertThat(result.getMood()).isEqualTo("Updated Mood");

        verify(sceneRepository).findById(sceneId);
        verify(sceneRepository).save(testScene);
    }

    @Test
    @DisplayName("장면 삭제 성공")
    void deleteScene_Success() {
        // given
        Long sceneId = 1L;
        when(sceneRepository.existsById(sceneId)).thenReturn(true);
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(testScene));
        doNothing().when(sceneRepository).deleteById(sceneId);

        // when
        sceneService.deleteScene(sceneId);

        // then
        verify(sceneRepository).existsById(sceneId);
        verify(sceneRepository).findById(sceneId);
        verify(sceneRepository).deleteById(sceneId);
    }

    @Test
    @DisplayName("장면 삭제 실패 - 존재하지 않음")
    void deleteScene_Failure_NotFound() {
        // given
        Long sceneId = 999L;
        when(sceneRepository.existsById(sceneId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> sceneService.deleteScene(sceneId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sceneRepository).existsById(sceneId);
        verify(sceneRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("장면에 대사 추가 성공")
    void addDialogue_Success() {
        // given
        Long sceneId = 1L;
        Dialogue dialogue = Dialogue.builder()
                .character(testCharacter)
                .text("Hello, world!")
                .build();
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(testScene));
        when(sceneRepository.save(any(Scene.class))).thenReturn(testScene);

        // when
        Scene result = sceneService.addDialogue(sceneId, dialogue);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDialogues()).hasSize(1);
        assertThat(dialogue.getScene()).isEqualTo(testScene);
        assertThat(dialogue.getDialogueOrder()).isEqualTo(1);

        verify(sceneRepository).findById(sceneId);
        verify(sceneRepository).save(testScene);
    }

    @Test
    @DisplayName("장면의 대사 목록 조회 성공")
    void getDialogues_Success() {
        // given
        Long sceneId = 1L;
        Dialogue dialogue1 = Dialogue.builder()
                .id(1L)
                .character(testCharacter)
                .text("First dialogue")
                .dialogueOrder(1)
                .scene(testScene)
                .build();
        Dialogue dialogue2 = Dialogue.builder()
                .id(2L)
                .character(testCharacter)
                .text("Second dialogue")
                .dialogueOrder(2)
                .scene(testScene)
                .build();
        List<Dialogue> dialogues = Arrays.asList(dialogue1, dialogue2);
        when(dialogueRepository.findBySceneIdOrderByDialogueOrderAsc(sceneId)).thenReturn(dialogues);

        // when
        List<Dialogue> result = sceneService.getDialogues(sceneId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getText()).isEqualTo("First dialogue");
        assertThat(result.get(1).getText()).isEqualTo("Second dialogue");

        verify(dialogueRepository).findBySceneIdOrderByDialogueOrderAsc(sceneId);
    }
}