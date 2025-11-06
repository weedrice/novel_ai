package com.jwyoo.api.controller;

import com.jwyoo.api.dto.SemanticSearchRequest;
import com.jwyoo.api.dto.SemanticSearchResponse;
import com.jwyoo.api.entity.RagVector;
import com.jwyoo.api.service.RagVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 의미 기반 검색 API
 * Vector DB를 사용한 시맨틱 검색
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final RagVectorService ragVectorService;

    /**
     * 의미 기반 검색 (Vector Similarity)
     *
     * @param request 검색 요청 (query, limit)
     * @return 유사도 기반 검색 결과
     */
    @PostMapping("/semantic")
    public ResponseEntity<List<SemanticSearchResponse>> searchSemantic(@RequestBody SemanticSearchRequest request) {
        log.info("Semantic search request: query={}, limit={}", request.getQuery(), request.getLimit());

        int limit = request.getLimit() != null ? request.getLimit() : 10;
        List<RagVector> results = ragVectorService.searchSimilar(request.getQuery(), limit);

        List<SemanticSearchResponse> response = results.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        log.info("Found {} results", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 타입별 의미 검색 (dialogue, scene, episode, character)
     *
     * @param request 검색 요청
     * @return 유사도 기반 검색 결과 (타입 필터 적용)
     */
    @PostMapping("/semantic/by-type")
    public ResponseEntity<List<SemanticSearchResponse>> searchSemanticByType(@RequestBody SemanticSearchRequest request) {
        log.info("Semantic search by type: query={}, sourceType={}, limit={}",
            request.getQuery(), request.getSourceType(), request.getLimit());

        if (request.getSourceType() == null || request.getSourceType().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        int limit = request.getLimit() != null ? request.getLimit() : 10;
        List<RagVector> results = ragVectorService.searchSimilarByType(
            request.getQuery(),
            request.getSourceType(),
            limit
        );

        List<SemanticSearchResponse> response = results.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        log.info("Found {} results for type {}", response.size(), request.getSourceType());
        return ResponseEntity.ok(response);
    }

    /**
     * 하이브리드 검색 (Vector Similarity + Keyword)
     *
     * @param request 검색 요청 (query, keyword, limit)
     * @return 하이브리드 검색 결과
     */
    @PostMapping("/hybrid")
    public ResponseEntity<List<SemanticSearchResponse>> searchHybrid(@RequestBody SemanticSearchRequest request) {
        log.info("Hybrid search request: query={}, keyword={}, limit={}",
            request.getQuery(), request.getKeyword(), request.getLimit());

        if (request.getKeyword() == null || request.getKeyword().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        int limit = request.getLimit() != null ? request.getLimit() : 10;
        List<RagVector> results = ragVectorService.hybridSearch(
            request.getQuery(),
            request.getKeyword(),
            limit
        );

        List<SemanticSearchResponse> response = results.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        log.info("Found {} results for hybrid search", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * RagVector를 Response DTO로 변환
     */
    private SemanticSearchResponse toResponse(RagVector vector) {
        return SemanticSearchResponse.builder()
            .id(vector.getId())
            .sourceType(vector.getSourceType())
            .sourceId(vector.getSourceId())
            .textChunk(vector.getTextChunk())
            .metadata(vector.getMetadata())
            .createdAt(vector.getCreatedAt())
            .build();
    }
}
