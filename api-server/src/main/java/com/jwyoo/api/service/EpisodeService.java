package com.jwyoo.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    @Value("${LLM_BASE_URL:http://localhost:8000}")
    private String llmBaseUrl;

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

        existing.setTitle(episode.getTitle());
        existing.setDescription(episode.getDescription());
        existing.setEpisodeOrder(episode.getEpisodeOrder());

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

            return episodeRepository.save(episode);

        } catch (Exception e) {
            log.error("Failed to analyze episode script: id={}, error={}", episodeId, e.getMessage(), e);
            episode.setAnalysisStatus("failed");
            episodeRepository.save(episode);
            throw new RuntimeException("Episode script analysis failed: " + e.getMessage(), e);
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