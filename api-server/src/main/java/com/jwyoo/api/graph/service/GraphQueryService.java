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
}
