package com.jwyoo.api.controller;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.entity.ScenarioVersion;
import com.jwyoo.api.service.LlmClient;
import com.jwyoo.api.service.SceneService;
import com.jwyoo.api.service.ScenarioVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/scenes")
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;
    private final LlmClient llmClient;
    private final ScenarioVersionService scenarioVersionService;

    @GetMapping
    public ResponseEntity<List<Scene>> getAllScenes() {
        log.info("GET /scenes - Fetching all scenes");
        return ResponseEntity.ok(sceneService.getAllScenes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getScene(@PathVariable Long id) {
        log.info("GET /scenes/{} - Fetching scene", id);
        Scene scene = sceneService.getSceneById(id);
        List<Character> participants = sceneService.getParticipants(scene);
        Map<String, Object> response = new HashMap<>();
        response.put("scene", scene);
        response.put("participants", participants);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/episode/{episodeId}")
    public ResponseEntity<List<Scene>> getScenesByEpisode(@PathVariable Long episodeId) {
        log.info("GET /scenes/episode/{} - Fetching scenes by episode", episodeId);
        return ResponseEntity.ok(sceneService.getScenesByEpisodeId(episodeId));
    }

    @GetMapping("/{id}/dialogues")
    public ResponseEntity<List<Dialogue>> getDialogues(@PathVariable Long id) {
        log.info("GET /scenes/{}/dialogues - Fetching dialogues", id);
        return ResponseEntity.ok(sceneService.getDialogues(id));
    }

    @PostMapping("/{id}/generate-scenario")
    public ResponseEntity<Map<String, Object>> generateScenario(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "openai") String provider,
            @RequestParam(required = false, defaultValue = "5") int dialogueCount
    ) {
        log.info("POST /scenes/{}/generate-scenario - provider: {}", id, provider);
        Scene scene = sceneService.getSceneById(id);
        List<Character> participants = sceneService.getParticipants(scene);
        if (participants.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Scene has no participants."));
        }

        Map<String, Object> request = new HashMap<>();
        request.put("sceneDescription", scene.getDescription());
        request.put("location", scene.getLocation());
        request.put("mood", scene.getMood());
        request.put("participants", participants.stream().map(c -> Map.of(
                "characterId", c.getCharacterId(),
                "name", c.getName(),
                "personality", c.getPersonality() != null ? c.getPersonality() : "",
                "speakingStyle", c.getSpeakingStyle() != null ? c.getSpeakingStyle() : ""
        )).collect(Collectors.toList()));
        request.put("dialogueCount", dialogueCount);
        request.put("provider", provider);

        Map<String, Object> llmResponse = llmClient.generateScenario(request);
        return ResponseEntity.ok(llmResponse);
    }

    @PostMapping
    public ResponseEntity<Scene> createScene(@RequestBody Scene scene) {
        log.info("POST /scenes - Creating new scene");
        return ResponseEntity.ok(sceneService.createScene(scene));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scene> updateScene(@PathVariable Long id, @RequestBody Scene scene) {
        log.info("PUT /scenes/{} - Updating scene", id);
        return ResponseEntity.ok(sceneService.updateScene(id, scene));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        log.info("DELETE /scenes/{} - Deleting scene", id);
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sceneId}/scenarios")
    public ResponseEntity<ScenarioVersion> saveScenarioVersion(
            @PathVariable Long sceneId,
            @RequestBody Map<String, String> request
    ) {
        log.info("POST /scenes/{}/scenarios - Saving scenario version", sceneId);
        String title = request.getOrDefault("title", "버전 " + System.currentTimeMillis());
        String content = request.get("content");
        String createdBy = request.getOrDefault("createdBy", "anonymous");
        return ResponseEntity.ok(scenarioVersionService.saveVersion(sceneId, title, content, createdBy));
    }

    @GetMapping("/{sceneId}/scenarios")
    public ResponseEntity<List<ScenarioVersion>> getScenarioVersions(@PathVariable Long sceneId) {
        log.info("GET /scenes/{}/scenarios - Fetching scenario versions", sceneId);
        return ResponseEntity.ok(scenarioVersionService.getVersionsBySceneId(sceneId));
    }

    @GetMapping("/scenarios/{versionId}")
    public ResponseEntity<ScenarioVersion> getScenarioVersion(@PathVariable Long versionId) {
        log.info("GET /scenarios/{} - Fetching scenario version", versionId);
        return ResponseEntity.ok(scenarioVersionService.getVersionById(versionId));
    }

    @DeleteMapping("/scenarios/{versionId}")
    public ResponseEntity<Void> deleteScenarioVersion(@PathVariable Long versionId) {
        log.info("DELETE /scenarios/{} - Deleting scenario version", versionId);
        scenarioVersionService.deleteVersion(versionId);
        return ResponseEntity.noContent().build();
    }
}
