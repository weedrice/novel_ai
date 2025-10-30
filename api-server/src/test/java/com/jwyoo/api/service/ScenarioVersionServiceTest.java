package com.jwyoo.api.service;

import com.jwyoo.api.entity.*;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.ScenarioVersionRepository;
import com.jwyoo.api.repository.SceneRepository;
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
 * ScenarioVersionService 단위 테스트
 * 시나리오 버전 관리 테스트
 */
@ExtendWith(MockitoExtension.class)
class ScenarioVersionServiceTest {

    @Mock
    private ScenarioVersionRepository scenarioVersionRepository;

    @Mock
    private SceneRepository sceneRepository;

    @InjectMocks
    private ScenarioVersionService scenarioVersionService;

    private User testUser;
    private Project testProject;
    private Episode testEpisode;
    private Scene testScene;
    private ScenarioVersion testVersion;

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

        testScene = Scene.builder()
                .id(1L)
                .sceneNumber(1)
                .location("Classroom")
                .episode(testEpisode)
                .build();

        testVersion = ScenarioVersion.builder()
                .id(1L)
                .scene(testScene)
                .version(1)
                .title("First Version")
                .content("{\"dialogues\": []}")
                .createdBy("testuser")
                .build();
    }

    @Test
    @DisplayName("시나리오 버전 저장 성공 - 첫 번째 버전")
    void saveVersion_Success_FirstVersion() {
        // given
        Long sceneId = 1L;
        String title = "First Version";
        String content = "{\"dialogues\": []}";
        String createdBy = "testuser";

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(testScene));
        when(scenarioVersionRepository.findMaxVersionBySceneId(sceneId)).thenReturn(Optional.empty());
        when(scenarioVersionRepository.save(any(ScenarioVersion.class))).thenReturn(testVersion);

        // when
        ScenarioVersion result = scenarioVersionService.saveVersion(sceneId, title, content, createdBy);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getTitle()).isEqualTo("First Version");

        verify(sceneRepository).findById(sceneId);
        verify(scenarioVersionRepository).findMaxVersionBySceneId(sceneId);
        verify(scenarioVersionRepository).save(any(ScenarioVersion.class));
    }

    @Test
    @DisplayName("시나리오 버전 저장 성공 - 다음 버전")
    void saveVersion_Success_NextVersion() {
        // given
        Long sceneId = 1L;
        String title = "Second Version";
        String content = "{\"dialogues\": []}";
        String createdBy = "testuser";

        ScenarioVersion secondVersion = ScenarioVersion.builder()
                .id(2L)
                .scene(testScene)
                .version(2)
                .title("Second Version")
                .content(content)
                .createdBy(createdBy)
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(testScene));
        when(scenarioVersionRepository.findMaxVersionBySceneId(sceneId)).thenReturn(Optional.of(1));
        when(scenarioVersionRepository.save(any(ScenarioVersion.class))).thenReturn(secondVersion);

        // when
        ScenarioVersion result = scenarioVersionService.saveVersion(sceneId, title, content, createdBy);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getTitle()).isEqualTo("Second Version");

        verify(sceneRepository).findById(sceneId);
        verify(scenarioVersionRepository).findMaxVersionBySceneId(sceneId);
        verify(scenarioVersionRepository).save(any(ScenarioVersion.class));
    }

    @Test
    @DisplayName("시나리오 버전 저장 실패 - 장면 존재하지 않음")
    void saveVersion_Failure_SceneNotFound() {
        // given
        Long sceneId = 999L;
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scenarioVersionService.saveVersion(sceneId, "Title", "Content", "user"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sceneRepository).findById(sceneId);
        verify(scenarioVersionRepository, never()).save(any());
    }

    @Test
    @DisplayName("장면의 모든 버전 조회 성공")
    void getVersionsBySceneId_Success() {
        // given
        Long sceneId = 1L;
        ScenarioVersion version2 = ScenarioVersion.builder()
                .id(2L)
                .scene(testScene)
                .version(2)
                .title("Second Version")
                .content("{}")
                .createdBy("testuser")
                .build();
        List<ScenarioVersion> versions = Arrays.asList(version2, testVersion);
        when(scenarioVersionRepository.findBySceneIdOrderByVersionDesc(sceneId)).thenReturn(versions);

        // when
        List<ScenarioVersion> result = scenarioVersionService.getVersionsBySceneId(sceneId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo(2);
        assertThat(result.get(1).getVersion()).isEqualTo(1);

        verify(scenarioVersionRepository).findBySceneIdOrderByVersionDesc(sceneId);
    }

    @Test
    @DisplayName("특정 버전 조회 성공")
    void getVersionById_Success() {
        // given
        Long versionId = 1L;
        when(scenarioVersionRepository.findById(versionId)).thenReturn(Optional.of(testVersion));

        // when
        ScenarioVersion result = scenarioVersionService.getVersionById(versionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(versionId);
        assertThat(result.getVersion()).isEqualTo(1);

        verify(scenarioVersionRepository).findById(versionId);
    }

    @Test
    @DisplayName("특정 버전 조회 실패 - 존재하지 않음")
    void getVersionById_Failure_NotFound() {
        // given
        Long versionId = 999L;
        when(scenarioVersionRepository.findById(versionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scenarioVersionService.getVersionById(versionId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(scenarioVersionRepository).findById(versionId);
    }

    @Test
    @DisplayName("장면 ID와 버전 번호로 조회 성공")
    void getVersionBySceneIdAndVersion_Success() {
        // given
        Long sceneId = 1L;
        Integer version = 1;
        when(scenarioVersionRepository.findBySceneIdAndVersion(sceneId, version))
                .thenReturn(Optional.of(testVersion));

        // when
        ScenarioVersion result = scenarioVersionService.getVersionBySceneIdAndVersion(sceneId, version);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScene().getId()).isEqualTo(sceneId);
        assertThat(result.getVersion()).isEqualTo(version);

        verify(scenarioVersionRepository).findBySceneIdAndVersion(sceneId, version);
    }

    @Test
    @DisplayName("장면 ID와 버전 번호로 조회 실패 - 존재하지 않음")
    void getVersionBySceneIdAndVersion_Failure_NotFound() {
        // given
        Long sceneId = 1L;
        Integer version = 999;
        when(scenarioVersionRepository.findBySceneIdAndVersion(sceneId, version))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scenarioVersionService.getVersionBySceneIdAndVersion(sceneId, version))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(scenarioVersionRepository).findBySceneIdAndVersion(sceneId, version);
    }

    @Test
    @DisplayName("버전 삭제 성공")
    void deleteVersion_Success() {
        // given
        Long versionId = 1L;
        when(scenarioVersionRepository.findById(versionId)).thenReturn(Optional.of(testVersion));
        doNothing().when(scenarioVersionRepository).delete(testVersion);

        // when
        scenarioVersionService.deleteVersion(versionId);

        // then
        verify(scenarioVersionRepository).findById(versionId);
        verify(scenarioVersionRepository).delete(testVersion);
    }

    @Test
    @DisplayName("버전 삭제 실패 - 존재하지 않음")
    void deleteVersion_Failure_NotFound() {
        // given
        Long versionId = 999L;
        when(scenarioVersionRepository.findById(versionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scenarioVersionService.deleteVersion(versionId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(scenarioVersionRepository).findById(versionId);
        verify(scenarioVersionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("버전 개수 조회 성공")
    void countVersions_Success() {
        // given
        Long sceneId = 1L;
        when(scenarioVersionRepository.countBySceneId(sceneId)).thenReturn(5L);

        // when
        long result = scenarioVersionService.countVersions(sceneId);

        // then
        assertThat(result).isEqualTo(5L);

        verify(scenarioVersionRepository).countBySceneId(sceneId);
    }
}