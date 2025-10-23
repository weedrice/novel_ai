package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 업로드된 스크립트 엔티티
 * 소설, 시나리오, 묘사 등 다양한 형식의 텍스트를 저장
 */
@Entity
@Table(name = "scripts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 스크립트 제목
     */
    @Column(nullable = false, length = 500)
    private String title;

    /**
     * 스크립트 원문 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    @Lob
    private String content;

    /**
     * 스크립트 형식 힌트 (novel, scenario, description 등)
     * 선택 사항, LLM이 자동으로 판단할 수도 있음
     */
    @Column(length = 50)
    private String formatHint;

    /**
     * 분석 상태 (uploaded, analyzing, analyzed, failed)
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "uploaded";

    /**
     * 분석 결과 (JSON 형식)
     * 캐릭터, 대사, 장면 등 LLM이 추출한 정보
     */
    @Column(columnDefinition = "TEXT")
    @Lob
    private String analysisResult;

    /**
     * 분석 시 사용한 LLM 프로바이더
     */
    @Column(length = 50)
    private String provider;

    /**
     * 생성 일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @Column(name = "updated_at", nullable = false)
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