package com.jwyoo.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_character_id", nullable = false)
    private Character fromCharacter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_character_id", nullable = false)
    private Character toCharacter;

    @Column(nullable = false, length = 50)
    private String relationType; // 관계 유형 (예: friend, rival, family)

    @Column
    private Double closeness; // 친밀도 (0.0 ~ 10.0)

    @Column(length = 500)
    private String description; // 관계 설명

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