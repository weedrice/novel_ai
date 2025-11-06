package com.jwyoo.api.service;

import com.jwyoo.api.entity.Concept;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.repository.ConceptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConceptService {

    private final ConceptRepository conceptRepository;

    /**
     * 개념 생성
     */
    @Transactional
    public Concept createConcept(Concept concept) {
        log.info("Creating concept: name={}, type={}, projectId={}",
            concept.getName(), concept.getType(), concept.getProject().getId());
        return conceptRepository.save(concept);
    }

    /**
     * 개념 조회 (ID)
     */
    @Transactional(readOnly = true)
    public Concept getConceptById(Long id) {
        log.info("Fetching concept: id={}", id);
        return conceptRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Concept not found: id=" + id));
    }

    /**
     * 프로젝트별 개념 조회
     */
    @Transactional(readOnly = true)
    public List<Concept> getConceptsByProjectId(Long projectId) {
        log.info("Fetching concepts by projectId: {}", projectId);
        return conceptRepository.findByProjectId(projectId);
    }

    /**
     * 프로젝트별 + 유형별 개념 조회
     */
    @Transactional(readOnly = true)
    public List<Concept> getConceptsByProjectIdAndType(Long projectId, String type) {
        log.info("Fetching concepts by projectId: {}, type: {}", projectId, type);
        return conceptRepository.findByProjectIdAndType(projectId, type);
    }

    /**
     * 에피소드별 개념 조회
     */
    @Transactional(readOnly = true)
    public List<Concept> getConceptsByEpisodeId(Long episodeId) {
        log.info("Fetching concepts by episodeId: {}", episodeId);
        return conceptRepository.findByEpisodeId(episodeId);
    }

    /**
     * 개념 업데이트
     */
    @Transactional
    public Concept updateConcept(Long id, Concept updatedConcept) {
        log.info("Updating concept: id={}", id);
        Concept concept = getConceptById(id);

        concept.setName(updatedConcept.getName());
        concept.setType(updatedConcept.getType());
        concept.setDescription(updatedConcept.getDescription());
        concept.setImportance(updatedConcept.getImportance());

        if (updatedConcept.getEpisode() != null) {
            concept.setEpisode(updatedConcept.getEpisode());
        }

        return conceptRepository.save(concept);
    }

    /**
     * 개념 삭제
     */
    @Transactional
    public void deleteConcept(Long id) {
        log.info("Deleting concept: id={}", id);
        conceptRepository.deleteById(id);
    }

    /**
     * 중요한 개념 상위 N개 조회
     */
    @Transactional(readOnly = true)
    public List<Concept> getTopConceptsByImportance(Long projectId) {
        log.info("Fetching top concepts by importance: projectId={}", projectId);
        return conceptRepository.findTopConceptsByImportance(projectId);
    }

    /**
     * 개념 검색 (이름 또는 설명)
     */
    @Transactional(readOnly = true)
    public List<Concept> searchConcepts(Long projectId, String keyword) {
        log.info("Searching concepts: projectId={}, keyword={}", projectId, keyword);
        List<Concept> byName = conceptRepository.searchByName(projectId, keyword);
        List<Concept> byDescription = conceptRepository.searchByDescription(projectId, keyword);

        // 중복 제거 및 병합
        byName.addAll(byDescription);
        return byName.stream().distinct().toList();
    }

    /**
     * 프로젝트와 에피소드를 연결하여 개념 생성 (헬퍼 메서드)
     */
    @Transactional
    public Concept createConceptWithRelations(String name, String type, String description,
                                               Long projectId, Long episodeId, Double importance, String source) {
        Project project = new Project();
        project.setId(projectId);

        Episode episode = null;
        if (episodeId != null) {
            episode = new Episode();
            episode.setId(episodeId);
        }

        Concept concept = Concept.builder()
            .name(name)
            .type(type)
            .description(description)
            .project(project)
            .episode(episode)
            .importance(importance)
            .source(source)
            .build();

        return createConcept(concept);
    }
}
