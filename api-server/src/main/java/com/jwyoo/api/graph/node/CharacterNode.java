package com.jwyoo.api.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j 캐릭터 노드
 * PostgreSQL Character 엔티티의 그래프 DB 버전
 */
@Node("Character")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterNode {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * PostgreSQL Character ID (동기화용)
     */
    private Long rdbId;

    /**
     * 프로젝트 ID (동기화용)
     */
    private Long projectId;

    /**
     * 캐릭터 ID (고유 식별자)
     */
    private String characterId;

    /**
     * 캐릭터 이름
     */
    private String name;

    /**
     * 캐릭터 설명
     */
    private String description;

    /**
     * 캐릭터 성격
     */
    private String personality;

    /**
     * 말투 스타일
     */
    private String speakingStyle;

    /**
     * 다른 캐릭터와의 관계
     * (INTERACTS_WITH 관계)
     */
    @Relationship(type = "INTERACTS_WITH", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<CharacterRelationship> relationships = new ArrayList<>();
}
