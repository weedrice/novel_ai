package com.jwyoo.api.dto;

import com.jwyoo.api.entity.Character;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 말투 프로필 전용 DTO
 */
public record SpeakingProfileDto(
    @NotBlank(message = "말투 특징은 필수입니다")
    @Size(max = 500, message = "말투 특징은 500자를 초과할 수 없습니다")
    String speakingStyle,

    @Size(max = 500, message = "자주 사용하는 어휘는 500자를 초과할 수 없습니다")
    String vocabulary,

    @Size(max = 500, message = "말투 키워드는 500자를 초과할 수 없습니다")
    String toneKeywords,

    String examples,

    @Size(max = 1000, message = "금지 단어는 1000자를 초과할 수 없습니다")
    String prohibitedWords,

    String sentencePatterns
) {
    /**
     * Character 엔티티로부터 말투 프로필 DTO 생성
     */
    public static SpeakingProfileDto fromEntity(Character character) {
        return new SpeakingProfileDto(
            character.getSpeakingStyle(),
            character.getVocabulary(),
            character.getToneKeywords(),
            character.getExamples(),
            character.getProhibitedWords(),
            character.getSentencePatterns()
        );
    }

    /**
     * Character 엔티티에 말투 프로필 적용
     */
    public void applyToEntity(Character character) {
        character.setSpeakingStyle(this.speakingStyle);
        character.setVocabulary(this.vocabulary);
        character.setToneKeywords(this.toneKeywords);
        character.setExamples(this.examples);
        character.setProhibitedWords(this.prohibitedWords);
        character.setSentencePatterns(this.sentencePatterns);
    }
}