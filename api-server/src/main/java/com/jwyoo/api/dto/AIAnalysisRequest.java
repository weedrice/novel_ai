package com.jwyoo.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 분석 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRequest {

    @NotNull(message = "에피소드 ID는 필수입니다")
    private Long episodeId;

    @NotBlank(message = "분석 유형은 필수입니다")
    private String analysisType;

    @NotBlank(message = "모델 이름은 필수입니다")
    private String modelName;

    @NotBlank(message = "분석 결과는 필수입니다")
    private String result;

    @Min(value = 0, message = "신뢰도는 0.0 이상이어야 합니다")
    @Max(value = 1, message = "신뢰도는 1.0 이하여야 합니다")
    private Double confidence;

    private Long executionTimeMs;

    private String status;

    private String errorMessage;
}
