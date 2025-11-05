package com.jwyoo.api.graph.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.EpisodeRelationship;
import com.jwyoo.api.graph.node.CharacterNode;
import com.jwyoo.api.graph.node.CharacterRelationship;
import com.jwyoo.api.graph.repository.CharacterNodeRepository;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.EpisodeRelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * RDB ↔ Neo4j 동기화 서비스
 * PostgreSQL의 데이터를 Neo4j GraphDB로 동기화합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphSyncService {

    private final CharacterRepository characterRepository;
    private final EpisodeRelationshipRepository episodeRelationshipRepository;
    private final CharacterNodeRepository characterNodeRepository;

    /**
     * 캐릭터를 Neo4j로 동기화
     */
    @Transactional
    public CharacterNode syncCharacter(Character character) {
        log.info("Syncing character to Neo4j: id={}, name={}", character.getId(), character.getName());

        // 기존 노드 확인
        Optional<CharacterNode> existingNode = characterNodeRepository.findByRdbId(character.getId());

        CharacterNode node;
        if (existingNode.isPresent()) {
            // 업데이트
            node = existingNode.get();
            node.setCharacterId(character.getCharacterId());
            node.setName(character.getName());
            node.setDescription(character.getDescription());
            node.setPersonality(character.getPersonality());
            node.setSpeakingStyle(character.getSpeakingStyle());
            log.debug("Updating existing character node: neo4jId={}", node.getId());
        } else {
            // 신규 생성
            node = CharacterNode.builder()
                .rdbId(character.getId())
                .projectId(character.getProject().getId())
                .characterId(character.getCharacterId())
                .name(character.getName())
                .description(character.getDescription())
                .personality(character.getPersonality())
                .speakingStyle(character.getSpeakingStyle())
                .build();
            log.debug("Creating new character node");
        }

        CharacterNode saved = characterNodeRepository.save(node);
        log.info("Character synced to Neo4j: neo4jId={}, rdbId={}", saved.getId(), saved.getRdbId());
        return saved;
    }

    /**
     * 에피소드 관계를 Neo4j로 동기화
     */
    @Transactional
    public void syncEpisodeRelationship(EpisodeRelationship relationship) {
        log.info("Syncing episode relationship to Neo4j: episodeId={}, from={}, to={}",
            relationship.getEpisode().getId(),
            relationship.getFromCharacter().getId(),
            relationship.getToCharacter().getId());

        try {
            // 캐릭터 노드 먼저 동기화
            CharacterNode fromNode = syncCharacter(relationship.getFromCharacter());
            CharacterNode toNode = syncCharacter(relationship.getToCharacter());

            // 관계 생성
            CharacterRelationship graphRelationship = CharacterRelationship.builder()
                .episodeId(relationship.getEpisode().getId())
                .relationType(relationship.getRelationType())
                .closeness(relationship.getCloseness())
                .description(relationship.getDescription())
                .targetCharacter(toNode)
                .build();

            // 기존 관계 확인 후 추가
            fromNode.getRelationships().removeIf(r ->
                r.getTargetCharacter().getRdbId().equals(toNode.getRdbId()) &&
                r.getEpisodeId().equals(relationship.getEpisode().getId())
            );
            fromNode.getRelationships().add(graphRelationship);

            characterNodeRepository.save(fromNode);
            log.info("Episode relationship synced to Neo4j successfully");

        } catch (Exception e) {
            log.error("Failed to sync episode relationship to Neo4j: error={}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync relationship to Neo4j", e);
        }
    }

    /**
     * 캐릭터 삭제 시 Neo4j에서도 삭제
     */
    @Transactional
    public void deleteCharacterNode(Long rdbId) {
        log.info("Deleting character node from Neo4j: rdbId={}", rdbId);

        Optional<CharacterNode> node = characterNodeRepository.findByRdbId(rdbId);
        if (node.isPresent()) {
            characterNodeRepository.delete(node.get());
            log.info("Character node deleted from Neo4j: neo4jId={}", node.get().getId());
        } else {
            log.warn("Character node not found in Neo4j: rdbId={}", rdbId);
        }
    }

    /**
     * 에피소드 관계 삭제 시 Neo4j에서도 삭제
     */
    @Transactional
    public void deleteEpisodeRelationshipNode(Long episodeId, Long fromCharacterId, Long toCharacterId) {
        log.info("Deleting episode relationship from Neo4j: episodeId={}, from={}, to={}",
            episodeId, fromCharacterId, toCharacterId);

        try {
            Optional<CharacterNode> fromNodeOpt = characterNodeRepository.findByRdbId(fromCharacterId);
            if (fromNodeOpt.isEmpty()) {
                log.warn("From character node not found in Neo4j: rdbId={}", fromCharacterId);
                return;
            }

            CharacterNode fromNode = fromNodeOpt.get();
            boolean removed = fromNode.getRelationships().removeIf(r ->
                r.getEpisodeId().equals(episodeId) &&
                r.getTargetCharacter().getRdbId().equals(toCharacterId)
            );

            if (removed) {
                characterNodeRepository.save(fromNode);
                log.info("Episode relationship deleted from Neo4j successfully");
            } else {
                log.warn("Relationship not found in Neo4j");
            }

        } catch (Exception e) {
            log.error("Failed to delete relationship from Neo4j: error={}", e.getMessage(), e);
        }
    }

    /**
     * 기존 데이터 일괄 마이그레이션
     */
    @Transactional
    public void migrateAllData() {
        log.info("Starting bulk migration from RDB to Neo4j");

        try {
            // 1. 모든 캐릭터 동기화
            List<Character> characters = characterRepository.findAll();
            log.info("Migrating {} characters", characters.size());
            for (Character character : characters) {
                syncCharacter(character);
            }

            // 2. 모든 에피소드 관계 동기화
            List<EpisodeRelationship> relationships = episodeRelationshipRepository.findAll();
            log.info("Migrating {} episode relationships", relationships.size());
            for (EpisodeRelationship relationship : relationships) {
                syncEpisodeRelationship(relationship);
            }

            log.info("Bulk migration completed successfully");

        } catch (Exception e) {
            log.error("Bulk migration failed: error={}", e.getMessage(), e);
            throw new RuntimeException("Bulk migration failed", e);
        }
    }

    /**
     * 특정 프로젝트 데이터 마이그레이션
     */
    @Transactional
    public void migrateProjectData(Long projectId) {
        log.info("Starting project data migration to Neo4j: projectId={}", projectId);

        try {
            // 프로젝트의 모든 캐릭터 동기화
            List<Character> characters = characterRepository.findAll().stream()
                .filter(c -> c.getProject().getId().equals(projectId))
                .toList();

            log.info("Migrating {} characters for project {}", characters.size(), projectId);
            for (Character character : characters) {
                syncCharacter(character);
            }

            // 프로젝트의 모든 관계 동기화
            List<EpisodeRelationship> relationships = episodeRelationshipRepository.findAll().stream()
                .filter(r -> r.getEpisode().getProject().getId().equals(projectId))
                .toList();

            log.info("Migrating {} relationships for project {}", relationships.size(), projectId);
            for (EpisodeRelationship relationship : relationships) {
                syncEpisodeRelationship(relationship);
            }

            log.info("Project data migration completed: projectId={}", projectId);

        } catch (Exception e) {
            log.error("Project data migration failed: projectId={}, error={}", projectId, e.getMessage(), e);
            throw new RuntimeException("Project data migration failed", e);
        }
    }
}
