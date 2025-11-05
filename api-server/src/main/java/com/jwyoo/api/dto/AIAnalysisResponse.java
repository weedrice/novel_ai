package com.jwyoo.api.dto;

import com.jwyoo.api.entity.AIAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 분석 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisResponse {

    private Long id;
    private Long episodeId;
    private String episodeTitle;
    private String analysisType;
    private String modelName;
    private String result;
    private Double confidence;
    private Long executionTimeMs;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * AIAnalysis 엔티티를 AIAnalysisResponse DTO로 변환
     */
    public static AIAnalysisResponse from(AIAnalysis analysis) {
        return AIAnalysisResponse.builder()
            .id(analysis.getId())
            .episodeId(analysis.getEpisode().getId())
            .episodeTitle(analysis.getEpisode().getTitle())
            .analysisType(analysis.getAnalysisType())
            .modelName(analysis.getModelName())
            .result(analysis.getResult())
            .confidence(analysis.getConfidence())
            .executionTimeMs(analysis.getExecutionTimeMs())
            .status(analysis.getStatus())
            .errorMessage(analysis.getErrorMessage())
            .createdAt(analysis.getCreatedAt())
            .updatedAt(analysis.getUpdatedAt())
            .build();
    }
}
