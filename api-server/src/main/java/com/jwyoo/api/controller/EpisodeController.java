package com.jwyoo.api.controller;

import com.jwyoo.api.dto.PlotAnalysisDto;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.service.EpisodeService;
import com.jwyoo.api.service.PlotAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/episodes")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;
    private final PlotAnalysisService plotAnalysisService;

    @GetMapping
    public List<Map<String, Object>> getEpisodes() {
        return episodeService.getAllEpisodes().stream()
            .map(episode -> Map.of(
                "id", (Object) episode.getId(),
                "title", episode.getTitle(),
                "description", episode.getDescription() != null ? episode.getDescription() : "",
                "order", episode.getEpisodeOrder()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Task 99: 에피소드 플롯 분석 API
     * 장면별 갈등 강도, 캐릭터 등장 빈도, 스토리 아크 데이터 제공
     */
    @GetMapping("/{id}/plot-analysis")
    public ResponseEntity<PlotAnalysisDto> getPlotAnalysis(@PathVariable Long id) {
        log.info("GET /episodes/{}/plot-analysis - Fetching plot analysis", id);
        PlotAnalysisDto analysis = plotAnalysisService.analyzeEpisode(id);
        return ResponseEntity.ok(analysis);
    }

    /**
     * 에피소드 스크립트 업로드 및 분석
     */
    @PostMapping("/{id}/upload-and-analyze-script")
    public ResponseEntity<Map<String, Object>> uploadAndAnalyzeScript(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String scriptText = request.get("scriptText");
        String scriptFormat = request.getOrDefault("scriptFormat", "novel");
        String provider = request.getOrDefault("provider", "openai");

        log.info("POST /episodes/{}/upload-and-analyze-script - format={}, provider={}", id, scriptFormat, provider);

        Episode episode = episodeService.uploadAndAnalyzeScript(id, scriptText, scriptFormat, provider);

        // 분석 결과 조회
        Map<String, Object> analysisResult = episodeService.getScriptAnalysisResult(id);

        return ResponseEntity.ok(Map.of(
                "episode", episode,
                "analysis", analysisResult
        ));
    }

    /**
     * 에피소드 스크립트 분석 시작
     */
    @PostMapping("/{id}/analyze-script")
    public ResponseEntity<Episode> analyzeScript(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "openai") String provider
    ) {
        log.info("POST /episodes/{}/analyze-script - Starting analysis with provider: {}", id, provider);

        Episode episode = episodeService.analyzeEpisodeScript(id, provider);
        return ResponseEntity.ok(episode);
    }

    /**
     * 에피소드 스크립트 분석 결과 조회
     */
    @GetMapping("/{id}/script-analysis")
    public ResponseEntity<Map<String, Object>> getScriptAnalysisResult(@PathVariable Long id) {
        log.info("GET /episodes/{}/script-analysis - Fetching analysis result", id);

        Map<String, Object> result = episodeService.getScriptAnalysisResult(id);
        return ResponseEntity.ok(result);
    }
}
