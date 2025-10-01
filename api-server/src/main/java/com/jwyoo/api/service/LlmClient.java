package com.jwyoo.api.service;

import com.jwyoo.api.dto.SuggestRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class LlmClient {

    @Value("${LLM_BASE_URL:http://localhost:8000}")
    private String llmBaseUrl;

    private final RestClient restClient = RestClient.create();

    public Map<String, Object> suggest(SuggestRequest request) {
        return restClient.post()
                .uri(llmBaseUrl + "/gen/suggest")
                .body(request)
                .retrieve()
                .body(Map.class);
    }
}
