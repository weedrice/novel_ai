package com.jwyoo.api.controller;

import com.jwyoo.api.dto.AIAnalysisRequest;
import com.jwyoo.api.dto.AIAnalysisResponse;
import com.jwyoo.api.entity.AIAnalysis;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.service.AIAnalysisService;
import com.jwyoo.api.service.EpisodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 분석 결과 관리 API 컨트롤러
 *
 * API 엔드포인트:
 * - POST   /episodes/{episodeId}/analyses        : AI 분석 실행
 * - GET    /episodes/{episodeId}/analyses        : 분석 목록 조회
 * - GET    /analyses/{id}                        : 특정 분석 조회
 * - DELETE /analyses/{id}                        : 분석 삭제
 * - GET    /analyses/compare                     : 여러 모델 결과 비교
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;
    private final EpisodeService episodeService;

    /**
     * AI 분석 실행 (새로운 분석 결과 생성)
     * POST /episodes/{episodeId}/analyses
     */
    @PostMapping("/episodes/{episodeId}/analyses")
    public ResponseEntity<AIAnalysisResponse> createAnalysis(
            @PathVariable Long episodeId,
            @Valid @RequestBody AIAnalysisRequest request
    ) {
        log.info("POST /episodes/{}/analyses - Creating new analysis: type={}, model={}",
            episodeId, request.getAnalysisType(), request.getModelName());

        Episode episode = episodeService.getEpisodeById(episodeId);

        AIAnalysis analysis = AIAnalysis.builder()
            .episode(episode)
            .analysisType(request.getAnalysisType())
            .modelName(request.getModelName())
            .result(request.getResult())
            .confidence(request.getConfidence())
            .executionTimeMs(request.getExecutionTimeMs())
            .status(request.getStatus() != null ? request.getStatus() : "completed")
            .errorMessage(request.getErrorMessage())
            .build();

        AIAnalysis saved = aiAnalysisService.createAnalysis(analysis);
        AIAnalysisResponse response = AIAnalysisResponse.from(saved);

        log.info("Analysis created successfully: id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 에피소드의 분석 목록 조회
     * GET /episodes/{episodeId}/analyses
     *
     * 쿼리 파라미터:
     * - type: 분석 유형 필터 (선택)
     * - model: 모델 필터 (선택)
     */
    @GetMapping("/episodes/{episodeId}/analyses")
    public ResponseEntity<List<AIAnalysisResponse>> getAnalysesByEpisode(
            @PathVariable Long episodeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String model
    ) {
        log.info("GET /episodes/{}/analyses - type={}, model={}", episodeId, type, model);

        List<AIAnalysis> analyses;

        if (type != null && model != null) {
            analyses = aiAnalysisService.getAnalysesByEpisodeIdAndTypeAndModel(episodeId, type, model);
        } else if (type != null) {
            analyses = aiAnalysisService.getAnalysesByEpisodeIdAndType(episodeId, type);
        } else {
            analyses = aiAnalysisService.getAnalysesByEpisodeId(episodeId);
        }

        List<AIAnalysisResponse> responses = analyses.stream()
            .map(AIAnalysisResponse::from)
            .collect(Collectors.toList());

        log.info("Found {} analyses for episode: {}", responses.size(), episodeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 분석 결과 조회
     * GET /analyses/{id}
     */
    @GetMapping("/analyses/{id}")
    public ResponseEntity<AIAnalysisResponse> getAnalysisById(@PathVariable Long id) {
        log.info("GET /analyses/{} - Fetching analysis", id);

        AIAnalysis analysis = aiAnalysisService.getAnalysisById(id);
        AIAnalysisResponse response = AIAnalysisResponse.from(analysis);

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 결과 삭제
     * DELETE /analyses/{id}
     */
    @DeleteMapping("/analyses/{id}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable Long id) {
        log.info("DELETE /analyses/{} - Deleting analysis", id);

        aiAnalysisService.deleteAnalysis(id);

        log.info("Analysis deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 여러 AI 모델의 분석 결과 비교
     * GET /analyses/compare?episodeId={episodeId}&type={type}
     *
     * 같은 에피소드, 같은 분석 유형에 대해 여러 모델의 최신 결과를 반환
     */
    @GetMapping("/analyses/compare")
    public ResponseEntity<Map<String, AIAnalysisResponse>> compareModelAnalyses(
            @RequestParam Long episodeId,
            @RequestParam String type
    ) {
        log.info("GET /analyses/compare - episodeId={}, type={}", episodeId, type);

        Map<String, AIAnalysis> latestByModel = aiAnalysisService.compareModelAnalyses(episodeId, type);

        Map<String, AIAnalysisResponse> response = latestByModel.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> AIAnalysisResponse.from(entry.getValue())
            ));

        log.info("Comparison found {} different models", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 에피소드의 최신 분석 결과 조회 (분석 유형별)
     * GET /episodes/{episodeId}/analyses/latest?type={type}
     */
    @GetMapping("/episodes/{episodeId}/analyses/latest")
    public ResponseEntity<AIAnalysisResponse> getLatestAnalysis(
            @PathVariable Long episodeId,
            @RequestParam String type
    ) {
        log.info("GET /episodes/{}/analyses/latest - type={}", episodeId, type);

        AIAnalysis latest = aiAnalysisService.getLatestAnalysisByEpisodeIdAndType(episodeId, type);
        AIAnalysisResponse response = AIAnalysisResponse.from(latest);

        return ResponseEntity.ok(response);
    }

    /**
     * 에피소드의 분석 개수 조회
     * GET /episodes/{episodeId}/analyses/count
     */
    @GetMapping("/episodes/{episodeId}/analyses/count")
    public ResponseEntity<Map<String, Long>> countAnalyses(@PathVariable Long episodeId) {
        log.info("GET /episodes/{}/analyses/count", episodeId);

        long count = aiAnalysisService.countAnalysesByEpisodeId(episodeId);

        return ResponseEntity.ok(Map.of("count", count));
    }
}
