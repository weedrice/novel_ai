package com.jwyoo.api.service;

import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 캐릭터 간 관계 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    /**
     * 모든 관계 조회
     */
    public List<Relationship> getAllRelationships() {
        log.debug("Fetching all relationships");
        List<Relationship> relationships = relationshipRepository.findAll();
        log.info("Fetched {} relationships", relationships.size());
        return relationships;
    }

    /**
     * ID로 관계 조회
     */
    public Relationship getRelationshipById(Long id) {
        log.debug("Fetching relationship by id: {}", id);
        Relationship relationship = relationshipRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Relationship not found with id: {}", id);
                    return new RuntimeException("Relationship not found with id: " + id);
                });
        log.debug("Found relationship: id={}, type={}, from={}, to={}",
            relationship.getId(), relationship.getRelationType(),
            relationship.getFromCharacter().getId(), relationship.getToCharacter().getId());
        return relationship;
    }

    /**
     * 특정 캐릭터가 시작점인 관계 목록 조회
     */
    public List<Relationship> getRelationshipsByFromCharacterId(Long characterId) {
        log.debug("Fetching relationships from character: {}", characterId);
        List<Relationship> relationships = relationshipRepository.findByFromCharacterId(characterId);
        log.info("Found {} relationships from character: {}", relationships.size(), characterId);
        return relationships;
    }

    /**
     * 특정 캐릭터가 대상인 관계 목록 조회
     */
    public List<Relationship> getRelationshipsByToCharacterId(Long characterId) {
        log.debug("Fetching relationships to character: {}", characterId);
        List<Relationship> relationships = relationshipRepository.findByToCharacterId(characterId);
        log.info("Found {} relationships to character: {}", relationships.size(), characterId);
        return relationships;
    }

    /**
     * 특정 캐릭터와 관련된 모든 관계 조회 (시작점 + 대상)
     */
    public List<Relationship> getAllRelationshipsForCharacter(Long characterId) {
        log.debug("Fetching all relationships for character: {}", characterId);
        List<Relationship> fromRelationships = relationshipRepository.findByFromCharacterId(characterId);
        List<Relationship> toRelationships = relationshipRepository.findByToCharacterId(characterId);
        fromRelationships.addAll(toRelationships);
        log.info("Found total {} relationships for character: {} (from: {}, to: {})",
            fromRelationships.size(), characterId,
            fromRelationships.size() - toRelationships.size(), toRelationships.size());
        return fromRelationships;
    }

    /**
     * 새로운 관계 생성
     */
    @Transactional
    public Relationship createRelationship(Relationship relationship) {
        log.info("Creating relationship: type={}, from={}, to={}, closeness={}",
            relationship.getRelationType(),
            relationship.getFromCharacter().getId(),
            relationship.getToCharacter().getId(),
            relationship.getCloseness());
        Relationship saved = relationshipRepository.save(relationship);
        log.info("Relationship created successfully: id={}", saved.getId());
        return saved;
    }

    /**
     * 관계 정보 수정
     */
    @Transactional
    public Relationship updateRelationship(Long id, Relationship updatedRelationship) {
        log.info("Updating relationship: id={}, newType={}, newCloseness={}",
            id, updatedRelationship.getRelationType(), updatedRelationship.getCloseness());
        Relationship relationship = getRelationshipById(id);

        String oldType = relationship.getRelationType();
        Double oldCloseness = relationship.getCloseness();

        relationship.setRelationType(updatedRelationship.getRelationType());
        relationship.setCloseness(updatedRelationship.getCloseness());
        relationship.setDescription(updatedRelationship.getDescription());

        Relationship saved = relationshipRepository.save(relationship);
        log.info("Relationship updated: id={}, type: {} -> {}, closeness: {} -> {}",
            id, oldType, saved.getRelationType(), oldCloseness, saved.getCloseness());
        return saved;
    }

    /**
     * 관계 삭제
     */
    @Transactional
    public void deleteRelationship(Long id) {
        log.info("Deleting relationship: id={}", id);

        if (!relationshipRepository.existsById(id)) {
            log.error("Cannot delete - relationship not found: id={}", id);
            throw new RuntimeException("Relationship not found with id: " + id);
        }

        relationshipRepository.deleteById(id);
        log.info("Relationship deleted successfully: id={}", id);
    }
}