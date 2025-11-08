package com.jwyoo.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM을 활용한 텍스트 분석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${llm-server.url:http://localhost:5000}")
    private String llmServerUrl;

    /**
     * AI 요약 생성
     */
    public Map<String, Object> generateSummary(String text) {
        log.info("Generating AI summary for text (length: {})", text.length());

        String url = llmServerUrl + "/gen/episode/summary";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "scriptText", text,
                "scriptFormat", "novel",
                "provider", "openai"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to generate summary: {}", e.getMessage());
            return createFallbackSummary(text);
        }
    }

    /**
     * 캐릭터 분석
     */
    public Map<String, Object> analyzeCharacters(String text) {
        log.info("Analyzing characters for text (length: {})", text.length());

        String url = llmServerUrl + "/gen/episode/characters";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "scriptText", text,
                "scriptFormat", "novel",
                "provider", "openai"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to analyze characters: {}", e.getMessage());
            return createFallbackCharacterAnalysis(text);
        }
    }

    /**
     * 장면 추출
     */
    public Map<String, Object> extractScenes(String text) {
        log.info("Extracting scenes for text (length: {})", text.length());

        String url = llmServerUrl + "/gen/episode/scenes";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "scriptText", text,
                "scriptFormat", "novel",
                "provider", "openai"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to extract scenes: {}", e.getMessage());
            return createFallbackSceneAnalysis(text);
        }
    }

    /**
     * 대사 분석
     */
    public Map<String, Object> analyzeDialogues(String text) {
        log.info("Analyzing dialogues for text (length: {})", text.length());

        String url = llmServerUrl + "/gen/episode/dialogues";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "scriptText", text,
                "scriptFormat", "novel",
                "provider", "openai"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to analyze dialogues: {}", e.getMessage());
            return createFallbackDialogueAnalysis(text);
        }
    }

    /**
     * 맞춤법 검사
     */
    public Map<String, Object> checkSpelling(String text) {
        log.info("Checking spelling for text (length: {})", text.length());

        String url = llmServerUrl + "/gen/episode/spell-check";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "scriptText", text,
                "scriptFormat", "novel",
                "provider", "openai"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to check spelling: {}", e.getMessage());
            return createFallbackSpellCheck(text);
        }
    }

    /**
     * 텍스트에서 JSON 추출 (필요시 사용)
     */
    private Map<String, Object> extractJsonFromText(String text) throws JsonProcessingException {
        // JSON 코드 블록 패턴 매칭
        Pattern jsonPattern = Pattern.compile("```json\\s*\\n([\\s\\S]*?)\\n```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = jsonPattern.matcher(text);

        String jsonText;
        if (matcher.find()) {
            jsonText = matcher.group(1).trim();
        } else {
            // 중괄호로 둘러싸인 JSON 찾기
            Pattern bracesPattern = Pattern.compile("\\{[\\s\\S]*\\}");
            Matcher bracesMatcher = bracesPattern.matcher(text);
            if (bracesMatcher.find()) {
                jsonText = bracesMatcher.group().trim();
            } else {
                throw new RuntimeException("No JSON found in response");
            }
        }

        return objectMapper.readValue(jsonText, Map.class);
    }

    // Fallback 메서드들
    private Map<String, Object> createFallbackSummary(String text) {
        String[] sentences = text.split("[.!?]+");
        String summary = sentences.length > 3
            ? String.join(". ", Arrays.copyOf(sentences, 3)) + "."
            : text;

        return Map.of(
                "summary", summary,
                "keyPoints", List.of("자동 요약 생성 실패"),
                "mood", "중성",
                "wordCount", text.split("\\s+").length,
                "summaryRatio", "30%"
        );
    }

    private Map<String, Object> createFallbackCharacterAnalysis(String text) {
        return Map.of(
                "characters", List.of(),
                "relationships", List.of(),
                "totalCharacters", 0,
                "error", "캐릭터 분석을 위해 LLM 서버가 필요합니다."
        );
    }

    private Map<String, Object> createFallbackSceneAnalysis(String text) {
        return Map.of(
                "scenes", List.of(),
                "totalScenes", 0,
                "mainLocation", "알 수 없음",
                "error", "장면 추출을 위해 LLM 서버가 필요합니다."
        );
    }

    private Map<String, Object> createFallbackDialogueAnalysis(String text) {
        return Map.of(
                "dialogues", List.of(),
                "dialogueRatio", "0%",
                "mainSpeakers", List.of(),
                "totalDialogues", 0,
                "error", "대사 분석을 위해 LLM 서버가 필요합니다."
        );
    }

    private Map<String, Object> createFallbackSpellCheck(String text) {
        return Map.of(
                "errors", List.of(),
                "totalErrors", 0,
                "accuracy", "100%",
                "correctedText", text,
                "message", "맞춤법 검사를 위해 LLM 서버가 필요합니다."
        );
    }
}