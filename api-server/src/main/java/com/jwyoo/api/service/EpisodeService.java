package com.jwyoo.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.AIAnalysis;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.repository.EpisodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 에피소드 비즈니스 로직을 처리하는 서비스
 * Task 90: Redis 캐싱 적용
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;
    private final AIAnalysisService aiAnalysisService;

    @Value("${LLM_BASE_URL:http://localhost:8000}")
    private String llmBaseUrl;

    // Circular dependency 방지를 위해 @Lazy 사용
    public EpisodeService(EpisodeRepository episodeRepository,
                         ProjectService projectService,
                         ObjectMapper objectMapper,
                         @Lazy AIAnalysisService aiAnalysisService) {
        this.episodeRepository = episodeRepository;
        this.projectService = projectService;
        this.objectMapper = objectMapper;
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * 모든 에피소드 조회 (프로젝트별)
     * Task 90: Redis 캐싱 적용 - 10분 TTL
     */
    @Cacheable(value = "episodes", key = "#root.method.name + '_' + @projectService.getCurrentProject().id")
    public List<Episode> getAllEpisodes() {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching all episodes for project: {} (from DB, not cache)", currentProject.getId());
        List<Episode> episodes = episodeRepository.findByProjectOrderByEpisodeOrderAsc(currentProject);
        log.info("Fetched {} episodes", episodes.size());
        return episodes;
    }

    /**
     * ID로 에피소드 조회 (프로젝트별)
     */
    public Episode getEpisodeById(Long id) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching episode by id: {}, project: {}", id, currentProject.getId());
        Episode episode = episodeRepository.findByIdAndProject(id, currentProject)
            .orElseThrow(() -> {
                log.error("Episode not found with id: {} in project: {}", id, currentProject.getId());
                return new IllegalArgumentException("Episode not found: " + id);
            });
        log.debug("Found episode: id={}, title={}", episode.getId(), episode.getTitle());
        return episode;
    }

    /**
     * 새로운 에피소드 생성 (현재 프로젝트에 자동 연결)
     * Task 90: 캐시 무효화
     */
    @Transactional
    @CacheEvict(value = "episodes", allEntries = true)
    public Episode createEpisode(Episode episode) {
        Project currentProject = projectService.getCurrentProject();
        log.info("Creating new episode: title={}, order={}, project={}",
                episode.getTitle(), episode.getEpisodeOrder(), currentProject.getId());

        // 현재 프로젝트 자동 설정
        episode.setProject(currentProject);

        Episode saved = episodeRepository.save(episode);
        log.info("Episode created successfully: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 에피소드 수정 (프로젝트별)
     * Task 90: 캐시 무효화
     */
    @Transactional
    @CacheEvict(value = "episodes", allEntries = true)
    public Episode updateEpisode(Long id, Episode episode) {
        log.info("Updating episode: id={}, newTitle={}, newOrder={}",
            id, episode.getTitle(), episode.getEpisodeOrder());
        Episode existing = getEpisodeById(id); // 이미 프로젝트 확인 포함

        String oldTitle = existing.getTitle();
        Integer oldOrder = existing.getEpisodeOrder();

        // 기본 정보 업데이트
        if (episode.getTitle() != null) {
            existing.setTitle(episode.getTitle());
        }
        if (episode.getDescription() != null) {
            existing.setDescription(episode.getDescription());
        }
        if (episode.getEpisodeOrder() != null) {
            existing.setEpisodeOrder(episode.getEpisodeOrder());
        }

        // 스크립트 텍스트 업데이트
        if (episode.getScriptText() != null) {
            existing.setScriptText(episode.getScriptText());
            log.debug("Updated scriptText for episode: id={}, length={}", id, episode.getScriptText().length());
        }
        if (episode.getScriptFormat() != null) {
            existing.setScriptFormat(episode.getScriptFormat());
        }

        Episode updated = episodeRepository.save(existing);
        log.info("Episode updated: id={}, title: {} -> {}, order: {} -> {}",
            id, oldTitle, updated.getTitle(), oldOrder, updated.getEpisodeOrder());
        return updated;
    }

    /**
     * 에피소드 삭제 (프로젝트별)
     * Task 90: 캐시 무효화
     */
    @Transactional
    @CacheEvict(value = "episodes", allEntries = true)
    public void deleteEpisode(Long id) {
        log.info("Deleting episode: id={}", id);

        Episode episode = getEpisodeById(id); // 이미 프로젝트 확인 포함

        episodeRepository.delete(episode);
        log.info("Episode deleted successfully: id={}", id);
    }

    /**
     * 에피소드 스크립트 업로드 및 분석
     */
    @Transactional
    @CacheEvict(value = "episodes", allEntries = true)
    public Episode uploadAndAnalyzeScript(Long episodeId, String scriptText, String scriptFormat, String provider) {
        log.info("Uploading and analyzing script for episode: id={}, format={}, provider={}", episodeId, scriptFormat, provider);

        Episode episode = getEpisodeById(episodeId);

        // 스크립트 설정
        episode.setScriptText(scriptText);
        episode.setScriptFormat(scriptFormat);
        episode.setAnalysisStatus("analyzing");
        episode.setLlmProvider(provider);

        Episode saved = episodeRepository.save(episode);

        // 비동기 분석 수행 (별도 트랜잭션)
        try {
            analyzeEpisodeScript(episodeId, provider);
        } catch (Exception e) {
            log.error("Failed to analyze episode script: episodeId={}, error={}", episodeId, e.getMessage());
            episode.setAnalysisStatus("failed");
            episodeRepository.save(episode);
        }

        return saved;
    }

    /**
     * 에피소드 스크립트 분석 (LLM 서버 호출)
     */
    @Transactional
    @CacheEvict(value = "episodes", allEntries = true)
    public Episode analyzeEpisodeScript(Long episodeId, String provider) {
        log.info("Analyzing episode script: id={}, provider={}", episodeId, provider);

        Episode episode = getEpisodeById(episodeId);

        if (episode.getScriptText() == null || episode.getScriptText().isEmpty()) {
            log.error("No script text found for episode: {}", episodeId);
            throw new IllegalArgumentException("Episode has no script text to analyze");
        }

        episode.setAnalysisStatus("analyzing");
        episodeRepository.save(episode);

        try {
            // LLM 서버에 분석 요청
            Map<String, Object> analysisRequest = Map.of(
                    "content", episode.getScriptText(),
                    "formatHint", episode.getScriptFormat() != null ? episode.getScriptFormat() : "",
                    "provider", provider != null ? provider : "openai"
            );

            log.info("Calling LLM server for episode script analysis: url={}/gen/analyze-script", llmBaseUrl);

            RestClient restClient = RestClient.builder().build();
            Map<String, Object> analysisResult = restClient.post()
                    .uri(llmBaseUrl + "/gen/analyze-script")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(analysisRequest)
                    .retrieve()
                    .body(Map.class);

            // 분석 결과를 JSON으로 저장
            String resultJson = objectMapper.writeValueAsString(analysisResult);
            episode.setAnalysisResult(resultJson);
            episode.setLlmProvider(provider);
            episode.setAnalysisStatus("analyzed");

            log.info("Episode script analysis completed: id={}, characters={}, dialogues={}, scenes={}, relationships={}",
                    episodeId,
                    analysisResult.get("characters") != null ? ((List<?>) analysisResult.get("characters")).size() : 0,
                    analysisResult.get("dialogues") != null ? ((List<?>) analysisResult.get("dialogues")).size() : 0,
                    analysisResult.get("scenes") != null ? ((List<?>) analysisResult.get("scenes")).size() : 0,
                    analysisResult.get("relationships") != null ? ((List<?>) analysisResult.get("relationships")).size() : 0
            );

            Episode savedEpisode = episodeRepository.save(episode);

            // AIAnalysis 엔티티로 분석 결과 저장 (구조화된 데이터)
            try {
                saveAnalysisResultToAIAnalysis(savedEpisode, resultJson, provider);
            } catch (Exception e) {
                log.error("Failed to save analysis result to AIAnalysis: episodeId={}, error={}", episodeId, e.getMessage());
                // AIAnalysis 저장 실패는 전체 트랜잭션을 롤백하지 않음
            }

            return savedEpisode;

        } catch (Exception e) {
            log.error("Failed to analyze episode script: id={}, error={}", episodeId, e.getMessage(), e);
            episode.setAnalysisStatus("failed");
            episodeRepository.save(episode);
            throw new RuntimeException("Episode script analysis failed: " + e.getMessage(), e);
        }
    }

    /**
     * 분석 결과를 AIAnalysis 엔티티로 저장
     * Episode의 analysisResult JSON을 분석 유형별로 분리하여 저장
     */
    private void saveAnalysisResultToAIAnalysis(Episode episode, String resultJson, String provider) {
        log.debug("Saving analysis result to AIAnalysis: episodeId={}", episode.getId());

        try {
            Map<String, Object> analysisResult = objectMapper.readValue(resultJson, Map.class);

            // 1. 캐릭터 추출 분석 저장
            if (analysisResult.containsKey("characters")) {
                String charactersJson = objectMapper.writeValueAsString(
                    Map.of("characters", analysisResult.get("characters"))
                );
                AIAnalysis characterAnalysis = AIAnalysis.builder()
                    .episode(episode)
                    .analysisType("character_extraction")
                    .modelName(provider)
                    .result(charactersJson)
                    .confidence(0.85) // 기본 신뢰도
                    .status("completed")
                    .build();
                aiAnalysisService.createAnalysis(characterAnalysis);
                log.debug("Character extraction analysis saved: episodeId={}", episode.getId());
            }

            // 2. 대사 추출 분석 저장
            if (analysisResult.containsKey("dialogues")) {
                String dialoguesJson = objectMapper.writeValueAsString(
                    Map.of("dialogues", analysisResult.get("dialogues"))
                );
                AIAnalysis dialogueAnalysis = AIAnalysis.builder()
                    .episode(episode)
                    .analysisType("dialogue_extraction")
                    .modelName(provider)
                    .result(dialoguesJson)
                    .confidence(0.85)
                    .status("completed")
                    .build();
                aiAnalysisService.createAnalysis(dialogueAnalysis);
                log.debug("Dialogue extraction analysis saved: episodeId={}", episode.getId());
            }

            // 3. 장면 추출 분석 저장
            if (analysisResult.containsKey("scenes")) {
                String scenesJson = objectMapper.writeValueAsString(
                    Map.of("scenes", analysisResult.get("scenes"))
                );
                AIAnalysis sceneAnalysis = AIAnalysis.builder()
                    .episode(episode)
                    .analysisType("scene_extraction")
                    .modelName(provider)
                    .result(scenesJson)
                    .confidence(0.85)
                    .status("completed")
                    .build();
                aiAnalysisService.createAnalysis(sceneAnalysis);
                log.debug("Scene extraction analysis saved: episodeId={}", episode.getId());
            }

            // 4. 관계 추출 분석 저장
            if (analysisResult.containsKey("relationships")) {
                String relationshipsJson = objectMapper.writeValueAsString(
                    Map.of("relationships", analysisResult.get("relationships"))
                );
                AIAnalysis relationshipAnalysis = AIAnalysis.builder()
                    .episode(episode)
                    .analysisType("relationship_extraction")
                    .modelName(provider)
                    .result(relationshipsJson)
                    .confidence(0.85)
                    .status("completed")
                    .build();
                aiAnalysisService.createAnalysis(relationshipAnalysis);
                log.debug("Relationship extraction analysis saved: episodeId={}", episode.getId());
            }

            log.info("All analysis results saved to AIAnalysis: episodeId={}", episode.getId());

        } catch (Exception e) {
            log.error("Failed to save analysis result to AIAnalysis: episodeId={}, error={}",
                episode.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save analysis result: " + e.getMessage(), e);
        }
    }

    /**
     * 에피소드 스크립트 분석 결과 조회 (JSON 파싱)
     */
    public Map<String, Object> getScriptAnalysisResult(Long episodeId) {
        log.debug("Fetching script analysis result for episode: {}", episodeId);

        Episode episode = getEpisodeById(episodeId);

        if (episode.getAnalysisResult() == null || episode.getAnalysisResult().isEmpty()) {
            log.warn("No analysis result found for episode: {}", episodeId);
            return Map.of(
                    "characters", List.of(),
                    "dialogues", List.of(),
                    "scenes", List.of(),
                    "relationships", List.of()
            );
        }

        try {
            Map<String, Object> result = objectMapper.readValue(episode.getAnalysisResult(), Map.class);
            log.info("Analysis result retrieved: episodeId={}, hasData={}", episodeId, !result.isEmpty());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse analysis result: episodeId={}, error={}", episodeId, e.getMessage());
            throw new RuntimeException("Failed to parse analysis result: " + e.getMessage(), e);
        }
    }
}