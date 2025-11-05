package com.jwyoo.api.controller;

import com.jwyoo.api.entity.EpisodeRelationship;
import com.jwyoo.api.service.EpisodeRelationshipService;
import com.jwyoo.api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 에피소드별 캐릭터 관계 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/episode-relationships")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EpisodeRelationshipController {

    private final EpisodeRelationshipService episodeRelationshipService;
    private final ProjectService projectService;

    /**
     * 특정 에피소드의 관계 그래프 조회
     */
    @GetMapping("/episode/{episodeId}/graph")
    public ResponseEntity<Map<String, Object>> getEpisodeGraph(@PathVariable Long episodeId) {
        log.info("GET /episode-relationships/episode/{}/graph - Fetching relationship graph", episodeId);

        List<EpisodeRelationship> relationships = episodeRelationshipService.getRelationshipsByEpisodeAndProject(
            episodeId,
            projectService.getCurrentProject()
        );

        // 노드 (캐릭터) 추출
        var nodes = relationships.stream()
            .flatMap(r -> java.util.stream.Stream.of(
                Map.of("id", r.getFromCharacter().getId(), "label", r.getFromCharacter().getName()),
                Map.of("id", r.getToCharacter().getId(), "label", r.getToCharacter().getName())
            ))
            .distinct()
            .collect(Collectors.toList());

        // 엣지 (관계) 변환
        var edges = relationships.stream()
            .map(r -> Map.of(
                "id", r.getId(),
                "source", r.getFromCharacter().getId(),
                "target", r.getToCharacter().getId(),
                "label", r.getRelationType(),
                "closeness", r.getCloseness() != null ? r.getCloseness() : 0.0
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "nodes", nodes,
            "edges", edges
        ));
    }

    /**
     * 특정 에피소드의 모든 관계 조회
     */
    @GetMapping("/episode/{episodeId}")
    public ResponseEntity<List<EpisodeRelationship>> getRelationshipsByEpisode(@PathVariable Long episodeId) {
        log.info("GET /episode-relationships/episode/{} - Fetching relationships", episodeId);

        List<EpisodeRelationship> relationships = episodeRelationshipService.getRelationshipsByEpisodeAndProject(
            episodeId,
            projectService.getCurrentProject()
        );

        return ResponseEntity.ok(relationships);
    }

    /**
     * 두 캐릭터 간의 관계 변화 히스토리 조회
     */
    @GetMapping("/history")
    public ResponseEntity<List<EpisodeRelationship>> getRelationshipHistory(
        @RequestParam Long char1Id,
        @RequestParam Long char2Id
    ) {
        log.info("GET /episode-relationships/history?char1Id={}&char2Id={}", char1Id, char2Id);

        List<EpisodeRelationship> history = episodeRelationshipService.getRelationshipHistory(char1Id, char2Id);

        return ResponseEntity.ok(history);
    }

    /**
     * 에피소드 관계 생성
     */
    @PostMapping
    public ResponseEntity<EpisodeRelationship> createRelationship(@RequestBody EpisodeRelationship relationship) {
        log.info("POST /episode-relationships - Creating relationship");

        EpisodeRelationship created = episodeRelationshipService.createRelationship(relationship);

        return ResponseEntity.ok(created);
    }

    /**
     * 에피소드 관계 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<EpisodeRelationship> updateRelationship(
        @PathVariable Long id,
        @RequestBody EpisodeRelationship relationship
    ) {
        log.info("PUT /episode-relationships/{} - Updating relationship", id);

        EpisodeRelationship updated = episodeRelationshipService.updateRelationship(id, relationship);

        return ResponseEntity.ok(updated);
    }

    /**
     * 에피소드 관계 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelationship(@PathVariable Long id) {
        log.info("DELETE /episode-relationships/{} - Deleting relationship", id);

        episodeRelationshipService.deleteRelationship(id);

        return ResponseEntity.noContent().build();
    }
}
