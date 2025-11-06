package com.jwyoo.api.graph.service;

import com.jwyoo.api.graph.node.CharacterNode;
import com.jwyoo.api.graph.repository.CharacterNodeRepository;
import com.jwyoo.api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Neo4j 기반 복잡한 관계 쿼리 서비스
 * - N단계 친구 찾기 (친구의 친구)
 * - 두 캐릭터 사이의 최단 경로
 * - 가장 많은 관계를 가진 캐릭터 (중심 인물)
 * - 특정 관계 유형으로 연결된 캐릭터
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraphQueryService {

    private final CharacterNodeRepository characterNodeRepository;
    private final ProjectService projectService;

    /**
     * 특정 캐릭터의 N단계 친구 찾기
     * @param characterId 시작 캐릭터 ID
     * @param depth 탐색 깊이 (1: 직접 친구, 2: 친구의 친구, etc.)
     * @return 찾은 캐릭터 목록
     */
    public List<CharacterNode> findNDegreeFriends(String characterId, int depth) {
        log.info("Finding {}-degree friends for character: {}", depth, characterId);

        if (depth < 1 || depth > 5) {
            throw new IllegalArgumentException("Depth must be between 1 and 5");
        }

        List<CharacterNode> friends = characterNodeRepository.findNDegreeFriends(characterId, depth);
        log.info("Found {} friends within {} degrees", friends.size(), depth);
        return friends;
    }

    /**
     * 두 캐릭터 사이의 최단 경로 찾기
     * @param fromCharacterId 시작 캐릭터 ID
     * @param toCharacterId 도착 캐릭터 ID
     * @return 경로 정보 (캐릭터 목록, 관계 목록, 거리)
     */
    public Map<String, Object> findShortestPath(String fromCharacterId, String toCharacterId) {
        log.info("Finding shortest path from {} to {}", fromCharacterId, toCharacterId);

        if (fromCharacterId.equals(toCharacterId)) {
            throw new IllegalArgumentException("Source and target characters cannot be the same");
        }

        Object result = characterNodeRepository.findShortestPath(fromCharacterId, toCharacterId);

        if (result == null) {
            log.warn("No path found between {} and {}", fromCharacterId, toCharacterId);
            return Map.of(
                "found", false,
                "message", "No path found between characters"
            );
        }

        log.info("Shortest path found");
        return Map.of(
            "found", true,
            "path", result
        );
    }

    /**
     * 특정 에피소드에서의 캐릭터 관계 조회
     * @param episodeId 에피소드 ID
     * @return 관계 목록
     */
    public List<Object> findRelationshipsByEpisode(Long episodeId) {
        log.info("Finding relationships for episode: {}", episodeId);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> relationships = characterNodeRepository.findRelationshipsByEpisodeId(episodeId, projectId);

        log.info("Found {} relationships for episode {}", relationships.size(), episodeId);
        return relationships;
    }

    /**
     * 프로젝트의 모든 관계 조회 (그래프 시각화용)
     * @return 관계 목록
     */
    public List<Object> findAllRelationships() {
        log.info("Finding all relationships for current project");

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> relationships = characterNodeRepository.findAllRelationshipsByProjectId(projectId);

        log.info("Found {} relationships for project {}", relationships.size(), projectId);
        return relationships;
    }

    /**
     * 가장 많은 관계를 가진 캐릭터 찾기 (중심 인물)
     * @param limit 결과 개수 제한
     * @return 중심 인물 목록 (캐릭터 + 관계 개수)
     */
    public List<Map<String, Object>> findMostConnectedCharacters(int limit) {
        log.info("Finding top {} most connected characters", limit);

        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("Limit must be between 1 and 50");
        }

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> results = characterNodeRepository.findMostConnectedCharacters(projectId, limit);

        log.info("Found {} central characters", results.size());

        // 결과를 Map으로 변환
        return results.stream()
            .map(obj -> {
                // Neo4j 결과를 Map으로 변환하는 로직
                // 실제로는 obj를 파싱해야 하지만, 간단히 Map으로 래핑
                return Map.<String, Object>of("result", obj);
            })
            .toList();
    }

    /**
     * 특정 관계 유형으로 연결된 캐릭터 찾기
     * @param characterId 시작 캐릭터 ID
     * @param relationType 관계 유형 (friend, rival, family, lover, enemy)
     * @return 연결된 캐릭터 목록
     */
    public List<CharacterNode> findCharactersByRelationType(String characterId, String relationType) {
        log.info("Finding characters connected to {} by relation type: {}", characterId, relationType);

        List<CharacterNode> characters = characterNodeRepository.findCharactersByRelationType(characterId, relationType);

        log.info("Found {} characters with relation type '{}'", characters.size(), relationType);
        return characters;
    }

    /**
     * 프로젝트의 모든 캐릭터 조회
     * @return 캐릭터 목록
     */
    public List<CharacterNode> findAllCharacters() {
        log.info("Finding all characters for current project");

        Long projectId = projectService.getCurrentProject().getId();
        List<CharacterNode> characters = characterNodeRepository.findByProjectId(projectId);

        log.info("Found {} characters for project {}", characters.size(), projectId);
        return characters;
    }

    /**
     * 캐릭터 ID로 조회
     * @param characterId 캐릭터 ID
     * @return 캐릭터 노드
     */
    public CharacterNode findByCharacterId(String characterId) {
        log.debug("Finding character by characterId: {}", characterId);

        return characterNodeRepository.findByCharacterId(characterId)
            .orElseThrow(() -> {
                log.error("Character not found in Neo4j: {}", characterId);
                return new IllegalArgumentException("Character not found in graph: " + characterId);
            });
    }

    /**
     * Degree Centrality 계산
     * 가장 많은 직접 연결을 가진 캐릭터 찾기
     * @param limit 결과 개수
     * @return Degree Centrality 순위
     */
    public List<Object> calculateDegreeCentrality(int limit) {
        log.info("Calculating Degree Centrality (top {})", limit);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> results = characterNodeRepository.calculateDegreeCentrality(projectId, limit);

        log.info("Degree Centrality calculated: {} results", results.size());
        return results;
    }

    /**
     * Betweenness Centrality 계산
     * 다른 캐릭터들 사이의 경로에 자주 등장하는 캐릭터 (중개자 역할)
     * @param limit 결과 개수
     * @return Betweenness Centrality 순위
     */
    public List<Object> calculateBetweennessCentrality(int limit) {
        log.info("Calculating Betweenness Centrality (top {})", limit);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> results = characterNodeRepository.calculateBetweennessCentrality(projectId, limit);

        log.info("Betweenness Centrality calculated: {} results", results.size());
        return results;
    }

    /**
     * Closeness Centrality 계산
     * 다른 모든 캐릭터와의 평균 거리가 가장 짧은 캐릭터
     * @param limit 결과 개수
     * @return Closeness Centrality 순위
     */
    public List<Object> calculateClosenessCentrality(int limit) {
        log.info("Calculating Closeness Centrality (top {})", limit);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> results = characterNodeRepository.calculateClosenessCentrality(projectId, limit);

        log.info("Closeness Centrality calculated: {} results", results.size());
        return results;
    }

    /**
     * Weighted Degree 계산
     * closeness 값을 가중치로 사용한 관계 강도 계산
     * @param limit 결과 개수
     * @return Weighted Degree 순위
     */
    public List<Object> calculateWeightedDegree(int limit) {
        log.info("Calculating Weighted Degree (top {})", limit);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> results = characterNodeRepository.calculateWeightedDegree(projectId, limit);

        log.info("Weighted Degree calculated: {} results", results.size());
        return results;
    }

    /**
     * 모든 Centrality 지표를 한번에 계산
     * @param limit 각 지표당 결과 개수
     * @return 모든 지표 결과
     */
    public Map<String, Object> calculateAllCentralities(int limit) {
        log.info("Calculating all centrality metrics (top {})", limit);

        return Map.of(
            "degreeCentrality", calculateDegreeCentrality(limit),
            "betweennessCentrality", calculateBetweennessCentrality(limit),
            "closenessCentrality", calculateClosenessCentrality(limit),
            "weightedDegree", calculateWeightedDegree(limit)
        );
    }

    /**
     * 에피소드 범위별 관계 변화 조회
     * @param startEpisodeId 시작 에피소드 ID
     * @param endEpisodeId 종료 에피소드 ID
     * @return 에피소드 범위 내 관계 목록
     */
    public List<Object> findRelationshipsByEpisodeRange(Long startEpisodeId, Long endEpisodeId) {
        log.info("Finding relationships from episode {} to {}", startEpisodeId, endEpisodeId);

        if (startEpisodeId > endEpisodeId) {
            throw new IllegalArgumentException("Start episode ID must be less than or equal to end episode ID");
        }

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> relationships = characterNodeRepository.findRelationshipsByEpisodeRange(
            projectId, startEpisodeId, endEpisodeId
        );

        log.info("Found {} relationships in episode range {}-{}", relationships.size(), startEpisodeId, endEpisodeId);
        return relationships;
    }

    /**
     * 특정 캐릭터의 관계 진화 추적
     * @param characterId 캐릭터 ID
     * @return 시간별 관계 진화 데이터
     */
    public List<Object> findCharacterRelationshipEvolution(String characterId) {
        log.info("Finding relationship evolution for character: {}", characterId);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> evolution = characterNodeRepository.findCharacterRelationshipEvolution(characterId, projectId);

        log.info("Found {} relationship evolution entries for character {}", evolution.size(), characterId);
        return evolution;
    }

    /**
     * 두 캐릭터 간 관계 타임라인
     * @param char1Id 캐릭터 1 ID
     * @param char2Id 캐릭터 2 ID
     * @return 관계 변화 타임라인
     */
    public List<Object> findRelationshipTimeline(String char1Id, String char2Id) {
        log.info("Finding relationship timeline between {} and {}", char1Id, char2Id);

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> timeline = characterNodeRepository.findRelationshipTimeline(char1Id, char2Id, projectId);

        log.info("Found {} timeline entries for relationship {}-{}", timeline.size(), char1Id, char2Id);
        return timeline;
    }

    /**
     * 에피소드별 네트워크 밀도 계산
     * @param episodeId 에피소드 ID
     * @return 네트워크 밀도 정보
     */
    public Object calculateNetworkDensityByEpisode(Long episodeId) {
        log.info("Calculating network density for episode: {}", episodeId);

        Long projectId = projectService.getCurrentProject().getId();
        Object density = characterNodeRepository.calculateNetworkDensityByEpisode(episodeId, projectId);

        log.info("Network density calculated for episode {}", episodeId);
        return density;
    }

    /**
     * 새로운 관계 추가 현황 조회
     * @return 에피소드별 새 관계 목록
     */
    public List<Object> findNewRelationshipsByEpisode() {
        log.info("Finding new relationships by episode");

        Long projectId = projectService.getCurrentProject().getId();
        List<Object> newRelationships = characterNodeRepository.findNewRelationshipsByEpisode(projectId);

        log.info("Found {} new relationship entries", newRelationships.size());
        return newRelationships;
    }
}
