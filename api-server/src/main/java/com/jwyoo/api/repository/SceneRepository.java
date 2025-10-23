package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByEpisodeIdOrderBySceneNumberAsc(Long episodeId);

    // 프로젝트별 조회 (Episode를 통한 간접 필터링)
    @Query("SELECT s FROM Scene s WHERE s.episode.project = :project ORDER BY s.episode.episodeOrder, s.sceneNumber")
    List<Scene> findByProject(@Param("project") Project project);

    @Query("SELECT s FROM Scene s WHERE s.id = :id AND s.episode.project = :project")
    Optional<Scene> findByIdAndProject(@Param("id") Long id, @Param("project") Project project);

    @Query("SELECT s FROM Scene s WHERE s.episode.id = :episodeId AND s.episode.project = :project ORDER BY s.sceneNumber ASC")
    List<Scene> findByEpisodeIdAndProject(@Param("episodeId") Long episodeId, @Param("project") Project project);
}