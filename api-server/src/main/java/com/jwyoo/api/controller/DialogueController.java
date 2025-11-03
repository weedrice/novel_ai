package com.jwyoo.api.controller;

import com.jwyoo.api.dto.SuggestRequest;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.DialogueRepository;
import com.jwyoo.api.repository.SceneRepository;
import com.jwyoo.api.service.LlmClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 대사 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/dialogue")
@RequiredArgsConstructor
public class DialogueController {

    private final LlmClient llmClient;
    private final DialogueRepository dialogueRepository;
    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;

    /**
     * LLM을 통한 대사 제안
     */
    @PostMapping("/suggest")
    public Map<String, Object> suggest(@RequestBody @Valid SuggestRequest request) {
        return llmClient.suggest(request);
    }

    /**
     * Task 92: 스트리밍 방식으로 LLM 대사 제안
     * Server-Sent Events를 통해 실시간으로 생성 중인 대사를 전송
     */
    @PostMapping(value = "/suggest-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> suggestStream(@RequestBody @Valid SuggestRequest request) {
        log.info("Streaming dialogue suggestion request: speakerId={}, intent={}",
                request.speakerId(), request.intent());
        return llmClient.suggestStream(request);
    }

    // ==================== 대사 CRUD API ====================

    /**
     * 대사 생성
     */
    @PostMapping
    public ResponseEntity<Dialogue> createDialogue(@RequestBody Map<String, Object> request) {
        log.info("POST /dialogue - Creating new dialogue");

        Long sceneId = Long.valueOf(request.get("sceneId").toString());
        Long characterId = Long.valueOf(request.get("characterId").toString());
        String text = request.get("text").toString();
        Integer dialogueOrder = Integer.valueOf(request.getOrDefault("dialogueOrder", 1).toString());
        String intent = (String) request.get("intent");
        String honorific = (String) request.get("honorific");
        String emotion = (String) request.get("emotion");

        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new ResourceNotFoundException("장면", sceneId));
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터", characterId));

        Dialogue dialogue = Dialogue.builder()
                .scene(scene)
                .character(character)
                .text(text)
                .dialogueOrder(dialogueOrder)
                .intent(intent)
                .honorific(honorific)
                .emotion(emotion)
                .build();

        Dialogue saved = dialogueRepository.save(dialogue);
        log.info("Created dialogue: {}", saved.getId());

        return ResponseEntity.ok(saved);
    }

    /**
     * 대사 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Dialogue> updateDialogue(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        log.info("PUT /dialogue/{} - Updating dialogue", id);

        Dialogue dialogue = dialogueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("대사", id));

        // 업데이트할 필드만 변경
        if (request.containsKey("text")) {
            dialogue.setText(request.get("text").toString());
        }
        if (request.containsKey("dialogueOrder")) {
            dialogue.setDialogueOrder(Integer.valueOf(request.get("dialogueOrder").toString()));
        }
        if (request.containsKey("intent")) {
            dialogue.setIntent((String) request.get("intent"));
        }
        if (request.containsKey("honorific")) {
            dialogue.setHonorific((String) request.get("honorific"));
        }
        if (request.containsKey("emotion")) {
            dialogue.setEmotion((String) request.get("emotion"));
        }
        if (request.containsKey("characterId")) {
            Long characterId = Long.valueOf(request.get("characterId").toString());
            Character character = characterRepository.findById(characterId)
                    .orElseThrow(() -> new ResourceNotFoundException("캐릭터", characterId));
            dialogue.setCharacter(character);
        }

        Dialogue updated = dialogueRepository.save(dialogue);
        log.info("Updated dialogue: {}", updated.getId());

        return ResponseEntity.ok(updated);
    }

    /**
     * 대사 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDialogue(@PathVariable Long id) {
        log.info("DELETE /dialogue/{} - Deleting dialogue", id);

        Dialogue dialogue = dialogueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("대사", id));

        dialogueRepository.delete(dialogue);
        log.info("Deleted dialogue: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 대사 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dialogue> getDialogue(@PathVariable Long id) {
        log.info("GET /dialogue/{} - Fetching dialogue", id);

        Dialogue dialogue = dialogueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("대사", id));

        return ResponseEntity.ok(dialogue);
    }

    /**
     * 장면의 대사 목록 조회 (순서대로)
     */
    @GetMapping("/scene/{sceneId}")
    public ResponseEntity<List<Dialogue>> getDialoguesByScene(@PathVariable Long sceneId) {
        log.info("GET /dialogue/scene/{} - Fetching dialogues by scene", sceneId);

        List<Dialogue> dialogues = dialogueRepository.findBySceneIdOrderByDialogueOrderAsc(sceneId);

        return ResponseEntity.ok(dialogues);
    }

    /**
     * 캐릭터의 대사 목록 조회 (최신순)
     */
    @GetMapping("/character/{characterId}")
    public ResponseEntity<List<Dialogue>> getDialoguesByCharacter(@PathVariable Long characterId) {
        log.info("GET /dialogue/character/{} - Fetching dialogues by character", characterId);

        List<Dialogue> dialogues = dialogueRepository.findByCharacterIdOrderByCreatedAtDesc(characterId);

        return ResponseEntity.ok(dialogues);
    }
}
