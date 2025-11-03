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
        List<Scene> scenes = sceneRepository.findAll();
        log.info("Fetched {} scenes", scenes.size());
        return scenes;
    }

    /**
     * ID로 장면 조회
     */
    public Scene getSceneById(Long id) {
        log.debug("Fetching scene by id: {}", id);
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Scene not found with id: {}", id);
                    return new ResourceNotFoundException("장면", id);
                });
        log.debug("Found scene: id={}, sceneNumber={}, location={}",
            scene.getId(), scene.getSceneNumber(), scene.getLocation());
        return scene;
    }

    /**
     * 에피소드별 장면 목록 조회
     */
    public List<Scene> getScenesByEpisodeId(Long episodeId) {
        log.debug("Fetching scenes by episode id: {}", episodeId);
        List<Scene> scenes = sceneRepository.findByEpisodeIdOrderBySceneNumberAsc(episodeId);
        log.info("Found {} scenes for episode: {}", scenes.size(), episodeId);
        return scenes;
    }

    /**
     * 장면에 참여하는 캐릭터 목록 조회
     * N+1 문제 해결: IN 쿼리로 한 번에 조회
     */
    public List<Character> getParticipants(Scene scene) {
        if (scene.getParticipants() == null || scene.getParticipants().trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> characterIds = Arrays.stream(scene.getParticipants().split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());

        if (characterIds.isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("Fetching participants for scene {} using IN query: {}", scene.getId(), characterIds);

        // N+1 문제 해결: 한 번의 IN 쿼리로 모든 캐릭터 조회
        return characterRepository.findByCharacterIdIn(characterIds);
    }

    /**
     * 장면 생성
     */
    @Transactional
    public Scene createScene(Scene scene) {
        log.info("Creating new scene: episodeId={}, sceneNumber={}, location={}",
            scene.getEpisode().getId(), scene.getSceneNumber(), scene.getLocation());
        Scene saved = sceneRepository.save(scene);
        log.info("Scene created successfully: id={}, sceneNumber={}", saved.getId(), saved.getSceneNumber());
        return saved;
    }

    /**
     * 장면 수정
     */
    @Transactional
    public Scene updateScene(Long id, Scene scene) {
        log.info("Updating scene: id={}, newSceneNumber={}, newLocation={}",
            id, scene.getSceneNumber(), scene.getLocation());
        Scene existing = getSceneById(id);

        Integer oldSceneNumber = existing.getSceneNumber();
        String oldLocation = existing.getLocation();

        existing.setSceneNumber(scene.getSceneNumber());
        existing.setLocation(scene.getLocation());
        existing.setMood(scene.getMood());
        existing.setDescription(scene.getDescription());
        existing.setParticipants(scene.getParticipants());

        Scene updated = sceneRepository.save(existing);
        log.info("Scene updated: id={}, sceneNumber: {} -> {}, location: {} -> {}",
            id, oldSceneNumber, updated.getSceneNumber(), oldLocation, updated.getLocation());
        return updated;
    }

    /**
     * 장면 삭제
     */
    @Transactional
    public void deleteScene(Long id) {
        log.info("Deleting scene: id={}", id);

        if (!sceneRepository.existsById(id)) {
            log.error("Cannot delete - scene not found: id={}", id);
            throw new ResourceNotFoundException("장면", id);
        }

        Scene scene = getSceneById(id);
        log.info("Deleting scene: id={}, sceneNumber={}, location={}",
            id, scene.getSceneNumber(), scene.getLocation());

        sceneRepository.deleteById(id);
        log.info("Scene deleted successfully: id={}", id);
    }

    /**
     * 장면에 대사 추가
     */
    @Transactional
    public Scene addDialogue(Long sceneId, Dialogue dialogue) {
        log.info("Adding dialogue to scene: sceneId={}", sceneId);
        Scene scene = getSceneById(sceneId);

        int newOrder = scene.getDialogues().size() + 1;
        dialogue.setScene(scene);
        dialogue.setDialogueOrder(newOrder);
        scene.getDialogues().add(dialogue);

        log.debug("Adding dialogue: sceneId={}, order={}, characterId={}",
            sceneId, newOrder, dialogue.getCharacter() != null ? dialogue.getCharacter().getId() : "null");

        Scene updated = sceneRepository.save(scene);
        log.info("Dialogue added successfully: sceneId={}, totalDialogues={}", sceneId, updated.getDialogues().size());
        return updated;
    }

    /**
     * 장면의 대사 목록 조회
     */
    public List<Dialogue> getDialogues(Long sceneId) {
        log.debug("Fetching dialogues for scene: {}", sceneId);
        // Use DialogueRepository directly to ensure character is fetched
        List<Dialogue> dialogues = dialogueRepository.findBySceneIdOrderByDialogueOrderAsc(sceneId);
        log.info("Found {} dialogues for scene: {}", dialogues.size(), sceneId);
        return dialogues;
    }
}