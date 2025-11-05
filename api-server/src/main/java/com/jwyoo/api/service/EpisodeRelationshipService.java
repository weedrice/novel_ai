package com.jwyoo.api.service;

import com.jwyoo.api.entity.EpisodeRelationship;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.EpisodeRelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 에피소드별 캐릭터 관계 비즈니스 로직을 처리하는 서비스
 * 에피소드별로 관계 변화를 추적하고 시간에 따른 관계 진행을 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeRelationshipService {

    private final EpisodeRelationshipRepository episodeRelationshipRepository;

    /**
     * 특정 에피소드의 모든 관계 조회
     */
    public List<EpisodeRelationship> getRelationshipsByEpisode(Long episodeId) {
        log.debug("Fetching relationships for episode: {}", episodeId);
        List<EpisodeRelationship> relationships = episodeRelationshipRepository.findByEpisodeId(episodeId);
        log.info("Found {} relationships for episode: {}", relationships.size(), episodeId);
        return relationships;
    }

    /**
     * 특정 에피소드와 프로젝트로 관계 조회
     */
    public List<EpisodeRelationship> getRelationshipsByEpisodeAndProject(Long episodeId, Project project) {
        log.debug("Fetching relationships for episode: {} in project: {}", episodeId, project.getId());
        List<EpisodeRelationship> relationships = episodeRelationshipRepository.findByEpisodeIdAndProject(episodeId, project);
        log.info("Found {} relationships for episode: {} in project: {}", relationships.size(), episodeId, project.getId());
        return relationships;
    }

    /**
     * 프로젝트의 모든 에피소드 관계 조회
     */
    public List<EpisodeRelationship> getRelationshipsByProject(Project project) {
        log.debug("Fetching all episode relationships for project: {}", project.getId());
        List<EpisodeRelationship> relationships = episodeRelationshipRepository.findByProject(project);
        log.info("Found {} episode relationships for project: {}", relationships.size(), project.getId());
        return relationships;
    }

    /**
     * 두 캐릭터 간의 관계 변화 히스토리 조회 (시간순)
     */
    public List<EpisodeRelationship> getRelationshipHistory(Long char1Id, Long char2Id) {
        log.debug("Fetching relationship history between characters: {} and {}", char1Id, char2Id);
        List<EpisodeRelationship> history = episodeRelationshipRepository.findRelationshipHistory(char1Id, char2Id);
        log.info("Found {} historical relationships between characters: {} and {}", history.size(), char1Id, char2Id);
        return history;
    }

    /**
     * 특정 에피소드에서 두 캐릭터 간의 관계 조회
     */
    public Optional<EpisodeRelationship> getRelationshipByEpisodeAndCharacters(Long episodeId, Long char1Id, Long char2Id) {
        log.debug("Fetching relationship in episode: {} between characters: {} and {}", episodeId, char1Id, char2Id);
        Optional<EpisodeRelationship> relationship = episodeRelationshipRepository.findByEpisodeAndCharacters(episodeId, char1Id, char2Id);
        if (relationship.isPresent()) {
            log.debug("Found relationship: id={}, type={}", relationship.get().getId(), relationship.get().getRelationType());
        } else {
            log.debug("No relationship found in episode: {} between characters: {} and {}", episodeId, char1Id, char2Id);
        }
        return relationship;
    }

    /**
     * ID로 관계 조회
     */
    public EpisodeRelationship getRelationshipById(Long id) {
        log.debug("Fetching episode relationship by id: {}", id);
        EpisodeRelationship relationship = episodeRelationshipRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Episode relationship not found with id: {}", id);
                    return new ResourceNotFoundException("EpisodeRelationship", id);
                });
        log.debug("Found episode relationship: id={}, type={}, episode={}",
            relationship.getId(), relationship.getRelationType(), relationship.getEpisode().getId());
        return relationship;
    }

    /**
     * 새로운 에피소드 관계 생성
     */
    @Transactional
    public EpisodeRelationship createRelationship(EpisodeRelationship relationship) {
        log.info("Creating episode relationship: episode={}, type={}, from={}, to={}, closeness={}",
            relationship.getEpisode().getId(),
            relationship.getRelationType(),
            relationship.getFromCharacter().getId(),
            relationship.getToCharacter().getId(),
            relationship.getCloseness());
        EpisodeRelationship saved = episodeRelationshipRepository.save(relationship);
        log.info("Episode relationship created successfully: id={}", saved.getId());
        return saved;
    }

    /**
     * 에피소드 관계 정보 수정
     */
    @Transactional
    public EpisodeRelationship updateRelationship(Long id, EpisodeRelationship updatedRelationship) {
        log.info("Updating episode relationship: id={}, newType={}, newCloseness={}",
            id, updatedRelationship.getRelationType(), updatedRelationship.getCloseness());
        EpisodeRelationship relationship = getRelationshipById(id);

        String oldType = relationship.getRelationType();
        Double oldCloseness = relationship.getCloseness();

        relationship.setRelationType(updatedRelationship.getRelationType());
        relationship.setCloseness(updatedRelationship.getCloseness());
        relationship.setDescription(updatedRelationship.getDescription());

        EpisodeRelationship saved = episodeRelationshipRepository.save(relationship);
        log.info("Episode relationship updated: id={}, type: {} -> {}, closeness: {} -> {}",
            id, oldType, saved.getRelationType(), oldCloseness, saved.getCloseness());
        return saved;
    }

    /**
     * 에피소드 관계 삭제
     */
    @Transactional
    public void deleteRelationship(Long id) {
        log.info("Deleting episode relationship: id={}", id);

        if (!episodeRelationshipRepository.existsById(id)) {
            log.error("Cannot delete - episode relationship not found: id={}", id);
            throw new ResourceNotFoundException("EpisodeRelationship", id);
        }

        episodeRelationshipRepository.deleteById(id);
        log.info("Episode relationship deleted successfully: id={}", id);
    }

    /**
     * 에피소드의 모든 관계 삭제
     */
    @Transactional
    public void deleteAllRelationshipsByEpisode(Long episodeId) {
        log.info("Deleting all relationships for episode: {}", episodeId);
        List<EpisodeRelationship> relationships = episodeRelationshipRepository.findByEpisodeId(episodeId);
        episodeRelationshipRepository.deleteAll(relationships);
        log.info("Deleted {} relationships for episode: {}", relationships.size(), episodeId);
    }
}
