package com.jwyoo.api.entity;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * RAG Vector 엔티티
 * 대사, 장면, 에피소드 등의 임베딩 벡터를 저장하여 의미 기반 검색 지원
 */
@Entity
@Table(name = "rag_vectors", indexes = {
    @Index(name = "idx_rag_vectors_source", columnList = "source_type, source_id"),
    @Index(name = "idx_rag_vectors_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소스 타입: dialogue, scene, episode, character
     */
    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    /**
     * 소스 ID (해당 엔티티의 ID)
     */
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    /**
     * 텍스트 청크 (실제 내용)
     */
    @Column(name = "text_chunk", nullable = false, columnDefinition = "TEXT")
    private String textChunk;

    /**
     * 임베딩 벡터 (OpenAI text-embedding-ada-002: 1536 dimensions)
     */
    @Column(name = "embedding", nullable = false, columnDefinition = "vector(1536)")
    private PGvector embedding;

    /**
     * 메타데이터 (JSONB)
     * 예: {"characterId": 1, "episodeId": 2, "tone": "sad"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    /**
     * 생성 시간
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
