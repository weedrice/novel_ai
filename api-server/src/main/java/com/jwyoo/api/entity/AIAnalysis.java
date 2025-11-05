package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 분석 결과 엔티티
 * Episode의 analysisResult(JSONB)를 구조화하여 여러 AI 모델 결과를 비교하고 분석 히스토리를 추적합니다.
 */
@Entity
@Table(name = "ai_analyses", indexes = {
    @Index(name = "idx_ai_analysis_episode_id", columnList = "episode_id"),
    @Index(name = "idx_ai_analysis_type", columnList = "analysisType"),
    @Index(name = "idx_ai_analysis_model", columnList = "modelName"),
    @Index(name = "idx_ai_analysis_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 분석 대상 에피소드
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    /**
     * 분석 유형
     * - sentiment: 감정 분석
     * - summary: 요약
     * - tone: 어조 분석
     * - character_extraction: 캐릭터 추출
     * - relationship_extraction: 관계 추출
     * - scene_extraction: 장면 추출
     * - dialogue_extraction: 대사 추출
     */
    @Column(nullable = false, length = 50)
    private String analysisType;

    /**
     * 사용된 AI 모델 이름
     * 예: gpt-4, gpt-3.5-turbo, claude-3-opus, claude-3-sonnet, gemini-pro
     */
    @Column(nullable = false, length = 100)
    private String modelName;

    /**
     * 분석 결과 (JSON 형식)
     * 분석 유형에 따라 다른 구조의 JSON 데이터 저장
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    @Lob
    private String result;

    /**
     * 분석 신뢰도 (0.0 ~ 1.0)
     * AI 모델이 제공하는 신뢰도 점수 또는 자체 평가 점수
     */
    @Column
    private Double confidence;

    /**
     * 분석 실행 시간 (밀리초)
     * 성능 모니터링 및 최적화를 위해 사용
     */
    @Column
    private Long executionTimeMs;

    /**
     * 분석 상태
     * - pending: 대기 중
     * - running: 실행 중
     * - completed: 완료
     * - failed: 실패
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "completed";

    /**
     * 에러 메시지 (분석 실패 시)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

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
