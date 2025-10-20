package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private Integer sceneNumber;

    @Column(length = 200)
    private String location; // 장면 위치

    @Column(length = 100)
    private String mood; // 분위기 (예: tense, happy, sad)

    @Column(length = 2000)
    private String description; // 장면 설명

    @Column(length = 500)
    private String participants; // 참여 캐릭터 ID (쉼표로 구분, 예: "char.seha,char.jiho")

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dialogueOrder ASC")
    @Builder.Default
    private List<Dialogue> dialogues = new ArrayList<>();

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