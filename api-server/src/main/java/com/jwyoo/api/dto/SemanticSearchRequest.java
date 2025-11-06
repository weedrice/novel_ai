package com.jwyoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 의미 검색 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemanticSearchRequest {

    /**
     * 검색 쿼리 (자연어)
     */
    private String query;

    /**
     * 키워드 (하이브리드 검색용)
     */
    private String keyword;

    /**
     * 소스 타입 필터 (dialogue, scene, episode, character)
     */
    private String sourceType;

    /**
     * 결과 개수 제한 (기본값: 10)
     */
    private Integer limit;
}
