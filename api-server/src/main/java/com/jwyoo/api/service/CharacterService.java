package com.jwyoo.api.service;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterService {

    private final CharacterRepository characterRepository;

    public List<Character> getAllCharacters() {
        return characterRepository.findAll();
    }

    public Character getCharacterById(Long id) {
        return characterRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));
    }

    public Character getCharacterByCharacterId(String characterId) {
        return characterRepository.findByCharacterId(characterId)
            .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
    }

    @Transactional
    public Character createCharacter(Character character) {
        if (characterRepository.existsByCharacterId(character.getCharacterId())) {
            throw new IllegalArgumentException("Character ID already exists: " + character.getCharacterId());
        }
        return characterRepository.save(character);
    }

    @Transactional
    public Character updateCharacter(Long id, Character character) {
        Character existing = getCharacterById(id);
        existing.setName(character.getName());
        existing.setDescription(character.getDescription());
        existing.setPersonality(character.getPersonality());
        existing.setSpeakingStyle(character.getSpeakingStyle());
        existing.setVocabulary(character.getVocabulary());
        existing.setToneKeywords(character.getToneKeywords());
        return characterRepository.save(existing);
    }

    @Transactional
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
    }
}