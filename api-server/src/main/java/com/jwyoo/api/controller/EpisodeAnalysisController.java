package com.jwyoo.api.controller;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.service.EpisodeService;
import com.jwyoo.api.service.LlmAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 에피소드 분석 관련 컨트롤러
 * AI를 활용한 다양한 텍스트 분석 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/episodes/{episodeId}/analysis")
@RequiredArgsConstructor
public class EpisodeAnalysisController {

    private final EpisodeService episodeService;
    private final LlmAnalysisService llmAnalysisService;

    /**
     * AI 요약 생성
     */
    @PostMapping("/summary")
    public ResponseEntity<?> generateSummary(@PathVariable Long episodeId) {
        log.info("POST /episodes/{}/analysis/summary - Generating AI summary", episodeId);

        try {
            Episode episode = episodeService.getEpisodeById(episodeId);

            if (episode.getScriptText() == null || episode.getScriptText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "에피소드에 내용이 없습니다."));
            }

            Map<String, Object> summary = llmAnalysisService.generateSummary(episode.getScriptText());

            return ResponseEntity.ok(Map.of(
                    "episodeId", episodeId,
                    "summary", summary,
                    "message", "AI 요약이 생성되었습니다."
            ));

        } catch (Exception e) {
            log.error("Failed to generate summary for episode {}: {}", episodeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "요약 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 캐릭터 분석
     */
    @PostMapping("/characters")
    public ResponseEntity<?> analyzeCharacters(@PathVariable Long episodeId) {
        log.info("POST /episodes/{}/analysis/characters - Analyzing characters", episodeId);

        try {
            Episode episode = episodeService.getEpisodeById(episodeId);

            if (episode.getScriptText() == null || episode.getScriptText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "에피소드에 내용이 없습니다."));
            }

            Map<String, Object> analysis = llmAnalysisService.analyzeCharacters(episode.getScriptText());

            return ResponseEntity.ok(Map.of(
                    "episodeId", episodeId,
                    "characters", analysis,
                    "message", "캐릭터 분석이 완료되었습니다."
            ));

        } catch (Exception e) {
            log.error("Failed to analyze characters for episode {}: {}", episodeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "캐릭터 분석에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 장면 추출
     */
    @PostMapping("/scenes")
    public ResponseEntity<?> extractScenes(@PathVariable Long episodeId) {
        log.info("POST /episodes/{}/analysis/scenes - Extracting scenes", episodeId);

        try {
            Episode episode = episodeService.getEpisodeById(episodeId);

            if (episode.getScriptText() == null || episode.getScriptText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "에피소드에 내용이 없습니다."));
            }

            Map<String, Object> scenes = llmAnalysisService.extractScenes(episode.getScriptText());

            return ResponseEntity.ok(Map.of(
                    "episodeId", episodeId,
                    "scenes", scenes,
                    "message", "장면 추출이 완료되었습니다."
            ));

        } catch (Exception e) {
            log.error("Failed to extract scenes for episode {}: {}", episodeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "장면 추출에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 대사 분석
     */
    @PostMapping("/dialogues")
    public ResponseEntity<?> analyzeDialogues(@PathVariable Long episodeId) {
        log.info("POST /episodes/{}/analysis/dialogues - Analyzing dialogues", episodeId);

        try {
            Episode episode = episodeService.getEpisodeById(episodeId);

            if (episode.getScriptText() == null || episode.getScriptText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "에피소드에 내용이 없습니다."));
            }

            Map<String, Object> analysis = llmAnalysisService.analyzeDialogues(episode.getScriptText());

            return ResponseEntity.ok(Map.of(
                    "episodeId", episodeId,
                    "dialogues", analysis,
                    "message", "대사 분석이 완료되었습니다."
            ));

        } catch (Exception e) {
            log.error("Failed to analyze dialogues for episode {}: {}", episodeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "대사 분석에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 맞춤법 검사
     */
    @PostMapping("/spell-check")
    public ResponseEntity<?> checkSpelling(@PathVariable Long episodeId) {
        log.info("POST /episodes/{}/analysis/spell-check - Checking spelling", episodeId);

        try {
            Episode episode = episodeService.getEpisodeById(episodeId);

            if (episode.getScriptText() == null || episode.getScriptText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "에피소드에 내용이 없습니다."));
            }

            Map<String, Object> spellCheck = llmAnalysisService.checkSpelling(episode.getScriptText());

            return ResponseEntity.ok(Map.of(
                    "episodeId", episodeId,
                    "spellCheck", spellCheck,
                    "message", "맞춤법 검사가 완료되었습니다."
            ));

        } catch (Exception e) {
            log.error("Failed to check spelling for episode {}: {}", episodeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "맞춤법 검사에 실패했습니다: " + e.getMessage()));
        }
    }
}