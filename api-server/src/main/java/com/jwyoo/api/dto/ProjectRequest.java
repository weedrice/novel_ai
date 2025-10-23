package com.jwyoo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프로젝트 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "프로젝트 이름은 필수입니다")
    @Size(min = 1, max = 200, message = "프로젝트 이름은 1-200자 사이여야 합니다")
    private String name;

    @Size(max = 1000, message = "설명은 최대 1000자까지 가능합니다")
    private String description;
}