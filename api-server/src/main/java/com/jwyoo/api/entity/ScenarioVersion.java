package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 시나리오 버전 엔티티
 * 장면별로 생성된 시나리오를 버전 관리합니다.
 */
@Entity
@Table(name = "scenario_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연관된 장면
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    /**
     * 버전 번호 (자동 증가)
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * 버전 제목 (사용자가 지정)
     */
    @Column(length = 200)
    private String title;

    /**
     * 시나리오 내용 (JSON 형식)
     * [{speaker: "캐릭터명", text: "대사", order: 1}, ...]
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 생성일시
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 생성자 (사용자 ID, 추후 인증 기능 추가 시 사용)
     */
    @Column(length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}