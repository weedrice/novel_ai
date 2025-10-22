package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.CharacterRepository;
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

    /**
     * 모든 캐릭터 조회
     */
    public List<Character> getAllCharacters() {
        log.debug("Fetching all characters");
        List<Character> characters = characterRepository.findAll();
        log.info("Fetched {} characters", characters.size());
        return characters;
    }

    /**
     * ID로 캐릭터 조회
     */
    public Character getCharacterById(Long id) {
        log.debug("Fetching character by id: {}", id);
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Character not found with id: {}", id);
                return new ResourceNotFoundException("캐릭터", id);
            });
        log.debug("Found character: id={}, characterId={}, name={}",
            character.getId(), character.getCharacterId(), character.getName());
        return character;
    }

    /**
     * characterId로 캐릭터 조회
     */
    public Character getCharacterByCharacterId(String characterId) {
        log.debug("Fetching character by characterId: {}", characterId);
        Character character = characterRepository.findByCharacterId(characterId)
            .orElseThrow(() -> {
                log.error("Character not found with characterId: {}", characterId);
                return new ResourceNotFoundException("캐릭터", characterId);
            });
        log.debug("Found character: id={}, name={}", character.getId(), character.getName());
        return character;
    }

    /**
     * 새로운 캐릭터 생성
     */
    @Transactional
    public Character createCharacter(Character character) {
        log.info("Creating new character: characterId={}, name={}",
            character.getCharacterId(), character.getName());

        if (characterRepository.existsByCharacterId(character.getCharacterId())) {
            log.error("Character with characterId already exists: {}", character.getCharacterId());
            throw new IllegalArgumentException("이미 존재하는 캐릭터 ID입니다: " + character.getCharacterId());
        }

        Character saved = characterRepository.save(character);
        log.info("Character created successfully: id={}, characterId={}, name={}",
            saved.getId(), saved.getCharacterId(), saved.getName());
        return saved;
    }

    /**
     * 캐릭터 정보 수정
     */
    @Transactional
    public Character updateCharacter(Long id, Character character) {
        log.info("Updating character: id={}, newName={}", id, character.getName());
        Character existing = getCharacterById(id);

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
     * 캐릭터 삭제
     */
    @Transactional
    public void deleteCharacter(Long id) {
        log.info("Deleting character: id={}", id);

        if (!characterRepository.existsById(id)) {
            log.error("Cannot delete - character not found: id={}", id);
            throw new ResourceNotFoundException("캐릭터", id);
        }

        Character character = getCharacterById(id);
        log.info("Deleting character: id={}, characterId={}, name={}",
            id, character.getCharacterId(), character.getName());

        characterRepository.deleteById(id);
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