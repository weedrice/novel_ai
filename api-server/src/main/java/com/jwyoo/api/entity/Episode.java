package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episodes", indexes = {
    @Index(name = "idx_episode_project_id", columnList = "project_id"),
    @Index(name = "idx_episode_order_project", columnList = "episodeOrder,project_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer episodeOrder; // 에피소드 순서

    /**
     * 스크립트 원문 내용 (에피소드의 원본 텍스트)
     */
    @Column(columnDefinition = "TEXT")
    @Lob
    private String scriptText;

    /**
     * 스크립트 형식 (novel, screenplay, description, dialogue 등)
     */
    @Column(length = 50)
    private String scriptFormat;

    /**
     * 분석 상태 (not_analyzed, analyzing, analyzed, failed)
     */
    @Column(length = 20)
    private String analysisStatus;

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
    private String llmProvider;

    /**
     * 프로젝트 (사용자별 데이터 분리)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sceneNumber ASC")
    @Builder.Default
    private List<Scene> scenes = new ArrayList<>();

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