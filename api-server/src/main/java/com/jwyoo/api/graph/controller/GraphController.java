package com.jwyoo.api.graph.controller;

import com.jwyoo.api.graph.node.CharacterNode;
import com.jwyoo.api.graph.service.GraphQueryService;
import com.jwyoo.api.graph.service.GraphSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Neo4j GraphDB 기반 복잡한 관계 쿼리 API
 *
 * API 엔드포인트:
 * - GET  /graph/characters                              : 모든 캐릭터 조회
 * - GET  /graph/characters/{characterId}                : 캐릭터 조회
 * - GET  /graph/characters/{characterId}/friends        : N단계 친구 찾기
 * - GET  /graph/characters/{characterId}/relations      : 특정 관계 유형 조회
 * - GET  /graph/path                                    : 최단 경로 찾기
 * - GET  /graph/relationships                           : 모든 관계 조회
 * - GET  /graph/relationships/episode/{episodeId}       : 에피소드별 관계 조회
 * - GET  /graph/central-characters                      : 중심 인물 찾기
 * - POST /graph/sync/all                                : 전체 데이터 동기화
 * - POST /graph/sync/project/{projectId}                : 프로젝트 데이터 동기화
 */
@Slf4j
@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphQueryService graphQueryService;
    private final GraphSyncService graphSyncService;

    /**
     * 모든 캐릭터 조회
     * GET /graph/characters
     */
    @GetMapping("/characters")
    public ResponseEntity<List<CharacterNode>> getAllCharacters() {
        log.info("GET /graph/characters - Fetching all characters from Neo4j");

        List<CharacterNode> characters = graphQueryService.findAllCharacters();

        return ResponseEntity.ok(characters);
    }

    /**
     * 캐릭터 ID로 조회
     * GET /graph/characters/{characterId}
     */
    @GetMapping("/characters/{characterId}")
    public ResponseEntity<CharacterNode> getCharacter(@PathVariable String characterId) {
        log.info("GET /graph/characters/{} - Fetching character from Neo4j", characterId);

        CharacterNode character = graphQueryService.findByCharacterId(characterId);

        return ResponseEntity.ok(character);
    }

    /**
     * N단계 친구 찾기 (친구의 친구)
     * GET /graph/characters/{characterId}/friends?depth=2
     *
     * @param characterId 시작 캐릭터 ID
     * @param depth 탐색 깊이 (1-5, 기본값: 2)
     */
    @GetMapping("/characters/{characterId}/friends")
    public ResponseEntity<Map<String, Object>> getNDegreeFriends(
            @PathVariable String characterId,
            @RequestParam(defaultValue = "2") int depth
    ) {
        log.info("GET /graph/characters/{}/friends?depth={} - Finding N-degree friends", characterId, depth);

        List<CharacterNode> friends = graphQueryService.findNDegreeFriends(characterId, depth);

        Map<String, Object> response = Map.of(
            "characterId", characterId,
            "depth", depth,
            "friends", friends,
            "count", friends.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 관계 유형으로 연결된 캐릭터 찾기
     * GET /graph/characters/{characterId}/relations?type=friend
     *
     * @param characterId 시작 캐릭터 ID
     * @param type 관계 유형 (friend, rival, family, lover, enemy)
     */
    @GetMapping("/characters/{characterId}/relations")
    public ResponseEntity<Map<String, Object>> getCharactersByRelationType(
            @PathVariable String characterId,
            @RequestParam String type
    ) {
        log.info("GET /graph/characters/{}/relations?type={} - Finding characters by relation type", characterId, type);

        List<CharacterNode> characters = graphQueryService.findCharactersByRelationType(characterId, type);

        Map<String, Object> response = Map.of(
            "characterId", characterId,
            "relationType", type,
            "characters", characters,
            "count", characters.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 두 캐릭터 사이의 최단 경로 찾기
     * GET /graph/path?from=char1&to=char2
     *
     * @param from 시작 캐릭터 ID
     * @param to 도착 캐릭터 ID
     */
    @GetMapping("/path")
    public ResponseEntity<Map<String, Object>> getShortestPath(
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("GET /graph/path?from={}&to={} - Finding shortest path", from, to);

        Map<String, Object> path = graphQueryService.findShortestPath(from, to);

        return ResponseEntity.ok(path);
    }

    /**
     * 프로젝트의 모든 관계 조회 (그래프 시각화용)
     * GET /graph/relationships
     */
    @GetMapping("/relationships")
    public ResponseEntity<Map<String, Object>> getAllRelationships() {
        log.info("GET /graph/relationships - Fetching all relationships from Neo4j");

        List<Object> relationships = graphQueryService.findAllRelationships();

        Map<String, Object> response = Map.of(
            "relationships", relationships,
            "count", relationships.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 에피소드의 관계 조회
     * GET /graph/relationships/episode/{episodeId}
     */
    @GetMapping("/relationships/episode/{episodeId}")
    public ResponseEntity<Map<String, Object>> getRelationshipsByEpisode(@PathVariable Long episodeId) {
        log.info("GET /graph/relationships/episode/{} - Fetching episode relationships", episodeId);

        List<Object> relationships = graphQueryService.findRelationshipsByEpisode(episodeId);

        Map<String, Object> response = Map.of(
            "episodeId", episodeId,
            "relationships", relationships,
            "count", relationships.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 가장 많은 관계를 가진 캐릭터 찾기 (중심 인물)
     * GET /graph/central-characters?limit=10
     *
     * @param limit 결과 개수 제한 (1-50, 기본값: 10)
     */
    @GetMapping("/central-characters")
    public ResponseEntity<Map<String, Object>> getCentralCharacters(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /graph/central-characters?limit={} - Finding central characters", limit);

        List<Map<String, Object>> characters = graphQueryService.findMostConnectedCharacters(limit);

        Map<String, Object> response = Map.of(
            "centralCharacters", characters,
            "count", characters.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 전체 데이터 동기화 (RDB → Neo4j)
     * POST /graph/sync/all
     */
    @PostMapping("/sync/all")
    public ResponseEntity<Map<String, String>> syncAllData() {
        log.info("POST /graph/sync/all - Starting bulk migration");

        try {
            graphSyncService.migrateAllData();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All data synced to Neo4j successfully"
            ));
        } catch (Exception e) {
            log.error("Bulk migration failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to sync data: " + e.getMessage()
            ));
        }
    }

    /**
     * 프로젝트 데이터 동기화 (RDB → Neo4j)
     * POST /graph/sync/project/{projectId}
     */
    @PostMapping("/sync/project/{projectId}")
    public ResponseEntity<Map<String, String>> syncProjectData(@PathVariable Long projectId) {
        log.info("POST /graph/sync/project/{} - Starting project migration", projectId);

        try {
            graphSyncService.migrateProjectData(projectId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Project data synced to Neo4j successfully",
                "projectId", projectId.toString()
            ));
        } catch (Exception e) {
            log.error("Project migration failed: projectId={}, error={}", projectId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to sync project data: " + e.getMessage()
            ));
        }
    }
}
