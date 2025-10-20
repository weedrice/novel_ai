package com.jwyoo.api.dto;

import com.jwyoo.api.entity.Character;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterDto {

    private Long id;

    @NotBlank
    private String characterId;

    @NotBlank
    private String name;

    private String description;
    private String personality;
    private String speakingStyle;
    private String vocabulary;
    private String toneKeywords;

    public static CharacterDto fromEntity(Character character) {
        return CharacterDto.builder()
            .id(character.getId())
            .characterId(character.getCharacterId())
            .name(character.getName())
            .description(character.getDescription())
            .personality(character.getPersonality())
            .speakingStyle(character.getSpeakingStyle())
            .vocabulary(character.getVocabulary())
            .toneKeywords(character.getToneKeywords())
            .build();
    }

    public Character toEntity() {
        return Character.builder()
            .characterId(this.characterId)
            .name(this.name)
            .description(this.description)
            .personality(this.personality)
            .speakingStyle(this.speakingStyle)
            .vocabulary(this.vocabulary)
            .toneKeywords(this.toneKeywords)
            .build();
    }
}