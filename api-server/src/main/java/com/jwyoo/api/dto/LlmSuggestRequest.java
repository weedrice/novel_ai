package com.jwyoo.api.dto;

import java.util.List;

/**
 * LLM 서버로 전송할 대사 제안 요청 DTO
 */
public record LlmSuggestRequest(
    String speakerId,
    List<String> targetIds,
    String intent,
    String honorific,
    Integer maxLen,
    Integer nCandidates,
    CharacterInfoDto characterInfo,  // 화자 캐릭터 정보
    List<String> targetNames,        // 대상 캐릭터 이름 목록
    String context,                  // 추가 컨텍스트 (선택적)
    String provider                  // LLM 프로바이더 (openai, claude, gemini)
) {
}