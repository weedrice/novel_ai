package com.jwyoo.api.controller;

import com.jwyoo.api.dto.SuggestRequest;
import com.jwyoo.api.service.LlmClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dialogue")
@RequiredArgsConstructor
public class DialogueController {

    private final LlmClient llmClient;

    @PostMapping("/suggest")
    public Map<String, Object> suggest(@RequestBody @Valid SuggestRequest request) {
        return llmClient.suggest(request);
    }
}
