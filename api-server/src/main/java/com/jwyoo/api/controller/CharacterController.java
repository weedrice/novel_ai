package com.jwyoo.api.controller;

import com.jwyoo.api.dto.CharacterDto;
import com.jwyoo.api.dto.SpeakingProfileDto;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.service.CharacterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping
    public List<CharacterDto> getAllCharacters() {
        return characterService.getAllCharacters().stream()
            .map(CharacterDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CharacterDto getCharacterById(@PathVariable Long id) {
        return CharacterDto.fromEntity(characterService.getCharacterById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterDto createCharacter(@RequestBody @Valid CharacterDto dto) {
        Character created = characterService.createCharacter(dto.toEntity());
        return CharacterDto.fromEntity(created);
    }

    @PutMapping("/{id}")
    public CharacterDto updateCharacter(@PathVariable Long id, @RequestBody @Valid CharacterDto dto) {
        Character updated = characterService.updateCharacter(id, dto.toEntity());
        return CharacterDto.fromEntity(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
    }

    /**
     * 말투 프로필 조회
     */
    @GetMapping("/{id}/speaking-profile")
    public SpeakingProfileDto getSpeakingProfile(@PathVariable Long id) {
        Character character = characterService.getCharacterById(id);
        return SpeakingProfileDto.fromEntity(character);
    }

    /**
     * 말투 프로필 업데이트
     */
    @PutMapping("/{id}/speaking-profile")
    public SpeakingProfileDto updateSpeakingProfile(
        @PathVariable Long id,
        @RequestBody @Valid SpeakingProfileDto profileDto
    ) {
        // DTO를 임시 Character 객체로 변환하여 서비스 호출
        Character tempCharacter = new Character();
        profileDto.applyToEntity(tempCharacter);

        Character updated = characterService.updateSpeakingProfile(id, tempCharacter);
        return SpeakingProfileDto.fromEntity(updated);
    }
}