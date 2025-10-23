package com.jwyoo.api.controller;

import com.jwyoo.api.entity.Script;
import com.jwyoo.api.service.ScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 스크립트 분석 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/scripts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScriptController {

    private final ScriptService scriptService;

    /**
     * 모든 스크립트 조회
     */
    @GetMapping
    public ResponseEntity<List<Script>> getAllScripts() {
        log.info("GET /scripts - Fetching all scripts");
        List<Script> scripts = scriptService.getAllScripts();
        return ResponseEntity.ok(scripts);
    }

    /**
     * ID로 스크립트 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptById(@PathVariable Long id) {
        log.info("GET /scripts/{} - Fetching script", id);
        Script script = scriptService.getScriptById(id);
        return ResponseEntity.ok(script);
    }

    /**
     * 상태별 스크립트 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Script>> getScriptsByStatus(@PathVariable String status) {
        log.info("GET /scripts/status/{} - Fetching scripts by status", status);
        List<Script> scripts = scriptService.getScriptsByStatus(status);
        return ResponseEntity.ok(scripts);
    }

    /**
     * 스크립트 업로드
     */
    @PostMapping
    public ResponseEntity<Script> uploadScript(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        String formatHint = request.getOrDefault("formatHint", null);

        log.info("POST /scripts - Uploading script: title={}, length={}", title, content.length());

        Script script = scriptService.uploadScript(title, content, formatHint);
        return ResponseEntity.status(HttpStatus.CREATED).body(script);
    }

    /**
     * 스크립트 분석 시작
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<Script> analyzeScript(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "openai") String provider
    ) {
        log.info("POST /scripts/{}/analyze - Starting analysis with provider: {}", id, provider);

        Script script = scriptService.analyzeScript(id, provider);
        return ResponseEntity.ok(script);
    }

    /**
     * 분석 결과 조회
     */
    @GetMapping("/{id}/analysis")
    public ResponseEntity<Map<String, Object>> getAnalysisResult(@PathVariable Long id) {
        log.info("GET /scripts/{}/analysis - Fetching analysis result", id);

        Map<String, Object> result = scriptService.getAnalysisResult(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 스크립트 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        log.info("DELETE /scripts/{} - Deleting script", id);
        scriptService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 스크립트 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<Script>> searchScripts(@RequestParam String keyword) {
        log.info("GET /scripts/search?keyword={}", keyword);
        List<Script> scripts = scriptService.searchScripts(keyword);
        return ResponseEntity.ok(scripts);
    }

    /**
     * 업로드 및 즉시 분석 (편의 메서드)
     */
    @PostMapping("/upload-and-analyze")
    public ResponseEntity<Map<String, Object>> uploadAndAnalyze(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        String formatHint = request.getOrDefault("formatHint", null);
        String provider = request.getOrDefault("provider", "openai");

        log.info("POST /scripts/upload-and-analyze - title={}, provider={}", title, provider);

        // 1. 업로드
        Script script = scriptService.uploadScript(title, content, formatHint);

        // 2. 즉시 분석
        script = scriptService.analyzeScript(script.getId(), provider);

        // 3. 분석 결과 반환
        Map<String, Object> analysisResult = scriptService.getAnalysisResult(script.getId());

        return ResponseEntity.ok(Map.of(
                "script", script,
                "analysis", analysisResult
        ));
    }
}