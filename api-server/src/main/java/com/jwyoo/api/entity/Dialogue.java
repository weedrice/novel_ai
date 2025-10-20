package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dialogues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dialogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(nullable = false, length = 2000)
    private String text; // 대사 내용

    @Column(nullable = false)
    private Integer dialogueOrder; // 대사 순서

    @Column(length = 50)
    private String intent; // 대화 의도 (예: reconcile, argue)

    @Column(length = 50)
    private String honorific; // 존댓말 유형 (예: banmal, jondae)

    @Column(length = 50)
    private String emotion; // 감정 (예: happy, angry, sad)

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