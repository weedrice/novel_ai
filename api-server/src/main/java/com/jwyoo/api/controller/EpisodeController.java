package com.jwyoo.api.controller;

import com.jwyoo.api.dto.PlotAnalysisDto;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.service.EpisodeService;
import com.jwyoo.api.service.PlotAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
