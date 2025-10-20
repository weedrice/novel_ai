package com.jwyoo.api.service;

import com.jwyoo.api.dto.CharacterInfoDto;
import com.jwyoo.api.dto.LlmSuggestRequest;
import com.jwyoo.api.dto.SuggestRequest;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    private final RestClient restClient = RestClient.create();

    public Map<String, Object> suggest(SuggestRequest request) {
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
                    null,  // context (추후 확장 가능)
                    request.provider()
            );

            log.info("Sending request to LLM server: speaker={}, intent={}, provider={}",
                    request.speakerId(), request.intent(), request.provider());

            return restClient.post()
                    .uri(llmBaseUrl + "/gen/suggest")
                    .body(llmRequest)
                    .retrieve()
                    .body(Map.class);

        } catch (Exception e) {
            log.error("Failed to call LLM server: {}", e.getMessage(), e);
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
}
