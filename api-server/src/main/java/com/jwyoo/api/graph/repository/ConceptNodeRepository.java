package com.jwyoo.api.graph.repository;

import com.jwyoo.api.graph.node.ConceptNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptNodeRepository extends Neo4jRepository<ConceptNode, Long> {

    /**
     * RDB ID로 개념 조회
     */
    Optional<ConceptNode> findByRdbId(Long rdbId);

    /**
     * 프로젝트별 개념 조회
     */
    List<ConceptNode> findByProjectId(Long projectId);

    /**
     * 프로젝트별 + 유형별 개념 조회
     */
    List<ConceptNode> findByProjectIdAndType(Long projectId, String type);

    /**
     * 유사한 개념 찾기 (RELATES_TO 관계 기반)
     *
     * @param conceptId 개념 ID (Neo4j ID)
     * @param minSimilarity 최소 유사도
     * @param limit 결과 수
     */
    @Query("MATCH (c:Concept)-[r:RELATES_TO]->(related:Concept) " +
           "WHERE id(c) = $conceptId AND r.similarity >= $minSimilarity " +
           "RETURN related, r " +
           "ORDER BY r.similarity DESC " +
           "LIMIT $limit")
    List<ConceptNode> findSimilarConcepts(@Param("conceptId") Long conceptId,
                                           @Param("minSimilarity") Double minSimilarity,
                                           @Param("limit") Integer limit);

    /**
     * 특정 유형의 유사한 개념 찾기
     */
    @Query("MATCH (c:Concept)-[r:RELATES_TO]->(related:Concept) " +
           "WHERE id(c) = $conceptId AND related.type = $type AND r.similarity >= $minSimilarity " +
           "RETURN related, r " +
           "ORDER BY r.similarity DESC " +
           "LIMIT $limit")
    List<ConceptNode> findSimilarConceptsByType(@Param("conceptId") Long conceptId,
                                                  @Param("type") String type,
                                                  @Param("minSimilarity") Double minSimilarity,
                                                  @Param("limit") Integer limit);

    /**
     * 에피소드와 연관된 개념 찾기
     */
    @Query("MATCH (e:Episode)-[r:CONTAINS]->(c:Concept) " +
           "WHERE e.rdbId = $episodeId " +
           "RETURN c " +
           "ORDER BY c.importance DESC")
    List<ConceptNode> findByEpisodeId(@Param("episodeId") Long episodeId);

    /**
     * 캐릭터와 연관된 개념 찾기
     */
    @Query("MATCH (ch:Character)-[r:RELATES_TO]->(c:Concept) " +
           "WHERE ch.rdbId = $characterId " +
           "RETURN c " +
           "ORDER BY c.importance DESC")
    List<ConceptNode> findByCharacterId(@Param("characterId") Long characterId);

    /**
     * 중요한 개념 상위 N개 조회
     */
    @Query("MATCH (c:Concept) " +
           "WHERE c.projectId = $projectId " +
           "RETURN c " +
           "ORDER BY c.importance DESC " +
           "LIMIT $limit")
    List<ConceptNode> findTopConceptsByImportance(@Param("projectId") Long projectId,
                                                    @Param("limit") Integer limit);

    /**
     * 두 개념 간 관계 생성
     */
    @Query("MATCH (c1:Concept), (c2:Concept) " +
           "WHERE id(c1) = $fromId AND id(c2) = $toId " +
           "MERGE (c1)-[r:RELATES_TO {relationType: $relationType, similarity: $similarity}]->(c2) " +
           "RETURN c1, r, c2")
    void createRelationship(@Param("fromId") Long fromId,
                             @Param("toId") Long toId,
                             @Param("relationType") String relationType,
                             @Param("similarity") Double similarity);

    /**
     * RDB ID로 개념 삭제
     */
    @Query("MATCH (c:Concept {rdbId: $rdbId}) DETACH DELETE c")
    void deleteByRdbId(@Param("rdbId") Long rdbId);
}
