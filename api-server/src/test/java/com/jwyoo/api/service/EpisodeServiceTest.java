package com.jwyoo.api.service;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.repository.EpisodeRepository;
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
 * EpisodeService 단위 테스트
 * 에피소드 CRUD 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class EpisodeServiceTest {

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private EpisodeService episodeService;

    private User testUser;
    private Project testProject;
    private Episode testEpisode;

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
    }

    @Test
    @DisplayName("모든 에피소드 조회 성공")
    void getAllEpisodes_Success() {
        // given
        Episode episode2 = Episode.builder()
                .id(2L)
                .title("Episode 2")
                .description("Second episode")
                .episodeOrder(2)
                .project(testProject)
                .build();
        List<Episode> episodes = Arrays.asList(testEpisode, episode2);
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.findByProjectOrderByEpisodeOrderAsc(testProject)).thenReturn(episodes);

        // when
        List<Episode> result = episodeService.getAllEpisodes();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Episode 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Episode 2");
        assertThat(result.get(0).getEpisodeOrder()).isEqualTo(1);
        assertThat(result.get(1).getEpisodeOrder()).isEqualTo(2);

        verify(projectService).getCurrentProject();
        verify(episodeRepository).findByProjectOrderByEpisodeOrderAsc(testProject);
    }

    @Test
    @DisplayName("ID로 에피소드 조회 성공")
    void getEpisodeById_Success() {
        // given
        Long episodeId = 1L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.findByIdAndProject(episodeId, testProject))
                .thenReturn(Optional.of(testEpisode));

        // when
        Episode result = episodeService.getEpisodeById(episodeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(episodeId);
        assertThat(result.getTitle()).isEqualTo("Episode 1");

        verify(projectService).getCurrentProject();
        verify(episodeRepository).findByIdAndProject(episodeId, testProject);
    }

    @Test
    @DisplayName("ID로 에피소드 조회 실패 - 존재하지 않음")
    void getEpisodeById_Failure_NotFound() {
        // given
        Long episodeId = 999L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.findByIdAndProject(episodeId, testProject))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> episodeService.getEpisodeById(episodeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Episode not found");

        verify(projectService).getCurrentProject();
        verify(episodeRepository).findByIdAndProject(episodeId, testProject);
    }

    @Test
    @DisplayName("에피소드 생성 성공")
    void createEpisode_Success() {
        // given
        Episode newEpisode = Episode.builder()
                .title("New Episode")
                .description("New description")
                .episodeOrder(3)
                .build();
        Episode savedEpisode = Episode.builder()
                .id(3L)
                .title("New Episode")
                .description("New description")
                .episodeOrder(3)
                .project(testProject)
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.save(any(Episode.class))).thenReturn(savedEpisode);

        // when
        Episode result = episodeService.createEpisode(newEpisode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTitle()).isEqualTo("New Episode");
        assertThat(result.getProject()).isEqualTo(testProject);

        verify(projectService).getCurrentProject();
        verify(episodeRepository).save(any(Episode.class));
    }

    @Test
    @DisplayName("에피소드 수정 성공")
    void updateEpisode_Success() {
        // given
        Long episodeId = 1L;
        Episode updateData = Episode.builder()
                .title("Updated Episode")
                .description("Updated description")
                .episodeOrder(5)
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.findByIdAndProject(episodeId, testProject))
                .thenReturn(Optional.of(testEpisode));
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        // when
        Episode result = episodeService.updateEpisode(episodeId, updateData);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Episode");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getEpisodeOrder()).isEqualTo(5);

        verify(projectService).getCurrentProject();
        verify(episodeRepository).findByIdAndProject(episodeId, testProject);
        verify(episodeRepository).save(testEpisode);
    }

    @Test
    @DisplayName("에피소드 삭제 성공")
    void deleteEpisode_Success() {
        // given
        Long episodeId = 1L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.findByIdAndProject(episodeId, testProject))
                .thenReturn(Optional.of(testEpisode));
        doNothing().when(episodeRepository).delete(testEpisode);

        // when
        episodeService.deleteEpisode(episodeId);

        // then
        verify(projectService).getCurrentProject();
        verify(episodeRepository).findByIdAndProject(episodeId, testProject);
        verify(episodeRepository).delete(testEpisode);
    }

    @Test
    @DisplayName("에피소드 삭제 실패 - 존재하지 않음")
    void deleteEpisode_Failure_NotFound() {
        // given
        Long episodeId = 999L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(episodeRepository.findByIdAndProject(episodeId, testProject))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> episodeService.deleteEpisode(episodeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Episode not found");

        verify(projectService).getCurrentProject();
        verify(episodeRepository).findByIdAndProject(episodeId, testProject);
        verify(episodeRepository, never()).delete(any(Episode.class));
    }
}