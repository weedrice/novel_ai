package com.jwyoo.api.dto;

import com.jwyoo.api.entity.Concept;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptResponse {

    private Long id;
    private String name;
    private String type;
    private String description;
    private Long projectId;
    private Long episodeId;
    private Double importance;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConceptResponse from(Concept concept) {
        return ConceptResponse.builder()
            .id(concept.getId())
            .name(concept.getName())
            .type(concept.getType())
            .description(concept.getDescription())
            .projectId(concept.getProject().getId())
            .episodeId(concept.getEpisode() != null ? concept.getEpisode().getId() : null)
            .importance(concept.getImportance())
            .source(concept.getSource())
            .createdAt(concept.getCreatedAt())
            .updatedAt(concept.getUpdatedAt())
            .build();
    }
}
