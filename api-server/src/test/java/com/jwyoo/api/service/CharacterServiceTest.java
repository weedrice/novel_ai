package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.ProjectRepository;
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
 * CharacterService 단위 테스트
 * 캐릭터 CRUD 및 말투 프로필 관리 테스트
 */
@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private CharacterService characterService;

    private User testUser;
    private Project testProject;
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

        testCharacter = Character.builder()
                .id(1L)
                .characterId("char001")
                .name("Test Character")
                .description("Test character description")
                .personality("Friendly and outgoing")
                .speakingStyle("Casual and warm")
                .vocabulary("친구, 안녕")
                .toneKeywords("친근한, 명랑한")
                .project(testProject)
                .build();
    }

    @Test
    @DisplayName("모든 캐릭터 조회 성공")
    void getAllCharacters_Success() {
        // given
        Character character2 = Character.builder()
                .id(2L)
                .characterId("char002")
                .name("Character 2")
                .project(testProject)
                .build();
        List<Character> characters = Arrays.asList(testCharacter, character2);
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByProject(testProject)).thenReturn(characters);

        // when
        List<Character> result = characterService.getAllCharacters();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Test Character");
        assertThat(result.get(1).getName()).isEqualTo("Character 2");

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByProject(testProject);
    }

    @Test
    @DisplayName("ID로 캐릭터 조회 성공")
    void getCharacterById_Success() {
        // given
        Long characterId = 1L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByIdAndProject(characterId, testProject))
                .thenReturn(Optional.of(testCharacter));

        // when
        Character result = characterService.getCharacterById(characterId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(characterId);
        assertThat(result.getName()).isEqualTo("Test Character");

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByIdAndProject(characterId, testProject);
    }

    @Test
    @DisplayName("ID로 캐릭터 조회 실패 - 존재하지 않음")
    void getCharacterById_Failure_NotFound() {
        // given
        Long characterId = 999L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByIdAndProject(characterId, testProject))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> characterService.getCharacterById(characterId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByIdAndProject(characterId, testProject);
    }

    @Test
    @DisplayName("characterId로 캐릭터 조회 성공")
    void getCharacterByCharacterId_Success() {
        // given
        String characterId = "char001";
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByCharacterIdAndProject(characterId, testProject))
                .thenReturn(Optional.of(testCharacter));

        // when
        Character result = characterService.getCharacterByCharacterId(characterId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCharacterId()).isEqualTo(characterId);
        assertThat(result.getName()).isEqualTo("Test Character");

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByCharacterIdAndProject(characterId, testProject);
    }

    @Test
    @DisplayName("characterId로 캐릭터 조회 실패 - 존재하지 않음")
    void getCharacterByCharacterId_Failure_NotFound() {
        // given
        String characterId = "nonexistent";
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByCharacterIdAndProject(characterId, testProject))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> characterService.getCharacterByCharacterId(characterId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByCharacterIdAndProject(characterId, testProject);
    }

    @Test
    @DisplayName("캐릭터 생성 성공")
    void createCharacter_Success() {
        // given
        Character newCharacter = Character.builder()
                .characterId("char002")
                .name("New Character")
                .description("New description")
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.existsByCharacterIdAndProject("char002", testProject))
                .thenReturn(false);
        when(characterRepository.save(any(Character.class))).thenReturn(newCharacter);

        // when
        Character result = characterService.createCharacter(newCharacter);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCharacterId()).isEqualTo("char002");
        assertThat(result.getName()).isEqualTo("New Character");

        verify(projectService).getCurrentProject();
        verify(characterRepository).existsByCharacterIdAndProject("char002", testProject);
        verify(characterRepository).save(any(Character.class));
    }

    @Test
    @DisplayName("캐릭터 생성 실패 - 중복된 characterId")
    void createCharacter_Failure_DuplicateCharacterId() {
        // given
        Character newCharacter = Character.builder()
                .characterId("char001")
                .name("Duplicate Character")
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.existsByCharacterIdAndProject("char001", testProject))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> characterService.createCharacter(newCharacter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 캐릭터 ID입니다");

        verify(projectService).getCurrentProject();
        verify(characterRepository).existsByCharacterIdAndProject("char001", testProject);
        verify(characterRepository, never()).save(any(Character.class));
    }

    @Test
    @DisplayName("캐릭터 수정 성공")
    void updateCharacter_Success() {
        // given
        Long characterId = 1L;
        Character updateData = Character.builder()
                .name("Updated Character")
                .description("Updated description")
                .personality("Updated personality")
                .speakingStyle("Updated style")
                .vocabulary("Updated vocab")
                .toneKeywords("Updated tone")
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByIdAndProject(characterId, testProject))
                .thenReturn(Optional.of(testCharacter));
        when(characterRepository.save(any(Character.class))).thenReturn(testCharacter);

        // when
        Character result = characterService.updateCharacter(characterId, updateData);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Character");
        assertThat(result.getDescription()).isEqualTo("Updated description");

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByIdAndProject(characterId, testProject);
        verify(characterRepository).save(testCharacter);
    }

    @Test
    @DisplayName("캐릭터 삭제 성공")
    void deleteCharacter_Success() {
        // given
        Long characterId = 1L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByIdAndProject(characterId, testProject))
                .thenReturn(Optional.of(testCharacter));
        doNothing().when(characterRepository).delete(testCharacter);

        // when
        characterService.deleteCharacter(characterId);

        // then
        verify(projectService).getCurrentProject();
        verify(characterRepository).findByIdAndProject(characterId, testProject);
        verify(characterRepository).delete(testCharacter);
    }

    @Test
    @DisplayName("말투 프로필 수정 성공")
    void updateSpeakingProfile_Success() {
        // given
        Long characterId = 1L;
        Character profileUpdate = Character.builder()
                .speakingStyle("New speaking style")
                .vocabulary("새로운, 어휘")
                .toneKeywords("공손한, 정중한")
                .examples("예시 대사 1, 예시 대사 2")
                .prohibitedWords("금지어1, 금지어2")
                .sentencePatterns("~습니다, ~입니다")
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(characterRepository.findByIdAndProject(characterId, testProject))
                .thenReturn(Optional.of(testCharacter));
        when(characterRepository.save(any(Character.class))).thenReturn(testCharacter);

        // when
        Character result = characterService.updateSpeakingProfile(characterId, profileUpdate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSpeakingStyle()).isEqualTo("New speaking style");
        assertThat(result.getVocabulary()).isEqualTo("새로운, 어휘");
        assertThat(result.getToneKeywords()).isEqualTo("공손한, 정중한");

        verify(projectService).getCurrentProject();
        verify(characterRepository).findByIdAndProject(characterId, testProject);
        verify(characterRepository).save(testCharacter);
    }
}