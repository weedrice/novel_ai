package com.jwyoo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SuggestRequest(
    @NotBlank String speakerId,
    @NotNull List<String> targetIds,
    @NotBlank String intent,
    @NotBlank String honorific,
    Integer maxLen,
    Integer nCandidates
) {
}
