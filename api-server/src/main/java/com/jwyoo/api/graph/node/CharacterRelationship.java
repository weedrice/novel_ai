package com.jwyoo.api.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/**
 * Neo4j 캐릭터 간 관계 (INTERACTS_WITH)
 * PostgreSQL EpisodeRelationship 엔티티의 그래프 DB 버전
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterRelationship {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * 에피소드 ID (어느 에피소드에서의 관계인지)
     */
    private Long episodeId;

    /**
     * 관계 유형 (friend, rival, family, lover, enemy)
     */
    private String relationType;

    /**
     * 친밀도 (0.0 - 10.0)
     */
    private Double closeness;

    /**
     * 관계 설명
     */
    private String description;

    /**
     * 관계 대상 캐릭터
     */
    @TargetNode
    private CharacterNode targetCharacter;
}
