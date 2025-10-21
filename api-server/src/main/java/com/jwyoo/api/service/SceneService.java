package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.DialogueRepository;
import com.jwyoo.api.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 장면 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SceneService {

    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;
    private final DialogueRepository dialogueRepository;

    /**
     * 모든 장면 조회
     */
    public List<Scene> getAllScenes() {
        log.debug("Fetching all scenes");
        return sceneRepository.findAll();
    }

    /**
     * ID로 장면 조회
     */
    public Scene getSceneById(Long id) {
        log.debug("Fetching scene by id: {}", id);
        return sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("장면", id));
    }

    /**
     * 에피소드별 장면 목록 조회
     */
    public List<Scene> getScenesByEpisodeId(Long episodeId) {
        log.debug("Fetching scenes by episode id: {}", episodeId);
        return sceneRepository.findByEpisodeIdOrderBySceneNumberAsc(episodeId);
    }

    /**
     * 장면에 참여하는 캐릭터 목록 조회
     */
    public List<Character> getParticipants(Scene scene) {
        if (scene.getParticipants() == null || scene.getParticipants().trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] characterIds = scene.getParticipants().split(",");
        log.debug("Fetching participants for scene {}: {}", scene.getId(), Arrays.toString(characterIds));

        return Arrays.stream(characterIds)
                .map(String::trim)
                .map(characterId -> characterRepository.findByCharacterId(characterId).orElse(null))
                .filter(character -> character != null)
                .collect(Collectors.toList());
    }

    /**
     * 장면 생성
     */
    @Transactional
    public Scene createScene(Scene scene) {
        log.info("Creating new scene for episode: {}", scene.getEpisode().getId());
        Scene saved = sceneRepository.save(scene);
        log.info("Scene created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * 장면 수정
     */
    @Transactional
    public Scene updateScene(Long id, Scene scene) {
        log.info("Updating scene: {}", id);
        Scene existing = getSceneById(id);

        existing.setSceneNumber(scene.getSceneNumber());
        existing.setLocation(scene.getLocation());
        existing.setMood(scene.getMood());
        existing.setDescription(scene.getDescription());
        existing.setParticipants(scene.getParticipants());

        Scene updated = sceneRepository.save(existing);
        log.info("Scene updated successfully: {}", id);
        return updated;
    }

    /**
     * 장면 삭제
     */
    @Transactional
    public void deleteScene(Long id) {
        log.info("Deleting scene: {}", id);

        if (!sceneRepository.existsById(id)) {
            throw new ResourceNotFoundException("장면", id);
        }

        sceneRepository.deleteById(id);
        log.info("Scene deleted successfully: {}", id);
    }

    /**
     * 장면에 대사 추가
     */
    @Transactional
    public Scene addDialogue(Long sceneId, Dialogue dialogue) {
        log.info("Adding dialogue to scene: {}", sceneId);
        Scene scene = getSceneById(sceneId);

        dialogue.setScene(scene);
        dialogue.setDialogueOrder(scene.getDialogues().size() + 1);
        scene.getDialogues().add(dialogue);

        Scene updated = sceneRepository.save(scene);
        log.info("Dialogue added successfully to scene: {}", sceneId);
        return updated;
    }

    /**
     * 장면의 대사 목록 조회
     */
    public List<Dialogue> getDialogues(Long sceneId) {
        log.debug("Fetching dialogues for scene: {}", sceneId);
        // Use DialogueRepository directly to ensure character is fetched
        return dialogueRepository.findBySceneIdOrderByDialogueOrderAsc(sceneId);
    }
}