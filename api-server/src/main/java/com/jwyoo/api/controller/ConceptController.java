package com.jwyoo.api.controller;

import com.jwyoo.api.dto.ConceptRequest;
import com.jwyoo.api.dto.ConceptResponse;
import com.jwyoo.api.dto.SemanticSearchRequest;
import com.jwyoo.api.dto.SemanticSearchResponse;
import com.jwyoo.api.entity.Concept;
import com.jwyoo.api.entity.RagVector;
import com.jwyoo.api.graph.node.ConceptNode;
import com.jwyoo.api.graph.repository.ConceptNodeRepository;
import com.jwyoo.api.service.ConceptService;
import com.jwyoo.api.service.RagVectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Concept 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/concepts")
@RequiredArgsConstructor
public class ConceptController {

    private final ConceptService conceptService;
    private final ConceptNodeRepository conceptNodeRepository;
    private final RagVectorService ragVectorService;

    /**
     * 개념 생성
     * POST /concepts
     */
    @PostMapping
    public ResponseEntity<ConceptResponse> createConcept(@Valid @RequestBody ConceptRequest request) {
        log.info("POST /concepts - Creating concept: name={}, type={}", request.getName(), request.getType());

        Concept concept = conceptService.createConceptWithRelations(
            request.getName(),
            request.getType(),
            request.getDescription(),
            request.getProjectId(),
            request.getEpisodeId(),
            request.getImportance(),
            request.getSource() != null ? request.getSource() : "manual"
        );

        ConceptResponse response = ConceptResponse.from(concept);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 개념 목록 조회 (프로젝트별)
     * GET /concepts?projectId={projectId}&type={type}
     */
    @GetMapping
    public ResponseEntity<List<ConceptResponse>> getConcepts(
            @RequestParam Long projectId,
            @RequestParam(required = false) String type) {
        log.info("GET /concepts - projectId={}, type={}", projectId, type);

        List<Concept> concepts;
        if (type != null) {
            concepts = conceptService.getConceptsByProjectIdAndType(projectId, type);
        } else {
            concepts = conceptService.getConceptsByProjectId(projectId);
        }

        List<ConceptResponse> responses = concepts.stream()
            .map(ConceptResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 개념 조회 (ID)
     * GET /concepts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConceptResponse> getConceptById(@PathVariable Long id) {
        log.info("GET /concepts/{}", id);

        Concept concept = conceptService.getConceptById(id);
        ConceptResponse response = ConceptResponse.from(concept);

        return ResponseEntity.ok(response);
    }

    /**
     * 개념 업데이트
     * PUT /concepts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConceptResponse> updateConcept(
            @PathVariable Long id,
            @Valid @RequestBody ConceptRequest request) {
        log.info("PUT /concepts/{} - Updating concept", id);

        Concept updatedConcept = new Concept();
        updatedConcept.setName(request.getName());
        updatedConcept.setType(request.getType());
        updatedConcept.setDescription(request.getDescription());
        updatedConcept.setImportance(request.getImportance());

        Concept concept = conceptService.updateConcept(id, updatedConcept);
        ConceptResponse response = ConceptResponse.from(concept);

        return ResponseEntity.ok(response);
    }

    /**
     * 개념 삭제
     * DELETE /concepts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcept(@PathVariable Long id) {
        log.info("DELETE /concepts/{}", id);

        conceptService.deleteConcept(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * 유사한 개념 찾기 (GraphDB)
     * GET /concepts/{id}/similar?minSimilarity={minSimilarity}&limit={limit}
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ConceptResponse>> getSimilarConcepts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0.5") Double minSimilarity,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("GET /concepts/{}/similar - minSimilarity={}, limit={}", id, minSimilarity, limit);

        // RDB ID를 Neo4j ID로 변환
        Concept concept = conceptService.getConceptById(id);
        ConceptNode conceptNode = conceptNodeRepository.findByRdbId(concept.getId())
            .orElseThrow(() -> new IllegalArgumentException("ConceptNode not found for rdbId: " + concept.getId()));

        // Neo4j에서 유사한 개념 찾기
        List<ConceptNode> similarNodes = conceptNodeRepository.findSimilarConcepts(
            conceptNode.getId(), minSimilarity, limit
        );

        // Neo4j ID → RDB Concept 변환
        List<ConceptResponse> responses = similarNodes.stream()
            .map(node -> {
                Concept c = conceptService.getConceptById(node.getRdbId());
                return ConceptResponse.from(c);
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 개념 의미 검색 (VectorDB)
     * POST /concepts/search
     */
    @PostMapping("/search")
    public ResponseEntity<List<SemanticSearchResponse>> searchConcepts(
            @Valid @RequestBody SemanticSearchRequest request) {
        log.info("POST /concepts/search - query={}, limit={}",
            request.getQuery(), request.getLimit());

        int limit = request.getLimit() != null ? request.getLimit() : 10;

        // 의미 검색 (RagVectorService 사용)
        List<RagVector> ragVectors = ragVectorService.searchSimilarByType(
            request.getQuery(), "concept", limit
        );

        List<SemanticSearchResponse> responses = ragVectors.stream()
            .map(ragVector -> SemanticSearchResponse.builder()
                .id(ragVector.getId())
                .sourceType(ragVector.getSourceType())
                .sourceId(ragVector.getSourceId())
                .textChunk(ragVector.getTextChunk())
                .metadata(ragVector.getMetadata())
                .createdAt(ragVector.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 하이브리드 검색 (GraphDB + VectorDB)
     * POST /concepts/hybrid-search
     *
     * GraphDB에서 관계된 개념을 찾고, VectorDB에서 의미적으로 유사한 개념을 찾아 병합
     */
    @PostMapping("/hybrid-search")
    public ResponseEntity<Map<String, Object>> hybridSearch(
            @RequestParam Long conceptId,
            @RequestParam(defaultValue = "0.5") Double minSimilarity,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("POST /concepts/hybrid-search - conceptId={}, minSimilarity={}, limit={}",
            conceptId, minSimilarity, limit);

        Concept concept = conceptService.getConceptById(conceptId);

        Map<String, Object> result = new HashMap<>();
        result.put("concept", ConceptResponse.from(concept));

        // 1. GraphDB에서 관계된 개념 찾기
        ConceptNode conceptNode = conceptNodeRepository.findByRdbId(concept.getId())
            .orElseThrow(() -> new IllegalArgumentException("ConceptNode not found"));

        List<ConceptNode> graphResults = conceptNodeRepository.findSimilarConcepts(
            conceptNode.getId(), minSimilarity, limit
        );

        List<ConceptResponse> graphResponses = graphResults.stream()
            .map(node -> {
                Concept c = conceptService.getConceptById(node.getRdbId());
                return ConceptResponse.from(c);
            })
            .collect(Collectors.toList());

        result.put("graph_related", graphResponses);

        // 2. VectorDB에서 의미적으로 유사한 개념 찾기
        String searchQuery = concept.getName() + ": " + concept.getDescription();
        List<RagVector> vectorResults = ragVectorService.searchSimilarByType(
            searchQuery, "concept", limit
        );

        List<Map<String, Object>> vectorResponses = vectorResults.stream()
            .filter(ragVector -> !ragVector.getSourceId().equals(conceptId)) // 자기 자신 제외
            .map(ragVector -> {
                Map<String, Object> item = new HashMap<>();
                item.put("conceptId", ragVector.getSourceId());
                item.put("textChunk", ragVector.getTextChunk());
                return item;
            })
            .collect(Collectors.toList());

        result.put("vector_similar", vectorResponses);

        return ResponseEntity.ok(result);
    }

    /**
     * 중요한 개념 상위 N개 조회
     * GET /concepts/top?projectId={projectId}&limit={limit}
     */
    @GetMapping("/top")
    public ResponseEntity<List<ConceptResponse>> getTopConcepts(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("GET /concepts/top - projectId={}, limit={}", projectId, limit);

        List<ConceptNode> topNodes = conceptNodeRepository.findTopConceptsByImportance(projectId, limit);

        List<ConceptResponse> responses = topNodes.stream()
            .map(node -> {
                Concept c = conceptService.getConceptById(node.getRdbId());
                return ConceptResponse.from(c);
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
