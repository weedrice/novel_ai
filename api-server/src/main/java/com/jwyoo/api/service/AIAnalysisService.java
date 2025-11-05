package com.jwyoo.api.service;

import com.jwyoo.api.entity.AIAnalysis;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.repository.AIAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 분석 결과 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIAnalysisService {

    private final AIAnalysisRepository aiAnalysisRepository;
    private final EpisodeService episodeService;

    /**
     * 새로운 AI 분석 결과 생성
     */
    @Transactional
    public AIAnalysis createAnalysis(AIAnalysis analysis) {
        log.info("Creating new AI analysis: episodeId={}, type={}, model={}",
            analysis.getEpisode().getId(), analysis.getAnalysisType(), analysis.getModelName());

        AIAnalysis saved = aiAnalysisRepository.save(analysis);
        log.info("AI analysis created successfully: id={}", saved.getId());
        return saved;
    }

    /**
     * 에피소드 ID로 AI 분석 결과 생성 (편의 메서드)
     */
    @Transactional
    public AIAnalysis createAnalysisForEpisode(Long episodeId, String analysisType, String modelName,
                                                String result, Double confidence) {
        Episode episode = episodeService.getEpisodeById(episodeId);

        AIAnalysis analysis = AIAnalysis.builder()
            .episode(episode)
            .analysisType(analysisType)
            .modelName(modelName)
            .result(result)
            .confidence(confidence)
            .status("completed")
            .build();

        return createAnalysis(analysis);
    }

    /**
     * ID로 분석 결과 조회
     */
    public AIAnalysis getAnalysisById(Long id) {
        log.debug("Fetching AI analysis by id: {}", id);
        return aiAnalysisRepository.findById(id)
            .orElseThrow(() -> {
                log.error("AI analysis not found with id: {}", id);
                return new IllegalArgumentException("AI analysis not found: " + id);
            });
    }

    /**
     * 특정 에피소드의 모든 분석 결과 조회 (최신순)
     */
    public List<AIAnalysis> getAnalysesByEpisodeId(Long episodeId) {
        log.debug("Fetching all analyses for episode: {}", episodeId);
        // episodeService.getEpisodeById()를 통해 프로젝트 권한 검증
        episodeService.getEpisodeById(episodeId);

        List<AIAnalysis> analyses = aiAnalysisRepository.findByEpisode_IdOrderByCreatedAtDesc(episodeId);
        log.info("Found {} analyses for episode: {}", analyses.size(), episodeId);
        return analyses;
    }

    /**
     * 특정 에피소드의 특정 분석 유형 결과 조회 (최신순)
     */
    public List<AIAnalysis> getAnalysesByEpisodeIdAndType(Long episodeId, String analysisType) {
        log.debug("Fetching analyses for episode: {}, type: {}", episodeId, analysisType);
        // episodeService.getEpisodeById()를 통해 프로젝트 권한 검증
        episodeService.getEpisodeById(episodeId);

        List<AIAnalysis> analyses = aiAnalysisRepository.findByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(
            episodeId, analysisType);
        log.info("Found {} analyses for episode: {}, type: {}", analyses.size(), episodeId, analysisType);
        return analyses;
    }

    /**
     * 특정 에피소드의 특정 분석 유형 + 모델 결과 조회 (최신순)
     */
    public List<AIAnalysis> getAnalysesByEpisodeIdAndTypeAndModel(Long episodeId, String analysisType, String modelName) {
        log.debug("Fetching analyses for episode: {}, type: {}, model: {}", episodeId, analysisType, modelName);
        // episodeService.getEpisodeById()를 통해 프로젝트 권한 검증
        episodeService.getEpisodeById(episodeId);

        List<AIAnalysis> analyses = aiAnalysisRepository.findByEpisode_IdAndAnalysisTypeAndModelNameOrderByCreatedAtDesc(
            episodeId, analysisType, modelName);
        log.info("Found {} analyses for episode: {}, type: {}, model: {}",
            analyses.size(), episodeId, analysisType, modelName);
        return analyses;
    }

    /**
     * 특정 에피소드의 최신 분석 결과 1개 조회 (분석 유형별)
     */
    public AIAnalysis getLatestAnalysisByEpisodeIdAndType(Long episodeId, String analysisType) {
        log.debug("Fetching latest analysis for episode: {}, type: {}", episodeId, analysisType);
        // episodeService.getEpisodeById()를 통해 프로젝트 권한 검증
        episodeService.getEpisodeById(episodeId);

        return aiAnalysisRepository.findFirstByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(episodeId, analysisType)
            .orElseThrow(() -> {
                log.error("No analysis found for episode: {}, type: {}", episodeId, analysisType);
                return new IllegalArgumentException("No analysis found for episode: " + episodeId + ", type: " + analysisType);
            });
    }

    /**
     * 여러 AI 모델의 분석 결과 비교
     * 같은 에피소드, 같은 분석 유형에 대해 여러 모델의 결과를 반환
     */
    public Map<String, AIAnalysis> compareModelAnalyses(Long episodeId, String analysisType) {
        log.debug("Comparing model analyses for episode: {}, type: {}", episodeId, analysisType);

        List<AIAnalysis> analyses = getAnalysesByEpisodeIdAndType(episodeId, analysisType);

        // 모델별로 가장 최신 결과만 그룹화
        Map<String, AIAnalysis> latestByModel = analyses.stream()
            .collect(Collectors.toMap(
                AIAnalysis::getModelName,
                analysis -> analysis,
                (existing, replacement) -> existing // 이미 최신순 정렬이므로 첫 번째 유지
            ));

        log.info("Found {} different models for episode: {}, type: {}",
            latestByModel.size(), episodeId, analysisType);
        return latestByModel;
    }

    /**
     * 분석 결과 삭제
     */
    @Transactional
    public void deleteAnalysis(Long id) {
        log.info("Deleting AI analysis: id={}", id);
        AIAnalysis analysis = getAnalysisById(id);

        // 프로젝트 권한 검증 (Episode를 통해 간접적으로)
        episodeService.getEpisodeById(analysis.getEpisode().getId());

        aiAnalysisRepository.delete(analysis);
        log.info("AI analysis deleted successfully: id={}", id);
    }

    /**
     * 특정 에피소드의 모든 분석 결과 삭제
     */
    @Transactional
    public void deleteAllAnalysesByEpisodeId(Long episodeId) {
        log.info("Deleting all analyses for episode: {}", episodeId);
        // episodeService.getEpisodeById()를 통해 프로젝트 권한 검증
        Episode episode = episodeService.getEpisodeById(episodeId);

        List<AIAnalysis> analyses = aiAnalysisRepository.findByEpisodeOrderByCreatedAtDesc(episode);
        aiAnalysisRepository.deleteAll(analyses);
        log.info("Deleted {} analyses for episode: {}", analyses.size(), episodeId);
    }

    /**
     * 분석 상태별 조회
     */
    public List<AIAnalysis> getAnalysesByStatus(String status) {
        log.debug("Fetching analyses with status: {}", status);
        List<AIAnalysis> analyses = aiAnalysisRepository.findByStatusOrderByCreatedAtDesc(status);
        log.info("Found {} analyses with status: {}", analyses.size(), status);
        return analyses;
    }

    /**
     * 특정 에피소드의 분석 개수 조회
     */
    public long countAnalysesByEpisodeId(Long episodeId) {
        log.debug("Counting analyses for episode: {}", episodeId);
        // episodeService.getEpisodeById()를 통해 프로젝트 권한 검증
        episodeService.getEpisodeById(episodeId);

        long count = aiAnalysisRepository.countByEpisode_Id(episodeId);
        log.debug("Episode {} has {} analyses", episodeId, count);
        return count;
    }
}
