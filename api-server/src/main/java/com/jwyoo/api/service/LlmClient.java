package com.jwyoo.api.service;

import com.jwyoo.api.dto.SuggestRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    @Value("${LLM_BASE_URL:http://localhost:8000}")
    private String llmBaseUrl;

    private final RestClient restClient = RestClient.create();

    public Map<String, Object> suggest(SuggestRequest request) {
        try {
            return restClient.post()
                    .uri(llmBaseUrl + "/gen/suggest")
                    .body(request)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
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
