package com.jwyoo.api.service;

import com.jwyoo.api.entity.AIAnalysis;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.repository.AIAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AIAnalysisService 단위 테스트
 * AI 분석 결과 CRUD 및 비교 테스트
 */
@ExtendWith(MockitoExtension.class)
class AIAnalysisServiceTest {

    @Mock
    private AIAnalysisRepository aiAnalysisRepository;

    @Mock
    private EpisodeService episodeService;

    @InjectMocks
    private AIAnalysisService aiAnalysisService;

    private User testUser;
    private Project testProject;
    private Episode testEpisode;
    private AIAnalysis testAnalysis;

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
                .title("Test Episode")
                .description("Test Description")
                .episodeOrder(1)
                .project(testProject)
                .scriptText("Test script")
                .scriptFormat("novel")
                .analysisStatus("analyzed")
                .build();

        testAnalysis = AIAnalysis.builder()
                .id(1L)
                .episode(testEpisode)
                .analysisType("character_extraction")
                .modelName("gpt-4")
                .result("{\"characters\": []}")
                .confidence(0.85)
                .status("completed")
                .build();
    }

    @Test
    @DisplayName("AI 분석 생성 성공")
    void createAnalysis_Success() {
        // given
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(testAnalysis);

        // when
        AIAnalysis created = aiAnalysisService.createAnalysis(testAnalysis);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getAnalysisType()).isEqualTo("character_extraction");
        assertThat(created.getModelName()).isEqualTo("gpt-4");
        verify(aiAnalysisRepository, times(1)).save(any(AIAnalysis.class));
    }

    @Test
    @DisplayName("에피소드 ID로 분석 생성 성공")
    void createAnalysisForEpisode_Success() {
        // given
        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(testAnalysis);

        // when
        AIAnalysis created = aiAnalysisService.createAnalysisForEpisode(
            1L, "character_extraction", "gpt-4", "{\"characters\": []}", 0.85);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getEpisode()).isEqualTo(testEpisode);
        verify(episodeService, times(1)).getEpisodeById(1L);
        verify(aiAnalysisRepository, times(1)).save(any(AIAnalysis.class));
    }

    @Test
    @DisplayName("ID로 분석 조회 성공")
    void getAnalysisById_Success() {
        // given
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(testAnalysis));

        // when
        AIAnalysis found = aiAnalysisService.getAnalysisById(1L);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        verify(aiAnalysisRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ID로 분석 조회 실패 - 존재하지 않음")
    void getAnalysisById_NotFound() {
        // given
        when(aiAnalysisRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> aiAnalysisService.getAnalysisById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI analysis not found");
    }

    @Test
    @DisplayName("에피소드별 분석 목록 조회 성공")
    void getAnalysesByEpisodeId_Success() {
        // given
        AIAnalysis analysis2 = AIAnalysis.builder()
                .id(2L)
                .episode(testEpisode)
                .analysisType("dialogue_extraction")
                .modelName("gpt-4")
                .result("{\"dialogues\": []}")
                .confidence(0.90)
                .status("completed")
                .build();

        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        when(aiAnalysisRepository.findByEpisode_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testAnalysis, analysis2));

        // when
        List<AIAnalysis> analyses = aiAnalysisService.getAnalysesByEpisodeId(1L);

        // then
        assertThat(analyses).hasSize(2);
        assertThat(analyses.get(0).getAnalysisType()).isEqualTo("character_extraction");
        assertThat(analyses.get(1).getAnalysisType()).isEqualTo("dialogue_extraction");
        verify(episodeService, times(1)).getEpisodeById(1L);
    }

    @Test
    @DisplayName("에피소드 및 분석 유형별 조회 성공")
    void getAnalysesByEpisodeIdAndType_Success() {
        // given
        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        when(aiAnalysisRepository.findByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(1L, "character_extraction"))
                .thenReturn(Arrays.asList(testAnalysis));

        // when
        List<AIAnalysis> analyses = aiAnalysisService.getAnalysesByEpisodeIdAndType(1L, "character_extraction");

        // then
        assertThat(analyses).hasSize(1);
        assertThat(analyses.get(0).getAnalysisType()).isEqualTo("character_extraction");
    }

    @Test
    @DisplayName("최신 분석 결과 조회 성공")
    void getLatestAnalysisByEpisodeIdAndType_Success() {
        // given
        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        when(aiAnalysisRepository.findFirstByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(1L, "character_extraction"))
                .thenReturn(Optional.of(testAnalysis));

        // when
        AIAnalysis latest = aiAnalysisService.getLatestAnalysisByEpisodeIdAndType(1L, "character_extraction");

        // then
        assertThat(latest).isNotNull();
        assertThat(latest.getAnalysisType()).isEqualTo("character_extraction");
    }

    @Test
    @DisplayName("여러 모델 분석 결과 비교 성공")
    void compareModelAnalyses_Success() {
        // given
        AIAnalysis gpt4Analysis = AIAnalysis.builder()
                .id(1L)
                .episode(testEpisode)
                .analysisType("character_extraction")
                .modelName("gpt-4")
                .result("{\"characters\": []}")
                .confidence(0.85)
                .build();

        AIAnalysis claudeAnalysis = AIAnalysis.builder()
                .id(2L)
                .episode(testEpisode)
                .analysisType("character_extraction")
                .modelName("claude-3")
                .result("{\"characters\": []}")
                .confidence(0.90)
                .build();

        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        when(aiAnalysisRepository.findByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(1L, "character_extraction"))
                .thenReturn(Arrays.asList(gpt4Analysis, claudeAnalysis));

        // when
        Map<String, AIAnalysis> comparison = aiAnalysisService.compareModelAnalyses(1L, "character_extraction");

        // then
        assertThat(comparison).hasSize(2);
        assertThat(comparison).containsKeys("gpt-4", "claude-3");
        assertThat(comparison.get("gpt-4").getConfidence()).isEqualTo(0.85);
        assertThat(comparison.get("claude-3").getConfidence()).isEqualTo(0.90);
    }

    @Test
    @DisplayName("분석 삭제 성공")
    void deleteAnalysis_Success() {
        // given
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(testAnalysis));
        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        doNothing().when(aiAnalysisRepository).delete(testAnalysis);

        // when
        aiAnalysisService.deleteAnalysis(1L);

        // then
        verify(aiAnalysisRepository, times(1)).delete(testAnalysis);
    }

    @Test
    @DisplayName("에피소드의 분석 개수 조회 성공")
    void countAnalysesByEpisodeId_Success() {
        // given
        when(episodeService.getEpisodeById(1L)).thenReturn(testEpisode);
        when(aiAnalysisRepository.countByEpisode_Id(1L)).thenReturn(5L);

        // when
        long count = aiAnalysisService.countAnalysesByEpisodeId(1L);

        // then
        assertThat(count).isEqualTo(5L);
        verify(aiAnalysisRepository, times(1)).countByEpisode_Id(1L);
    }
}
