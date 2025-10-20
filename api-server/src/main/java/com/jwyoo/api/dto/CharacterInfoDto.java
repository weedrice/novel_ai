package com.jwyoo.api.dto;

/**
 * LLM 서버에 전달할 캐릭터 정보 DTO
 */
public record CharacterInfoDto(
    String name,
    String description,
    String personality,
    String speakingStyle,
    String vocabulary,
    String toneKeywords,
    String examples,
    String prohibitedWords,
    String sentencePatterns
) {
}