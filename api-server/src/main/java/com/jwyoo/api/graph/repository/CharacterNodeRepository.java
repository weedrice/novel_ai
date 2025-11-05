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
}
