package com.jwyoo.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Script;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 스크립트 분석 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    @Value("${LLM_BASE_URL:http://localhost:8000}")
    private String llmBaseUrl;

    /**
     * 모든 스크립트 조회 (최신순, 프로젝트별)
     */
    public List<Script> getAllScripts() {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching all scripts for project: {}", currentProject.getId());
        List<Script> scripts = scriptRepository.findByProjectOrderByCreatedAtDesc(currentProject);
        log.info("Fetched {} scripts", scripts.size());
        return scripts;
    }

    /**
     * ID로 스크립트 조회 (프로젝트별)
     */
    public Script getScriptById(Long id) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching script by id: {}, project: {}", id, currentProject.getId());
        Script script = scriptRepository.findByIdAndProject(id, currentProject)
                .orElseThrow(() -> {
                    log.error("Script not found with id: {} in project: {}", id, currentProject.getId());
                    return new ResourceNotFoundException("Script", id);
                });
        log.debug("Found script: id={}, title={}, status={}", script.getId(), script.getTitle(), script.getStatus());
        return script;
    }

    /**
     * 상태별 스크립트 조회 (프로젝트별)
     */
    public List<Script> getScriptsByStatus(String status) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching scripts by status: {}, project: {}", status, currentProject.getId());
        List<Script> scripts = scriptRepository.findByProjectAndStatus(currentProject, status);
        log.info("Found {} scripts with status: {}", scripts.size(), status);
        return scripts;
    }

    /**
     * 스크립트 업로드 (현재 프로젝트에 자동 연결)
     */
    @Transactional
    public Script uploadScript(String title, String content, String formatHint) {
        Project currentProject = projectService.getCurrentProject();
        log.info("Uploading new script: title={}, length={} chars, format={}, project={}",
                title, content.length(), formatHint, currentProject.getId());

        Script script = Script.builder()
                .title(title)
                .content(content)
                .formatHint(formatHint)
                .status("uploaded")
                .project(currentProject)
                .build();

        Script saved = scriptRepository.save(script);
        log.info("Script uploaded successfully: id={}", saved.getId());
        return saved;
    }

    /**
     * 스크립트 분석 (LLM 서버 호출)
     */
    @Transactional
    public Script analyzeScript(Long scriptId, String provider) {
        log.info("Analyzing script: id={}, provider={}", scriptId, provider);

        Script script = getScriptById(scriptId);
        script.setStatus("analyzing");
        scriptRepository.save(script);

        try {
            // LLM 서버에 분석 요청
            Map<String, Object> analysisRequest = Map.of(
                    "content", script.getContent(),
                    "formatHint", script.getFormatHint() != null ? script.getFormatHint() : "",
                    "provider", provider != null ? provider : "openai"
            );

            log.info("Calling LLM server for script analysis: url={}/gen/analyze-script", llmBaseUrl);

            RestClient restClient = RestClient.builder().build();
            Map<String, Object> analysisResult = restClient.post()
                    .uri(llmBaseUrl + "/gen/analyze-script")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(analysisRequest)
                    .retrieve()
                    .body(Map.class);

            // 분석 결과를 JSON으로 저장
            String resultJson = objectMapper.writeValueAsString(analysisResult);
            script.setAnalysisResult(resultJson);
            script.setProvider(provider);
            script.setStatus("analyzed");

            log.info("Script analysis completed: id={}, characters={}, dialogues={}, scenes={}, relationships={}",
                    scriptId,
                    analysisResult.get("characters") != null ? ((List<?>) analysisResult.get("characters")).size() : 0,
                    analysisResult.get("dialogues") != null ? ((List<?>) analysisResult.get("dialogues")).size() : 0,
                    analysisResult.get("scenes") != null ? ((List<?>) analysisResult.get("scenes")).size() : 0,
                    analysisResult.get("relationships") != null ? ((List<?>) analysisResult.get("relationships")).size() : 0
            );

            return scriptRepository.save(script);

        } catch (Exception e) {
            log.error("Failed to analyze script: id={}, error={}", scriptId, e.getMessage(), e);
            script.setStatus("failed");
            scriptRepository.save(script);
            throw new RuntimeException("Script analysis failed: " + e.getMessage(), e);
        }
    }

    /**
     * 분석 결과 조회 (JSON 파싱)
     */
    public Map<String, Object> getAnalysisResult(Long scriptId) {
        log.debug("Fetching analysis result for script: {}", scriptId);

        Script script = getScriptById(scriptId);

        if (script.getAnalysisResult() == null || script.getAnalysisResult().isEmpty()) {
            log.warn("No analysis result found for script: {}", scriptId);
            return Map.of(
                    "characters", List.of(),
                    "dialogues", List.of(),
                    "scenes", List.of(),
                    "relationships", List.of()
            );
        }

        try {
            Map<String, Object> result = objectMapper.readValue(script.getAnalysisResult(), Map.class);
            log.info("Analysis result retrieved: scriptId={}, hasData={}", scriptId, !result.isEmpty());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse analysis result: scriptId={}, error={}", scriptId, e.getMessage());
            throw new RuntimeException("Failed to parse analysis result: " + e.getMessage(), e);
        }
    }

    /**
     * 스크립트 삭제 (프로젝트별)
     */
    @Transactional
    public void deleteScript(Long id) {
        log.info("Deleting script: id={}", id);

        Script script = getScriptById(id); // 이미 프로젝트 확인 포함

        scriptRepository.delete(script);
        log.info("Script deleted successfully: id={}", id);
    }

    /**
     * 스크립트 제목으로 검색 (프로젝트별)
     */
    public List<Script> searchScripts(String keyword) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Searching scripts with keyword: {}, project: {}", keyword, currentProject.getId());
        List<Script> scripts = scriptRepository.findByProjectAndTitleContainingIgnoreCase(currentProject, keyword);
        log.info("Found {} scripts matching '{}'", scripts.size(), keyword);
        return scripts;
    }
}