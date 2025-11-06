package com.jwyoo.api.graph.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j Concept 노드
 * 추상적 개념 (테마, 감정, 사건 등)을 그래프로 표현
 */
@Node("Concept")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptNode {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * RDB Concept.id
     */
    @Property("rdbId")
    private Long rdbId;

    /**
     * 프로젝트 ID
     */
    @Property("projectId")
    private Long projectId;

    /**
     * 개념 이름
     */
    @Property("name")
    private String name;

    /**
     * 개념 유형 (theme, emotion, event, setting, trait)
     */
    @Property("type")
    private String type;

    /**
     * 개념 설명
     */
    @Property("description")
    private String description;

    /**
     * 중요도 (0.0 ~ 1.0)
     */
    @Property("importance")
    private Double importance;

    /**
     * 연관된 에피소드 ID (선택적)
     */
    @Property("episodeId")
    private Long episodeId;

    /**
     * 관련된 다른 개념들
     * RELATES_TO 관계
     */
    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<ConceptRelationship> relatedConcepts = new ArrayList<>();

    /**
     * 개념 간 관계 (유사도 기반)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConceptRelationship {

        /**
         * 관련된 개념
         */
        private ConceptNode concept;

        /**
         * 관계 유형
         * - similar: 유사한 개념
         * - opposite: 반대 개념
         * - contains: 포함 관계
         * - derived: 파생 관계
         */
        @Property("relationType")
        private String relationType;

        /**
         * 유사도 또는 관련 강도 (0.0 ~ 1.0)
         */
        @Property("similarity")
        private Double similarity;
    }
}
