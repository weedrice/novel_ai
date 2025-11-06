package com.jwyoo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type; // theme, emotion, event, setting, trait

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long episodeId; // Optional

    private Double importance; // 0.0 ~ 1.0

    private String source; // ai_analysis, manual
}
