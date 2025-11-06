package com.jwyoo.api.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.service.RagVectorService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA 엔티티 생명주기 이벤트를 감지하여 Vector DB 동기화
 *
 * @PostPersist: 엔티티 생성 후 임베딩 생성
 * @PostUpdate: 엔티티 업데이트 후 임베딩 갱신
 * @PostRemove: 엔티티 삭제 후 임베딩 삭제
 */
@Slf4j
@Component
public class EmbeddingSyncEventListener {

    @Lazy
    @Autowired
    private RagVectorService ragVectorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 대사 생성 시 임베딩 생성
     */
    @PostPersist
    public void onDialogueCreated(Dialogue dialogue) {
        try {
            log.info("Dialogue created event: id={}, text={}", dialogue.getId(),
                dialogue.getText().substring(0, Math.min(50, dialogue.getText().length())));

            String metadata = buildDialogueMetadata(dialogue);
            ragVectorService.saveEmbedding("dialogue", dialogue.getId(), dialogue.getText(), metadata);
        } catch (Exception e) {
            log.error("Failed to create embedding for dialogue: {}", e.getMessage(), e);
            // 임베딩 생성 실패는 전체 트랜잭션을 롤백하지 않음
        }
    }

    /**
     * 대사 업데이트 시 임베딩 갱신
     */
    @PostUpdate
    public void onDialogueUpdated(Dialogue dialogue) {
        try {
            log.info("Dialogue updated event: id={}, text={}", dialogue.getId(),
                dialogue.getText().substring(0, Math.min(50, dialogue.getText().length())));

            String metadata = buildDialogueMetadata(dialogue);
            ragVectorService.saveEmbedding("dialogue", dialogue.getId(), dialogue.getText(), metadata);
        } catch (Exception e) {
            log.error("Failed to update embedding for dialogue: {}", e.getMessage(), e);
        }
    }

    /**
     * 대사 삭제 시 임베딩 삭제
     */
    @PostRemove
    public void onDialogueDeleted(Dialogue dialogue) {
        try {
            log.info("Dialogue deleted event: id={}", dialogue.getId());
            ragVectorService.deleteEmbedding("dialogue", dialogue.getId());
        } catch (Exception e) {
            log.error("Failed to delete embedding for dialogue: {}", e.getMessage(), e);
        }
    }

    /**
     * 대사 메타데이터 빌드 (JSON 형식)
     */
    private String buildDialogueMetadata(Dialogue dialogue) {
        try {
            Map<String, Object> metadata = new HashMap<>();

            if (dialogue.getCharacter() != null) {
                metadata.put("characterId", dialogue.getCharacter().getId());
                metadata.put("characterName", dialogue.getCharacter().getName());
            }

            if (dialogue.getScene() != null) {
                metadata.put("sceneId", dialogue.getScene().getId());
                metadata.put("sceneNumber", dialogue.getScene().getSceneNumber());
                if (dialogue.getScene().getLocation() != null) {
                    metadata.put("sceneLocation", dialogue.getScene().getLocation());
                }

                if (dialogue.getScene().getEpisode() != null) {
                    metadata.put("episodeId", dialogue.getScene().getEpisode().getId());
                    metadata.put("episodeTitle", dialogue.getScene().getEpisode().getTitle());
                }
            }

            if (dialogue.getEmotion() != null) {
                metadata.put("emotion", dialogue.getEmotion());
            }

            if (dialogue.getIntent() != null) {
                metadata.put("intent", dialogue.getIntent());
            }

            if (dialogue.getHonorific() != null) {
                metadata.put("honorific", dialogue.getHonorific());
            }

            metadata.put("dialogueOrder", dialogue.getDialogueOrder());

            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Failed to build metadata for dialogue: {}", e.getMessage());
            return "{}";
        }
    }
}
