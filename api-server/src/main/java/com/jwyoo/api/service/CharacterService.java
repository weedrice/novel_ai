package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 캐릭터 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    /**
     * 모든 캐릭터 조회 (프로젝트별)
     */
    public List<Character> getAllCharacters() {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching all characters for project: {}", currentProject.getId());
        List<Character> characters = characterRepository.findByProject(currentProject);
        log.info("Fetched {} characters", characters.size());
        return characters;
    }

    /**
     * ID로 캐릭터 조회 (프로젝트별)
     */
    public Character getCharacterById(Long id) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching character by id: {}, project: {}", id, currentProject.getId());
        Character character = characterRepository.findByIdAndProject(id, currentProject)
            .orElseThrow(() -> {
                log.error("Character not found with id: {} in project: {}", id, currentProject.getId());
                return new ResourceNotFoundException("캐릭터", id);
            });
        log.debug("Found character: id={}, characterId={}, name={}",
            character.getId(), character.getCharacterId(), character.getName());
        return character;
    }

    /**
     * characterId로 캐릭터 조회 (프로젝트별)
     */
    public Character getCharacterByCharacterId(String characterId) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching character by characterId: {}, project: {}", characterId, currentProject.getId());
        Character character = characterRepository.findByCharacterIdAndProject(characterId, currentProject)
            .orElseThrow(() -> {
                log.error("Character not found with characterId: {} in project: {}", characterId, currentProject.getId());
                return new ResourceNotFoundException("캐릭터", characterId);
            });
        log.debug("Found character: id={}, name={}", character.getId(), character.getName());
        return character;
    }

    /**
     * 새로운 캐릭터 생성 (현재 프로젝트에 자동 연결)
     */
    @Transactional
    public Character createCharacter(Character character) {
        Project currentProject = projectService.getCurrentProject();
        log.info("Creating new character: characterId={}, name={}, project={}",
            character.getCharacterId(), character.getName(), currentProject.getId());

        // 같은 프로젝트 내에서 중복 확인
        if (characterRepository.existsByCharacterIdAndProject(character.getCharacterId(), currentProject)) {
            log.error("Character with characterId already exists in project: {}", character.getCharacterId());
            throw new IllegalArgumentException("이미 존재하는 캐릭터 ID입니다: " + character.getCharacterId());
        }

        // 현재 프로젝트 자동 설정
        character.setProject(currentProject);

        Character saved = characterRepository.save(character);
        log.info("Character created successfully: id={}, characterId={}, name={}",
            saved.getId(), saved.getCharacterId(), saved.getName());
        return saved;
    }

    /**
     * 캐릭터 정보 수정 (프로젝트별)
     */
    @Transactional
    public Character updateCharacter(Long id, Character character) {
        log.info("Updating character: id={}, newName={}", id, character.getName());
        Character existing = getCharacterById(id); // 이미 프로젝트 확인 포함

        String oldName = existing.getName();
        log.debug("Character before update: id={}, name={}, personality={}",
            id, oldName, existing.getPersonality());

        existing.setName(character.getName());
        existing.setDescription(character.getDescription());
        existing.setPersonality(character.getPersonality());
        existing.setSpeakingStyle(character.getSpeakingStyle());
        existing.setVocabulary(character.getVocabulary());
        existing.setToneKeywords(character.getToneKeywords());

        Character updated = characterRepository.save(existing);
        log.info("Character updated successfully: id={}, name: {} -> {}",
            id, oldName, updated.getName());
        return updated;
    }

    /**
     * 캐릭터 삭제 (프로젝트별)
     */
    @Transactional
    public void deleteCharacter(Long id) {
        log.info("Deleting character: id={}", id);

        Character character = getCharacterById(id); // 이미 프로젝트 확인 포함
        log.info("Deleting character: id={}, characterId={}, name={}",
            id, character.getCharacterId(), character.getName());

        characterRepository.delete(character);
        log.info("Character deleted successfully: id={}", id);
    }

    /**
     * 말투 프로필 수정
     */
    @Transactional
    public Character updateSpeakingProfile(Long id, Character profileUpdate) {
        log.info("Updating speaking profile for character: id={}", id);
        Character existing = getCharacterById(id);

        log.debug("Updating speaking profile fields for character: id={}, name={}",
            id, existing.getName());
        log.debug("New speaking style: {}", profileUpdate.getSpeakingStyle());
        log.debug("New tone keywords: {}", profileUpdate.getToneKeywords());

        existing.setSpeakingStyle(profileUpdate.getSpeakingStyle());
        existing.setVocabulary(profileUpdate.getVocabulary());
        existing.setToneKeywords(profileUpdate.getToneKeywords());
        existing.setExamples(profileUpdate.getExamples());
        existing.setProhibitedWords(profileUpdate.getProhibitedWords());
        existing.setSentencePatterns(profileUpdate.getSentencePatterns());

        Character updated = characterRepository.save(existing);
        log.info("Speaking profile updated successfully for character: id={}, name={}",
            id, updated.getName());
        return updated;
    }
}