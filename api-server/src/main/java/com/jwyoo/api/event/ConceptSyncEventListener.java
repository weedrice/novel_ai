package com.jwyoo.api.event;

import com.jwyoo.api.entity.Concept;
import com.jwyoo.api.graph.service.ConceptSyncService;
import com.jwyoo.api.service.RagVectorService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Concept 엔티티 이벤트 리스너
 * Concept 생성/수정/삭제 시 Neo4j와 VectorDB로 자동 동기화
 */
@Slf4j
@Component
public class ConceptSyncEventListener {

    private ConceptSyncService conceptSyncService;
    private RagVectorService ragVectorService;

    /**
     * @Lazy 주입으로 순환 의존성 방지
     */
    @Autowired
    public void setConceptSyncService(@Lazy ConceptSyncService conceptSyncService) {
        this.conceptSyncService = conceptSyncService;
    }

    @Autowired
    public void setRagVectorService(@Lazy RagVectorService ragVectorService) {
        this.ragVectorService = ragVectorService;
    }

    /**
     * Concept 생성 시 Neo4j 동기화 및 임베딩 생성
     */
    @PostPersist
    public void onPostPersist(Concept concept) {
        log.info("@PostPersist triggered for Concept: id={}, name={}", concept.getId(), concept.getName());

        // 1. Neo4j 동기화
        if (conceptSyncService != null) {
            conceptSyncService.syncConcept(concept);
        }

        // 2. 임베딩 생성 (Concept 설명 기반)
        if (ragVectorService != null && concept.getDescription() != null && !concept.getDescription().isBlank()) {
            try {
                String embeddingText = concept.getName() + ": " + concept.getDescription();
                ragVectorService.saveEmbedding("concept", concept.getId(), embeddingText, null);
                log.info("Embedding generated for concept: id={}", concept.getId());
            } catch (Exception e) {
                log.error("Failed to generate embedding for concept: id={}", concept.getId(), e);
            }
        }
    }

    /**
     * Concept 수정 시 Neo4j 동기화 및 임베딩 재생성
     */
    @PostUpdate
    public void onPostUpdate(Concept concept) {
        log.info("@PostUpdate triggered for Concept: id={}, name={}", concept.getId(), concept.getName());

        // 1. Neo4j 동기화
        if (conceptSyncService != null) {
            conceptSyncService.syncConcept(concept);
        }

        // 2. 임베딩 재생성
        if (ragVectorService != null && concept.getDescription() != null && !concept.getDescription().isBlank()) {
            try {
                // 기존 임베딩 삭제
                ragVectorService.deleteEmbedding("concept", concept.getId());

                // 새 임베딩 생성
                String embeddingText = concept.getName() + ": " + concept.getDescription();
                ragVectorService.saveEmbedding("concept", concept.getId(), embeddingText, null);
                log.info("Embedding regenerated for concept: id={}", concept.getId());
            } catch (Exception e) {
                log.error("Failed to regenerate embedding for concept: id={}", concept.getId(), e);
            }
        }
    }

    /**
     * Concept 삭제 시 Neo4j 및 임베딩 삭제
     */
    @PostRemove
    public void onPostRemove(Concept concept) {
        log.info("@PostRemove triggered for Concept: id={}, name={}", concept.getId(), concept.getName());

        // 1. Neo4j 삭제
        if (conceptSyncService != null) {
            conceptSyncService.deleteConceptNode(concept.getId());
        }

        // 2. 임베딩 삭제
        if (ragVectorService != null) {
            try {
                ragVectorService.deleteEmbedding("concept", concept.getId());
                log.info("Embedding deleted for concept: id={}", concept.getId());
            } catch (Exception e) {
                log.error("Failed to delete embedding for concept: id={}", concept.getId(), e);
            }
        }
    }
}
