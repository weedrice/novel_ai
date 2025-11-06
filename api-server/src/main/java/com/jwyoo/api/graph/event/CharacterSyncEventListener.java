package com.jwyoo.api.graph.event;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.graph.service.GraphSyncService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Character 엔티티 이벤트 리스너
 * Character 생성/수정/삭제 시 Neo4j 동기화
 */
@Slf4j
@Component
public class CharacterSyncEventListener {

    @Lazy
    @Autowired
    private GraphSyncService graphSyncService;

    /**
     * 캐릭터 생성 시 Neo4j 동기화
     */
    @PostPersist
    public void onCharacterCreated(Character character) {
        try {
            log.info("Character created event: id={}, name={}", character.getId(), character.getName());
            graphSyncService.syncCharacter(character);
        } catch (Exception e) {
            log.error("Failed to sync character creation to Neo4j: {}", e.getMessage());
            // 동기화 실패는 전체 트랜잭션을 롤백하지 않음
        }
    }

    /**
     * 캐릭터 업데이트 시 Neo4j 동기화
     */
    @PostUpdate
    public void onCharacterUpdated(Character character) {
        try {
            log.info("Character updated event: id={}, name={}", character.getId(), character.getName());
            graphSyncService.syncCharacter(character);
        } catch (Exception e) {
            log.error("Failed to sync character update to Neo4j: {}", e.getMessage());
        }
    }

    /**
     * 캐릭터 삭제 시 Neo4j에서도 삭제
     */
    @PostRemove
    public void onCharacterDeleted(Character character) {
        try {
            log.info("Character deleted event: id={}, name={}", character.getId(), character.getName());
            graphSyncService.deleteCharacterNode(character.getId());
        } catch (Exception e) {
            log.error("Failed to delete character from Neo4j: {}", e.getMessage());
        }
    }
}
