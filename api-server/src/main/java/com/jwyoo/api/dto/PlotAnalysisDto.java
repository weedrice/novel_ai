package com.jwyoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Task 99: 플롯 구조 시각화를 위한 분석 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotAnalysisDto {

    /**
     * 에피소드 기본 정보
     */
    private Long episodeId;
    private String episodeTitle;
    private String episodeDescription;

    /**
     * 장면별 분석 데이터
     */
    private List<SceneAnalysis> scenes;

    /**
     * 캐릭터별 등장 빈도
     */
    private List<CharacterFrequency> characterFrequencies;

    /**
     * 전체 통계
     */
    private int totalScenes;
    private int totalDialogues;
    private double averageTensionLevel;

    /**
     * 장면별 분석 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneAnalysis {
        private Long sceneId;
        private String sceneTitle;
        private String location;
        private String mood;
        private int sceneNumber;
        private int dialogueCount;

        /**
         * 갈등 강도 (0.0 ~ 1.0)
         * 대사 수, 참여 캐릭터 수, 분위기 등을 고려하여 계산
         */
        private double tensionLevel;

        /**
         * 참여 캐릭터 목록
         */
        private List<String> participants;

        /**
         * 타임라인 시각화를 위한 누적 대사 수
         */
        private int cumulativeDialogueCount;
    }

    /**
     * 캐릭터별 등장 빈도
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterFrequency {
        private String characterId;  // String 타입 (예: "char.seha")
        private String characterName;
        private int appearanceCount;  // 등장한 장면 수
        private int dialogueCount;    // 전체 대사 수
        private double appearanceRate; // 등장 비율 (%)
    }
}
