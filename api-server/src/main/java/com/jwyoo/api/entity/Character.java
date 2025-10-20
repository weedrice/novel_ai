package com.jwyoo.api.entity;

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

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Dialogue> dialogues = new ArrayList<>();

    @OneToMany(mappedBy = "fromCharacter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Relationship> relationshipsFrom = new ArrayList<>();

    @OneToMany(mappedBy = "toCharacter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
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