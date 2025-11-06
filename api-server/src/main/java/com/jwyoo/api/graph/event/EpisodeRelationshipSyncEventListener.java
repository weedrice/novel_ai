package com.jwyoo.api.graph.event;

import com.jwyoo.api.entity.EpisodeRelationship;
import com.jwyoo.api.graph.service.GraphSyncService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * EpisodeRelationship 엔티티 이벤트 리스너
 * EpisodeRelationship 생성/수정/삭제 시 Neo4j 동기화
 */
@Slf4j
@Component
public class EpisodeRelationshipSyncEventListener {

    @Lazy
    @Autowired
    private GraphSyncService graphSyncService;

    /**
     * 에피소드 관계 생성 시 Neo4j 동기화
     */
    @PostPersist
    public void onEpisodeRelationshipCreated(EpisodeRelationship relationship) {
        try {
            log.info("EpisodeRelationship created event: episodeId={}, from={}, to={}",
                relationship.getEpisode().getId(),
                relationship.getFromCharacter().getId(),
                relationship.getToCharacter().getId());
            graphSyncService.syncEpisodeRelationship(relationship);
        } catch (Exception e) {
            log.error("Failed to sync relationship creation to Neo4j: {}", e.getMessage());
        }
    }

    /**
     * 에피소드 관계 업데이트 시 Neo4j 동기화
     */
    @PostUpdate
    public void onEpisodeRelationshipUpdated(EpisodeRelationship relationship) {
        try {
            log.info("EpisodeRelationship updated event: episodeId={}, from={}, to={}",
                relationship.getEpisode().getId(),
                relationship.getFromCharacter().getId(),
                relationship.getToCharacter().getId());
            graphSyncService.syncEpisodeRelationship(relationship);
        } catch (Exception e) {
            log.error("Failed to sync relationship update to Neo4j: {}", e.getMessage());
        }
    }

    /**
     * 에피소드 관계 삭제 시 Neo4j에서도 삭제
     */
    @PostRemove
    public void onEpisodeRelationshipDeleted(EpisodeRelationship relationship) {
        try {
            log.info("EpisodeRelationship deleted event: episodeId={}, from={}, to={}",
                relationship.getEpisode().getId(),
                relationship.getFromCharacter().getId(),
                relationship.getToCharacter().getId());
            graphSyncService.deleteEpisodeRelationshipNode(
                relationship.getEpisode().getId(),
                relationship.getFromCharacter().getId(),
                relationship.getToCharacter().getId()
            );
        } catch (Exception e) {
            log.error("Failed to delete relationship from Neo4j: {}", e.getMessage());
        }
    }
}
