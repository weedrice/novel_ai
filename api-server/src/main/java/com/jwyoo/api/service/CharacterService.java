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
        return characterRepository.findAll();
    }

    /**
     * ID로 캐릭터 조회
     */
    public Character getCharacterById(Long id) {
        log.debug("Fetching character by id: {}", id);
        return characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("캐릭터", id));
    }

    /**
     * characterId로 캐릭터 조회
     */
    public Character getCharacterByCharacterId(String characterId) {
        log.debug("Fetching character by characterId: {}", characterId);
        return characterRepository.findByCharacterId(characterId)
            .orElseThrow(() -> new ResourceNotFoundException("캐릭터", characterId));
    }

    /**
     * 새로운 캐릭터 생성
     */
    @Transactional
    public Character createCharacter(Character character) {
        log.info("Creating new character: {}", character.getCharacterId());

        if (characterRepository.existsByCharacterId(character.getCharacterId())) {
            throw new IllegalArgumentException("이미 존재하는 캐릭터 ID입니다: " + character.getCharacterId());
        }

        Character saved = characterRepository.save(character);
        log.info("Character created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * 캐릭터 정보 수정
     */
    @Transactional
    public Character updateCharacter(Long id, Character character) {
        log.info("Updating character: {}", id);
        Character existing = getCharacterById(id);

        existing.setName(character.getName());
        existing.setDescription(character.getDescription());
        existing.setPersonality(character.getPersonality());
        existing.setSpeakingStyle(character.getSpeakingStyle());
        existing.setVocabulary(character.getVocabulary());
        existing.setToneKeywords(character.getToneKeywords());

        Character updated = characterRepository.save(existing);
        log.info("Character updated successfully: {}", id);
        return updated;
    }

    /**
     * 캐릭터 삭제
     */
    @Transactional
    public void deleteCharacter(Long id) {
        log.info("Deleting character: {}", id);

        if (!characterRepository.existsById(id)) {
            throw new ResourceNotFoundException("캐릭터", id);
        }

        characterRepository.deleteById(id);
        log.info("Character deleted successfully: {}", id);
    }

    /**
     * 말투 프로필 수정
     */
    @Transactional
    public Character updateSpeakingProfile(Long id, Character profileUpdate) {
        log.info("Updating speaking profile for character: {}", id);
        Character existing = getCharacterById(id);

        existing.setSpeakingStyle(profileUpdate.getSpeakingStyle());
        existing.setVocabulary(profileUpdate.getVocabulary());
        existing.setToneKeywords(profileUpdate.getToneKeywords());
        existing.setExamples(profileUpdate.getExamples());
        existing.setProhibitedWords(profileUpdate.getProhibitedWords());
        existing.setSentencePatterns(profileUpdate.getSentencePatterns());

        Character updated = characterRepository.save(existing);
        log.info("Speaking profile updated successfully: {}", id);
        return updated;
    }
}