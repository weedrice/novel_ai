package com.jwyoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Episode DTO
 * API 요청/응답에 사용되는 에피소드 데이터 전송 객체
 */
public class EpisodeDto {

    /**
     * 에피소드 생성/수정 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String title;
        private String description;
        private Integer episodeOrder;
        private String scriptText;
        private String scriptFormat;
    }

    /**
     * 에피소드 응답
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Integer episodeOrder;
        private String scriptText;
        private String scriptFormat;
        private String analysisStatus;
        private String llmProvider;
        private String createdAt;
        private String updatedAt;
    }
}
