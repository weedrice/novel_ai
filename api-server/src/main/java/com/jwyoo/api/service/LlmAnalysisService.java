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

        String prompt = String.format("""
            다음 텍스트를 간단하고 명확하게 요약해주세요.

            텍스트:
            %s

            요약 조건:
            1. 핵심 내용을 3-5문장으로 요약
            2. 주요 사건이나 갈등을 포함
            3. 감정적 톤이나 분위기 언급
            4. 간단명료하게 작성

            JSON 형식으로 응답:
            {
                "summary": "요약 내용",
                "keyPoints": ["핵심 포인트1", "핵심 포인트2", ...],
                "mood": "전반적인 분위기",
                "wordCount": 원문_단어수,
                "summaryRatio": "요약비율(퍼센트)"
            }
            """, text);

        try {
            Map<String, Object> response = callLlmServer(prompt, "gpt-4");
            return processLlmResponse(response);
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

        String prompt = String.format("""
            다음 텍스트에서 등장하는 캐릭터들을 분석해주세요.

            텍스트:
            %s

            분석 항목:
            1. 등장하는 캐릭터 이름
            2. 각 캐릭터의 성격 특징
            3. 말투나 특징적 표현
            4. 관계성 분석
            5. 등장 빈도

            JSON 형식으로 응답:
            {
                "characters": [
                    {
                        "name": "캐릭터 이름",
                        "personality": "성격 특징",
                        "speechStyle": "말투 특징",
                        "frequency": 등장빈도(1-10),
                        "role": "주인공/조연/기타"
                    }
                ],
                "relationships": [
                    {
                        "character1": "캐릭터1",
                        "character2": "캐릭터2",
                        "relationship": "관계 설명"
                    }
                ],
                "totalCharacters": 총_캐릭터수
            }
            """, text);

        try {
            Map<String, Object> response = callLlmServer(prompt, "gpt-4");
            return processLlmResponse(response);
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

        String prompt = String.format("""
            다음 텍스트에서 장면들을 추출하고 분석해주세요.

            텍스트:
            %s

            분석 항목:
            1. 주요 장면 구분
            2. 각 장면의 배경/위치
            3. 등장 인물
            4. 핵심 사건
            5. 감정적 톤

            JSON 형식으로 응답:
            {
                "scenes": [
                    {
                        "sceneNumber": 번호,
                        "location": "장소/배경",
                        "characters": ["등장인물1", "등장인물2"],
                        "summary": "장면 요약",
                        "mood": "분위기",
                        "keyEvent": "핵심 사건"
                    }
                ],
                "totalScenes": 총_장면수,
                "mainLocation": "주요 배경"
            }
            """, text);

        try {
            Map<String, Object> response = callLlmServer(prompt, "gpt-4");
            return processLlmResponse(response);
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

        String prompt = String.format("""
            다음 텍스트에서 대사들을 추출하고 분석해주세요.

            텍스트:
            %s

            분석 항목:
            1. 직접 대화 추출
            2. 화자 구분
            3. 대화의 톤/감정
            4. 중요한 대화 식별
            5. 대화 비율 분석

            JSON 형식으로 응답:
            {
                "dialogues": [
                    {
                        "speaker": "화자",
                        "text": "대사 내용",
                        "emotion": "감정/톤",
                        "importance": "중요도(1-10)"
                    }
                ],
                "dialogueRatio": "전체_텍스트_대비_대화_비율",
                "mainSpeakers": ["주요화자1", "주요화자2"],
                "totalDialogues": 총_대화수
            }
            """, text);

        try {
            Map<String, Object> response = callLlmServer(prompt, "gpt-4");
            return processLlmResponse(response);
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

        String prompt = String.format("""
            다음 텍스트의 맞춤법과 문법을 검사해주세요.

            텍스트:
            %s

            검사 항목:
            1. 맞춤법 오류
            2. 문법 오류
            3. 띄어쓰기 오류
            4. 올바른 표현 제안

            JSON 형식으로 응답:
            {
                "errors": [
                    {
                        "type": "오류타입(spelling/grammar/spacing)",
                        "original": "원래 텍스트",
                        "suggestion": "수정 제안",
                        "position": "위치",
                        "reason": "오류 이유"
                    }
                ],
                "totalErrors": 총_오류수,
                "accuracy": "정확도_퍼센트",
                "correctedText": "수정된_전체_텍스트"
            }
            """, text);

        try {
            Map<String, Object> response = callLlmServer(prompt, "gpt-4");
            return processLlmResponse(response);
        } catch (Exception e) {
            log.error("Failed to check spelling: {}", e.getMessage());
            return createFallbackSpellCheck(text);
        }
    }

    /**
     * LLM 서버 호출
     */
    private Map<String, Object> callLlmServer(String prompt, String provider) {
        String url = llmServerUrl + "/gen/text";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "prompt", prompt,
                "provider", provider,
                "max_tokens", 2000,
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call LLM server: {}", e.getMessage());
            throw new RuntimeException("LLM 서버 호출 실패: " + e.getMessage());
        }
    }

    /**
     * LLM 응답 처리
     */
    private Map<String, Object> processLlmResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("generated_text")) {
            throw new RuntimeException("Invalid LLM response");
        }

        String generatedText = (String) response.get("generated_text");

        // JSON 추출 시도
        try {
            return extractJsonFromText(generatedText);
        } catch (Exception e) {
            log.warn("Failed to parse JSON from LLM response, using fallback");
            return Map.of("result", generatedText, "rawResponse", true);
        }
    }

    /**
     * 텍스트에서 JSON 추출
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