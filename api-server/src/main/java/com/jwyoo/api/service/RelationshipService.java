package com.jwyoo.api.service;

import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public List<Relationship> getAllRelationships() {
        return relationshipRepository.findAll();
    }

    public Relationship getRelationshipById(Long id) {
        return relationshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relationship not found with id: " + id));
    }

    public List<Relationship> getRelationshipsByFromCharacterId(Long characterId) {
        return relationshipRepository.findByFromCharacterId(characterId);
    }

    public List<Relationship> getRelationshipsByToCharacterId(Long characterId) {
        return relationshipRepository.findByToCharacterId(characterId);
    }

    public List<Relationship> getAllRelationshipsForCharacter(Long characterId) {
        List<Relationship> fromRelationships = relationshipRepository.findByFromCharacterId(characterId);
        List<Relationship> toRelationships = relationshipRepository.findByToCharacterId(characterId);
        fromRelationships.addAll(toRelationships);
        return fromRelationships;
    }

    @Transactional
    public Relationship createRelationship(Relationship relationship) {
        return relationshipRepository.save(relationship);
    }

    @Transactional
    public Relationship updateRelationship(Long id, Relationship updatedRelationship) {
        Relationship relationship = getRelationshipById(id);

        relationship.setRelationType(updatedRelationship.getRelationType());
        relationship.setCloseness(updatedRelationship.getCloseness());
        relationship.setDescription(updatedRelationship.getDescription());

        return relationshipRepository.save(relationship);
    }

    @Transactional
    public void deleteRelationship(Long id) {
        if (!relationshipRepository.existsById(id)) {
            throw new RuntimeException("Relationship not found with id: " + id);
        }
        relationshipRepository.deleteById(id);
    }
}