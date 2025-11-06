package com.jwyoo.api.graph.repository;

import com.jwyoo.api.graph.node.CharacterNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Neo4j 캐릭터 노드 Repository
 */
@Repository
public interface CharacterNodeRepository extends Neo4jRepository<CharacterNode, Long> {

    /**
     * RDB ID로 캐릭터 노드 조회
     */
    Optional<CharacterNode> findByRdbId(Long rdbId);

    /**
     * 프로젝트 ID로 모든 캐릭터 조회
     */
    List<CharacterNode> findByProjectId(Long projectId);

    /**
     * 캐릭터 ID로 조회
     */
    Optional<CharacterNode> findByCharacterId(String characterId);

    /**
     * 특정 캐릭터의 N단계 친구 찾기 (친구의 친구)
     * @param characterId 시작 캐릭터 ID
     * @param depth 탐색 깊이 (1: 직접 친구, 2: 친구의 친구, etc.)
     * @return 찾은 캐릭터 목록
     */
    @Query("""
        MATCH path = (start:Character {characterId: $characterId})-[:INTERACTS_WITH*1..$depth]-(friend:Character)
        WHERE start <> friend
        RETURN DISTINCT friend
        """)
    List<CharacterNode> findNDegreeFriends(@Param("characterId") String characterId, @Param("depth") int depth);

    /**
     * 두 캐릭터 사이의 최단 경로 찾기
     */
    @Query("""
        MATCH path = shortestPath(
          (start:Character {characterId: $fromCharacterId})-[:INTERACTS_WITH*]-(end:Character {characterId: $toCharacterId})
        )
        RETURN nodes(path) as characters, relationships(path) as relations, length(path) as distance
        """)
    Object findShortestPath(@Param("fromCharacterId") String fromCharacterId,
                           @Param("toCharacterId") String toCharacterId);

    /**
     * 특정 에피소드에서의 캐릭터 관계 조회
     */
    @Query("""
        MATCH (c1:Character)-[r:INTERACTS_WITH {episodeId: $episodeId}]->(c2:Character)
        WHERE c1.projectId = $projectId
        RETURN c1, r, c2
        """)
    List<Object> findRelationshipsByEpisodeId(@Param("episodeId") Long episodeId, @Param("projectId") Long projectId);

    /**
     * 프로젝트의 모든 관계 조회 (그래프 시각화용)
     */
    @Query("""
        MATCH (c1:Character)-[r:INTERACTS_WITH]->(c2:Character)
        WHERE c1.projectId = $projectId
        RETURN c1, r, c2
        """)
    List<Object> findAllRelationshipsByProjectId(@Param("projectId") Long projectId);

    /**
     * 가장 많은 관계를 가진 캐릭터 찾기 (중심 인물)
     */
    @Query("""
        MATCH (c:Character)-[r:INTERACTS_WITH]-(other:Character)
        WHERE c.projectId = $projectId
        RETURN c, count(r) as relationshipCount
        ORDER BY relationshipCount DESC
        LIMIT $limit
        """)
    List<Object> findMostConnectedCharacters(@Param("projectId") Long projectId, @Param("limit") int limit);

    /**
     * 특정 관계 유형으로 연결된 캐릭터 찾기
     */
    @Query("""
        MATCH (c1:Character)-[r:INTERACTS_WITH {relationType: $relationType}]->(c2:Character)
        WHERE c1.characterId = $characterId
        RETURN c2
        """)
    List<CharacterNode> findCharactersByRelationType(@Param("characterId") String characterId,
                                                      @Param("relationType") String relationType);

    /**
     * Degree Centrality 계산 (연결된 관계 개수)
     * 가장 많은 직접 연결을 가진 캐릭터 찾기
     */
    @Query("""
        MATCH (c:Character)-[r:INTERACTS_WITH]-(other:Character)
        WHERE c.projectId = $projectId
        RETURN c.characterId as characterId, c.name as name, count(DISTINCT other) as degreeCentrality
        ORDER BY degreeCentrality DESC
        LIMIT $limit
        """)
    List<Object> calculateDegreeCentrality(@Param("projectId") Long projectId, @Param("limit") int limit);

    /**
     * Betweenness Centrality 계산 (중개자 역할)
     * 다른 캐릭터들 사이의 경로에 자주 등장하는 캐릭터
     */
    @Query("""
        MATCH (c:Character)
        WHERE c.projectId = $projectId
        WITH c
        MATCH (a:Character)-[*]-(c)-[*]-(b:Character)
        WHERE a <> b AND a <> c AND b <> c
            AND a.projectId = $projectId AND b.projectId = $projectId
        WITH c, count(DISTINCT [a, b]) as paths
        RETURN c.characterId as characterId, c.name as name, paths as betweennessCentrality
        ORDER BY betweennessCentrality DESC
        LIMIT $limit
        """)
    List<Object> calculateBetweennessCentrality(@Param("projectId") Long projectId, @Param("limit") int limit);

    /**
     * Closeness Centrality 계산 (중심성)
     * 다른 모든 캐릭터와의 평균 거리가 가장 짧은 캐릭터
     */
    @Query("""
        MATCH (c:Character)
        WHERE c.projectId = $projectId
        WITH c
        MATCH (c)-[*]-(other:Character)
        WHERE c <> other AND other.projectId = $projectId
        WITH c, avg(length(shortestPath((c)-[*]-(other)))) as avgDistance
        WHERE avgDistance IS NOT NULL
        RETURN c.characterId as characterId, c.name as name,
               1.0/avgDistance as closenessCentrality
        ORDER BY closenessCentrality DESC
        LIMIT $limit
        """)
    List<Object> calculateClosenessCentrality(@Param("projectId") Long projectId, @Param("limit") int limit);

    /**
     * Weighted Degree (가중치 합계)
     * closeness 값을 가중치로 사용한 관계 강도 계산
     */
    @Query("""
        MATCH (c:Character)-[r:INTERACTS_WITH]-(other:Character)
        WHERE c.projectId = $projectId
        RETURN c.characterId as characterId, c.name as name,
               sum(r.closeness) as weightedDegree, count(r) as relationshipCount
        ORDER BY weightedDegree DESC
        LIMIT $limit
        """)
    List<Object> calculateWeightedDegree(@Param("projectId") Long projectId, @Param("limit") int limit);

    /**
     * 에피소드 범위별 관계 변화 추적
     * 특정 에피소드 범위 내에서 관계가 어떻게 변화했는지 추적
     */
    @Query("""
        MATCH (c1:Character)-[r:INTERACTS_WITH]-(c2:Character)
        WHERE c1.projectId = $projectId
          AND r.episodeId >= $startEpisodeId
          AND r.episodeId <= $endEpisodeId
        RETURN c1, r, c2, r.episodeId as episodeId
        ORDER BY r.episodeId
        """)
    List<Object> findRelationshipsByEpisodeRange(
        @Param("projectId") Long projectId,
        @Param("startEpisodeId") Long startEpisodeId,
        @Param("endEpisodeId") Long endEpisodeId
    );

    /**
     * 특정 캐릭터의 시간별 관계 진화
     * 한 캐릭터의 관계가 에피소드별로 어떻게 변했는지 추적
     */
    @Query("""
        MATCH (c:Character {characterId: $characterId})-[r:INTERACTS_WITH]-(other:Character)
        WHERE c.projectId = $projectId
        RETURN c, r, other, r.episodeId as episodeId, r.closeness as closeness
        ORDER BY r.episodeId
        """)
    List<Object> findCharacterRelationshipEvolution(
        @Param("characterId") String characterId,
        @Param("projectId") Long projectId
    );

    /**
     * 두 캐릭터 간 관계 변화 타임라인
     * 두 캐릭터 사이의 관계가 시간에 따라 어떻게 변했는지 추적
     */
    @Query("""
        MATCH (c1:Character {characterId: $char1Id})-[r:INTERACTS_WITH]-(c2:Character {characterId: $char2Id})
        WHERE c1.projectId = $projectId
        RETURN r.episodeId as episodeId, r.relationType as relationType,
               r.closeness as closeness, r.description as description
        ORDER BY r.episodeId
        """)
    List<Object> findRelationshipTimeline(
        @Param("char1Id") String char1Id,
        @Param("char2Id") String char2Id,
        @Param("projectId") Long projectId
    );

    /**
     * 에피소드별 네트워크 밀도 계산
     * 각 에피소드에서 전체 네트워크의 연결 밀도 측정
     */
    @Query("""
        MATCH (c:Character)
        WHERE c.projectId = $projectId
        WITH count(c) as totalNodes
        MATCH (c1:Character)-[r:INTERACTS_WITH]-(c2:Character)
        WHERE c1.projectId = $projectId AND r.episodeId = $episodeId
        WITH totalNodes, count(DISTINCT r) as totalEdges
        RETURN totalNodes, totalEdges,
               toFloat(totalEdges) / (totalNodes * (totalNodes - 1) / 2) as density
        """)
    Object calculateNetworkDensityByEpisode(@Param("episodeId") Long episodeId, @Param("projectId") Long projectId);

    /**
     * 에피소드별 새로운 관계 추가 현황
     * 각 에피소드에서 새롭게 형성된 관계 추적
     */
    @Query("""
        MATCH (c1:Character)-[r:INTERACTS_WITH]-(c2:Character)
        WHERE c1.projectId = $projectId
        WITH c1, c2, r, r.episodeId as episodeId
        ORDER BY episodeId
        WITH c1, c2, collect({episodeId: episodeId, relation: r}) as timeline
        RETURN c1.characterId as char1, c2.characterId as char2,
               timeline[0].episodeId as firstAppearance,
               size(timeline) as interactionCount
        ORDER BY firstAppearance
        """)
    List<Object> findNewRelationshipsByEpisode(@Param("projectId") Long projectId);
}
