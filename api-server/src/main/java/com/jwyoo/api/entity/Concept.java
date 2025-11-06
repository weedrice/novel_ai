package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 추상적 개념 엔티티
 * 에피소드, 캐릭터와 연관된 테마, 감정, 사건 등을 추상적 개념으로 관리
 * Neo4j ConceptNode와 pgvector Embedding을 연결하여 고급 분석 제공
 */
@Entity
@EntityListeners(com.jwyoo.api.event.ConceptSyncEventListener.class)
@Table(name = "concepts", indexes = {
    @Index(name = "idx_concept_episode_id", columnList = "episode_id"),
    @Index(name = "idx_concept_project_id", columnList = "project_id"),
    @Index(name = "idx_concept_type", columnList = "type"),
    @Index(name = "idx_concept_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Concept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 개념 이름
     * 예: "사랑", "우정", "배신", "갈등", "성장"
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 개념 유형
     * - theme: 테마 (예: 성장, 사랑, 우정)
     * - emotion: 감정 (예: 기쁨, 슬픔, 분노)
     * - event: 사건 (예: 만남, 이별, 갈등)
     * - setting: 배경 (예: 학교, 집, 카페)
     * - trait: 특성 (예: 용기, 비겁, 친절)
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 개념 설명
     * 이 설명은 임베딩으로 변환되어 의미 검색에 사용됨
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 연관된 에피소드 (선택적)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    /**
     * 프로젝트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 중요도 (0.0 ~ 1.0)
     * AI 분석 결과에서 추출된 신뢰도 또는 중요도
     */
    @Column
    private Double importance;

    /**
     * 출처
     * - ai_analysis: AI 분석 결과에서 자동 추출
     * - manual: 사용자가 수동으로 생성
     */
    @Column(length = 50)
    @Builder.Default
    private String source = "manual";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
