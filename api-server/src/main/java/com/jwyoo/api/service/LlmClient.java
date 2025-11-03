package com.jwyoo.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.CharacterInfoDto;
import com.jwyoo.api.dto.LlmSuggestRequest;
import com.jwyoo.api.dto.SuggestRequest;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmClient {

    @Value("${LLM_BASE_URL:http://localhost:8000}")
    private String llmBaseUrl;

    private final CharacterRepository characterRepository;
    private final ObjectMapper objectMapper;

    private RestClient getRestClient() {
        return RestClient.builder()
                .messageConverters(converters -> {
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                })
                .build();
    }

    public Map<String, Object> suggest(SuggestRequest request) {
        log.info("LLM suggestion request started: speakerId={}, intent={}, honorific={}, provider={}",
                request.speakerId(), request.intent(), request.honorific(), request.provider());
        log.debug("LLM suggestion details: targetIds={}, maxLen={}, nCandidates={}",
                request.targetIds(), request.maxLen(), request.nCandidates());

        try {
            // 화자 캐릭터 정보 조회
            log.debug("Fetching speaker character: {}", request.speakerId());
            Character speaker = characterRepository.findByCharacterId(request.speakerId())
                    .orElseThrow(() -> {
                        log.error("Speaker character not found: {}", request.speakerId());
                        return new IllegalArgumentException("Speaker not found: " + request.speakerId());
                    });
            log.debug("Found speaker character: id={}, name={}", speaker.getId(), speaker.getName());

            // 대상 캐릭터 이름 목록 조회
            log.debug("Fetching target character names for: {}", request.targetIds());
            List<String> targetNames = request.targetIds().stream()
                    .map(targetId -> characterRepository.findByCharacterId(targetId)
                            .map(Character::getName)
                            .orElse(targetId))
                    .collect(Collectors.toList());
            log.debug("Target character names: {}", targetNames);

            // 캐릭터 정보 DTO 생성
            CharacterInfoDto characterInfo = new CharacterInfoDto(
                    speaker.getName(),
                    speaker.getDescription(),
                    speaker.getPersonality(),
                    speaker.getSpeakingStyle(),
                    speaker.getVocabulary(),
                    speaker.getToneKeywords(),
                    speaker.getExamples(),
                    speaker.getProhibitedWords(),
                    speaker.getSentencePatterns()
            );

            // LLM 서버로 전송할 요청 생성
            LlmSuggestRequest llmRequest = new LlmSuggestRequest(
                    request.speakerId(),
                    request.targetIds(),
                    request.intent(),
                    request.honorific(),
                    request.maxLen(),
                    request.nCandidates(),
                    characterInfo,
                    targetNames,
                    null,  // context (추후 확장 가능)
                    request.provider()
            );

            log.info("Calling LLM server: url={}/gen/suggest, speaker={}, intent={}, provider={}",
                    llmBaseUrl, request.speakerId(), request.intent(), request.provider());

            Map<String, Object> response = getRestClient().post()
                    .uri(llmBaseUrl + "/gen/suggest")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(llmRequest)
                    .retrieve()
                    .body(Map.class);

            log.info("LLM server response received: candidates={}",
                    response != null && response.containsKey("candidates") ?
                    ((List<?>) response.get("candidates")).size() : 0);
            log.debug("LLM response: {}", response);

            return response;

        } catch (Exception e) {
            log.error("Failed to call LLM server: {}", e.getMessage(), e);
            log.warn("Returning fallback dummy response for speaker: {}", request.speakerId());
            // LLM 서버가 없을 때 임시 더미 응답 반환
            return Map.of(
                "candidates", List.of(
                    Map.of("text", "안녕? 오랜만이야!", "score", 0.95),
                    Map.of("text", "어, 안녕! 잘 지냈어?", "score", 0.88),
                    Map.of("text", "야! 여기서 뭐해?", "score", 0.82)
                )
            );
        }
    }

    public Map<String, Object> generateScenario(Map<String, Object> payload) {
        log.info("Scenario generation request started");
        log.debug("Scenario payload: {}", payload);

        try {
            log.info("Calling LLM server for scenario: url={}/gen/scenario", llmBaseUrl);

            Map<String, Object> response = getRestClient().post()
                    .uri(llmBaseUrl + "/gen/scenario")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            log.info("Scenario generated successfully: dialogues={}",
                    response != null && response.containsKey("dialogues") ?
                    ((List<?>) response.get("dialogues")).size() : 0);
            log.debug("Scenario response: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Failed to call LLM server (scenario): {}", e.getMessage(), e);
            log.warn("Returning fallback scenario response");
            return Map.of(
                    "dialogues", List.of(
                            Map.of("speaker", "A", "characterId", "unknown", "text", "Scenario fallback line 1", "order", 1),
                            Map.of("speaker", "B", "characterId", "unknown", "text", "Scenario fallback line 2", "order", 2)
                    )
            );
        }
    }

    /**
     * Task 92: 스트리밍 방식으로 대사 제안 요청
     * LLM 서버의 SSE 스트림을 프록시하여 반환
     */
    public Flux<ServerSentEvent<Map<String, Object>>> suggestStream(SuggestRequest request) {
        log.info("LLM streaming suggestion request started: speakerId={}, intent={}, provider={}",
                request.speakerId(), request.intent(), request.provider());

        try {
            // 화자 캐릭터 정보 조회
            Character speaker = characterRepository.findByCharacterId(request.speakerId())
                    .orElseThrow(() -> new IllegalArgumentException("Speaker not found: " + request.speakerId()));

            // 대상 캐릭터 이름 목록 조회
            List<String> targetNames = request.targetIds().stream()
                    .map(targetId -> characterRepository.findByCharacterId(targetId)
                            .map(Character::getName)
                            .orElse(targetId))
                    .collect(Collectors.toList());

            // 캐릭터 정보 DTO 생성
            CharacterInfoDto characterInfo = new CharacterInfoDto(
                    speaker.getName(),
                    speaker.getDescription(),
                    speaker.getPersonality(),
                    speaker.getSpeakingStyle(),
                    speaker.getVocabulary(),
                    speaker.getToneKeywords(),
                    speaker.getExamples(),
                    speaker.getProhibitedWords(),
                    speaker.getSentencePatterns()
            );

            // LLM 서버로 전송할 요청 생성
            LlmSuggestRequest llmRequest = new LlmSuggestRequest(
                    request.speakerId(),
                    request.targetIds(),
                    request.intent(),
                    request.honorific(),
                    request.maxLen(),
                    request.nCandidates(),
                    characterInfo,
                    targetNames,
                    null,
                    request.provider()
            );

            log.info("Calling LLM server streaming endpoint: url={}/gen/suggest-stream", llmBaseUrl);

            // WebClient를 사용하여 SSE 스트림 수신
            WebClient webClient = WebClient.builder()
                    .baseUrl(llmBaseUrl)
                    .build();

            return webClient.post()
                    .uri("/gen/suggest-stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(llmRequest)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .map(line -> {
                        // SSE 형식 파싱 (data: {...})
                        if (line.startsWith("data: ")) {
                            try {
                                String jsonStr = line.substring(6);
                                @SuppressWarnings("unchecked")
                                Map<String, Object> eventData = objectMapper.readValue(jsonStr, Map.class);
                                return ServerSentEvent.<Map<String, Object>>builder()
                                        .data(eventData)
                                        .build();
                            } catch (Exception e) {
                                log.error("Failed to parse SSE event: {}", line, e);
                                return ServerSentEvent.<Map<String, Object>>builder()
                                        .data(Map.of("type", "error", "message", "Parse error"))
                                        .build();
                            }
                        }
                        return null;
                    })
                    .filter(event -> event != null);

        } catch (Exception e) {
            log.error("Failed to initialize streaming: {}", e.getMessage(), e);
            // 오류 발생 시 에러 이벤트 반환
            return Flux.just(
                    ServerSentEvent.<Map<String, Object>>builder()
                            .data(Map.of("type", "error", "message", e.getMessage()))
                            .build()
            );
        }
    }
}
