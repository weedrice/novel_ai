package com.jwyoo.api.repository;

import com.jwyoo.api.entity.AIAnalysis;
import com.jwyoo.api.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    /**
     * 특정 에피소드의 모든 분석 결과 조회 (최신순)
     */
    List<AIAnalysis> findByEpisodeOrderByCreatedAtDesc(Episode episode);

    /**
     * 특정 에피소드의 분석 결과를 생성일 기준 최신순으로 조회
     */
    List<AIAnalysis> findByEpisode_IdOrderByCreatedAtDesc(Long episodeId);

    /**
     * 특정 에피소드의 특정 분석 유형 결과 조회 (최신순)
     */
    List<AIAnalysis> findByEpisodeAndAnalysisTypeOrderByCreatedAtDesc(Episode episode, String analysisType);

    /**
     * 특정 에피소드 ID와 분석 유형으로 조회 (최신순)
     */
    List<AIAnalysis> findByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(Long episodeId, String analysisType);

    /**
     * 특정 에피소드의 특정 모델 분석 결과 조회 (최신순)
     */
    List<AIAnalysis> findByEpisodeAndModelNameOrderByCreatedAtDesc(Episode episode, String modelName);

    /**
     * 특정 에피소드의 특정 분석 유형 + 모델 결과 조회 (최신순)
     */
    List<AIAnalysis> findByEpisodeAndAnalysisTypeAndModelNameOrderByCreatedAtDesc(
        Episode episode, String analysisType, String modelName);

    /**
     * 특정 에피소드 ID와 분석 유형, 모델로 조회 (최신순)
     */
    List<AIAnalysis> findByEpisode_IdAndAnalysisTypeAndModelNameOrderByCreatedAtDesc(
        Long episodeId, String analysisType, String modelName);

    /**
     * 특정 에피소드의 최신 분석 결과 1개 조회 (분석 유형별)
     */
    Optional<AIAnalysis> findFirstByEpisodeAndAnalysisTypeOrderByCreatedAtDesc(Episode episode, String analysisType);

    /**
     * 특정 에피소드 ID의 최신 분석 결과 1개 조회 (분석 유형별)
     */
    Optional<AIAnalysis> findFirstByEpisode_IdAndAnalysisTypeOrderByCreatedAtDesc(Long episodeId, String analysisType);

    /**
     * 특정 프로젝트의 모든 분석 결과 조회 (Episode를 통해 간접 조회)
     */
    @Query("SELECT a FROM AIAnalysis a WHERE a.episode.project.id = :projectId ORDER BY a.createdAt DESC")
    List<AIAnalysis> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 특정 프로젝트의 특정 분석 유형 결과 조회
     */
    @Query("SELECT a FROM AIAnalysis a WHERE a.episode.project.id = :projectId AND a.analysisType = :analysisType ORDER BY a.createdAt DESC")
    List<AIAnalysis> findByProjectIdAndAnalysisType(@Param("projectId") Long projectId, @Param("analysisType") String analysisType);

    /**
     * 분석 상태별 조회 (실패한 분석 등을 추적하기 위해)
     */
    List<AIAnalysis> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 특정 에피소드의 분석 개수 조회
     */
    long countByEpisode(Episode episode);

    /**
     * 특정 에피소드 ID의 분석 개수 조회
     */
    long countByEpisode_Id(Long episodeId);
}
