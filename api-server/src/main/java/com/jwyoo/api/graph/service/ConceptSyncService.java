package com.jwyoo.api.graph.service;

import com.jwyoo.api.entity.Concept;
import com.jwyoo.api.graph.node.ConceptNode;
import com.jwyoo.api.graph.repository.ConceptNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Concept 동기화 서비스 (RDB → Neo4j)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConceptSyncService {

    private final ConceptNodeRepository conceptNodeRepository;

    /**
     * Concept를 Neo4j로 동기화
     *
     * @param concept RDB Concept 엔티티
     */
    @Transactional
    public void syncConcept(Concept concept) {
        try {
            log.info("Syncing concept to Neo4j: id={}, name={}", concept.getId(), concept.getName());

            Optional<ConceptNode> existingNode = conceptNodeRepository.findByRdbId(concept.getId());

            ConceptNode conceptNode;
            if (existingNode.isPresent()) {
                // 기존 노드 업데이트
                conceptNode = existingNode.get();
                conceptNode.setName(concept.getName());
                conceptNode.setType(concept.getType());
                conceptNode.setDescription(concept.getDescription());
                conceptNode.setImportance(concept.getImportance());
                conceptNode.setEpisodeId(concept.getEpisode() != null ? concept.getEpisode().getId() : null);
                log.info("Updating existing ConceptNode: rdbId={}", concept.getId());
            } else {
                // 새 노드 생성
                conceptNode = ConceptNode.builder()
                    .rdbId(concept.getId())
                    .projectId(concept.getProject().getId())
                    .name(concept.getName())
                    .type(concept.getType())
                    .description(concept.getDescription())
                    .importance(concept.getImportance())
                    .episodeId(concept.getEpisode() != null ? concept.getEpisode().getId() : null)
                    .build();
                log.info("Creating new ConceptNode: rdbId={}", concept.getId());
            }

            conceptNodeRepository.save(conceptNode);
            log.info("ConceptNode synced successfully: rdbId={}, neo4jId={}", concept.getId(), conceptNode.getId());

        } catch (Exception e) {
            log.error("Failed to sync concept to Neo4j: id={}", concept.getId(), e);
            // 동기화 실패는 메인 트랜잭션에 영향을 주지 않도록 예외를 던지지 않음
        }
    }

    /**
     * Concept 삭제 시 Neo4j에서도 삭제
     *
     * @param conceptId RDB Concept ID
     */
    @Transactional
    public void deleteConceptNode(Long conceptId) {
        try {
            log.info("Deleting ConceptNode from Neo4j: rdbId={}", conceptId);
            conceptNodeRepository.deleteByRdbId(conceptId);
            log.info("ConceptNode deleted successfully: rdbId={}", conceptId);
        } catch (Exception e) {
            log.error("Failed to delete ConceptNode from Neo4j: rdbId={}", conceptId, e);
        }
    }

    /**
     * 두 개념 간 관계 생성 (Neo4j)
     *
     * @param fromConceptId 시작 개념 ID (Neo4j ID)
     * @param toConceptId 종료 개념 ID (Neo4j ID)
     * @param relationType 관계 유형 (similar, opposite, contains, derived)
     * @param similarity 유사도 (0.0 ~ 1.0)
     */
    @Transactional
    public void createConceptRelationship(Long fromConceptId, Long toConceptId, String relationType, Double similarity) {
        try {
            log.info("Creating concept relationship: from={}, to={}, type={}, similarity={}",
                fromConceptId, toConceptId, relationType, similarity);
            conceptNodeRepository.createRelationship(fromConceptId, toConceptId, relationType, similarity);
            log.info("Concept relationship created successfully");
        } catch (Exception e) {
            log.error("Failed to create concept relationship: from={}, to={}", fromConceptId, toConceptId, e);
        }
    }
}
