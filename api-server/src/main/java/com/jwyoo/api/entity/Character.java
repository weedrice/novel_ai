package com.jwyoo.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String characterId; // 예: "char.seha"

    @Column(nullable = false, length = 100)
    private String name; // 예: "세하"

    @Column(length = 1000)
    private String description; // 캐릭터 설명

    @Column(length = 500)
    private String personality; // 성격

    @Column(length = 500)
    private String speakingStyle; // 말투 특징

    @Column(length = 1000)
    private String vocabulary; // 자주 사용하는 어휘 (쉼표로 구분)

    @Column(length = 1000)
    private String toneKeywords; // 말투 키워드 (쉼표로 구분)

    @Column(columnDefinition = "TEXT")
    private String examples; // 실제 대사 예시 (줄바꿈으로 구분, Few-shot 학습용)

    @Column(length = 1000)
    private String prohibitedWords; // 사용하지 않는 단어 목록 (쉼표로 구분)

    @Column(columnDefinition = "TEXT")
    private String sentencePatterns; // 문장 패턴 예시 (줄바꿈으로 구분)

    /**
     * 프로젝트 (사용자별 데이터 분리)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"character", "scene"})
    private List<Dialogue> dialogues = new ArrayList<>();

    @OneToMany(mappedBy = "fromCharacter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"fromCharacter", "toCharacter"})
    private List<Relationship> relationshipsFrom = new ArrayList<>();

    @OneToMany(mappedBy = "toCharacter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"fromCharacter", "toCharacter"})
    private List<Relationship> relationshipsTo = new ArrayList<>();

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