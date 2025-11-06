package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 에피소드별 캐릭터 관계
 * 각 에피소드에서의 캐릭터 간 관계 상태를 저장하여 시간에 따른 관계 변화를 추적
 */
@Entity
@Table(name = "episode_relationships", indexes = {
    @Index(name = "idx_episode_rel_episode", columnList = "episode_id"),
    @Index(name = "idx_episode_rel_characters", columnList = "from_character_id,to_character_id,episode_id")
})
@EntityListeners(com.jwyoo.api.graph.event.GraphSyncEventListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpisodeRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 관계가 나타난 에피소드
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    /**
     * 관계의 주체 캐릭터
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_character_id", nullable = false)
    private Character fromCharacter;

    /**
     * 관계의 대상 캐릭터
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_character_id", nullable = false)
    private Character toCharacter;

    /**
     * 관계 유형 (friend, rival, family, lover, enemy, stranger 등)
     */
    @Column(nullable = false, length = 50)
    private String relationType;

    /**
     * 친밀도 (0.0 ~ 10.0)
     * 숫자가 클수록 친밀함
     */
    @Column
    private Double closeness;

    /**
     * 관계 설명 (해당 에피소드에서의 관계 변화 또는 특징)
     */
    @Column(length = 1000)
    private String description;

    /**
     * 생성 일시
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
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
