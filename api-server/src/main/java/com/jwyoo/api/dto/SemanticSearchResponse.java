package com.jwyoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 의미 검색 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemanticSearchResponse {

    /**
     * RagVector ID
     */
    private Long id;

    /**
     * 소스 타입 (dialogue, scene, episode, character)
     */
    private String sourceType;

    /**
     * 소스 ID (해당 엔티티의 ID)
     */
    private Long sourceId;

    /**
     * 텍스트 내용
     */
    private String textChunk;

    /**
     * 메타데이터 (JSON)
     */
    private String metadata;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
}
