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

    /**
     * Degree Centrality 계산
     * GET /graph/centrality/degree?limit=10
     *
     * @param limit 결과 개수 (기본값: 10)
     */
    @GetMapping("/centrality/degree")
    public ResponseEntity<Map<String, Object>> getDegreeCentrality(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /graph/centrality/degree?limit={} - Calculating Degree Centrality", limit);

        List<Object> results = graphQueryService.calculateDegreeCentrality(limit);

        Map<String, Object> response = Map.of(
            "metric", "degreeCentrality",
            "results", results,
            "count", results.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Betweenness Centrality 계산
     * GET /graph/centrality/betweenness?limit=10
     *
     * @param limit 결과 개수 (기본값: 10)
     */
    @GetMapping("/centrality/betweenness")
    public ResponseEntity<Map<String, Object>> getBetweennessCentrality(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /graph/centrality/betweenness?limit={} - Calculating Betweenness Centrality", limit);

        List<Object> results = graphQueryService.calculateBetweennessCentrality(limit);

        Map<String, Object> response = Map.of(
            "metric", "betweennessCentrality",
            "results", results,
            "count", results.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Closeness Centrality 계산
     * GET /graph/centrality/closeness?limit=10
     *
     * @param limit 결과 개수 (기본값: 10)
     */
    @GetMapping("/centrality/closeness")
    public ResponseEntity<Map<String, Object>> getClosenessCentrality(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /graph/centrality/closeness?limit={} - Calculating Closeness Centrality", limit);

        List<Object> results = graphQueryService.calculateClosenessCentrality(limit);

        Map<String, Object> response = Map.of(
            "metric", "closenessCentrality",
            "results", results,
            "count", results.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Weighted Degree 계산
     * GET /graph/centrality/weighted?limit=10
     *
     * @param limit 결과 개수 (기본값: 10)
     */
    @GetMapping("/centrality/weighted")
    public ResponseEntity<Map<String, Object>> getWeightedDegree(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /graph/centrality/weighted?limit={} - Calculating Weighted Degree", limit);

        List<Object> results = graphQueryService.calculateWeightedDegree(limit);

        Map<String, Object> response = Map.of(
            "metric", "weightedDegree",
            "results", results,
            "count", results.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 모든 Centrality 지표 한번에 계산
     * GET /graph/centrality/all?limit=10
     *
     * @param limit 각 지표당 결과 개수 (기본값: 10)
     */
    @GetMapping("/centrality/all")
    public ResponseEntity<Map<String, Object>> getAllCentralities(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /graph/centrality/all?limit={} - Calculating all centrality metrics", limit);

        Map<String, Object> allMetrics = graphQueryService.calculateAllCentralities(limit);

        return ResponseEntity.ok(allMetrics);
    }

    /**
     * 에피소드 범위별 관계 변화 조회
     * GET /graph/timeline/range?start=1&end=10
     *
     * @param start 시작 에피소드 ID
     * @param end 종료 에피소드 ID
     */
    @GetMapping("/timeline/range")
    public ResponseEntity<Map<String, Object>> getRelationshipsByEpisodeRange(
            @RequestParam Long start,
            @RequestParam Long end
    ) {
        log.info("GET /graph/timeline/range?start={}&end={} - Fetching relationships by episode range", start, end);

        List<Object> relationships = graphQueryService.findRelationshipsByEpisodeRange(start, end);

        Map<String, Object> response = Map.of(
            "startEpisodeId", start,
            "endEpisodeId", end,
            "relationships", relationships,
            "count", relationships.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 캐릭터의 관계 진화 추적
     * GET /graph/timeline/character/{characterId}
     *
     * @param characterId 캐릭터 ID
     */
    @GetMapping("/timeline/character/{characterId}")
    public ResponseEntity<Map<String, Object>> getCharacterRelationshipEvolution(
            @PathVariable String characterId
    ) {
        log.info("GET /graph/timeline/character/{} - Fetching character relationship evolution", characterId);

        List<Object> evolution = graphQueryService.findCharacterRelationshipEvolution(characterId);

        Map<String, Object> response = Map.of(
            "characterId", characterId,
            "evolution", evolution,
            "count", evolution.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 두 캐릭터 간 관계 타임라인
     * GET /graph/timeline/relationship?char1=alice&char2=bob
     *
     * @param char1 캐릭터 1 ID
     * @param char2 캐릭터 2 ID
     */
    @GetMapping("/timeline/relationship")
    public ResponseEntity<Map<String, Object>> getRelationshipTimeline(
            @RequestParam String char1,
            @RequestParam String char2
    ) {
        log.info("GET /graph/timeline/relationship?char1={}&char2={} - Fetching relationship timeline", char1, char2);

        List<Object> timeline = graphQueryService.findRelationshipTimeline(char1, char2);

        Map<String, Object> response = Map.of(
            "character1", char1,
            "character2", char2,
            "timeline", timeline,
            "count", timeline.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 에피소드별 네트워크 밀도 계산
     * GET /graph/timeline/density/{episodeId}
     *
     * @param episodeId 에피소드 ID
     */
    @GetMapping("/timeline/density/{episodeId}")
    public ResponseEntity<Map<String, Object>> getNetworkDensityByEpisode(
            @PathVariable Long episodeId
    ) {
        log.info("GET /graph/timeline/density/{} - Calculating network density", episodeId);

        Object density = graphQueryService.calculateNetworkDensityByEpisode(episodeId);

        Map<String, Object> response = Map.of(
            "episodeId", episodeId,
            "density", density
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 새로운 관계 추가 현황 조회
     * GET /graph/timeline/new-relationships
     */
    @GetMapping("/timeline/new-relationships")
    public ResponseEntity<Map<String, Object>> getNewRelationshipsByEpisode() {
        log.info("GET /graph/timeline/new-relationships - Fetching new relationships by episode");

        List<Object> newRelationships = graphQueryService.findNewRelationshipsByEpisode();

        Map<String, Object> response = Map.of(
            "newRelationships", newRelationships,
            "count", newRelationships.size()
        );

        return ResponseEntity.ok(response);
    }
}
