package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findAllByOrderByEpisodeOrderAsc();

    // 프로젝트별 조회
    List<Episode> findByProjectOrderByEpisodeOrderAsc(Project project);
    List<Episode> findByProjectOrderByCreatedAtDesc(Project project); // 생성일 기준 최신순
    Optional<Episode> findByIdAndProject(Long id, Project project);

    /**
     * N+1 문제 해결: scenes를 함께 fetch
     * Episode와 연관된 모든 Scene을 한 번의 쿼리로 조회
     */
    @EntityGraph(attributePaths = {"scenes"})
    List<Episode> findWithScenesByProjectOrderByEpisodeOrderAsc(Project project);

    @EntityGraph(attributePaths = {"scenes"})
    Optional<Episode> findWithScenesById(Long id);

    @EntityGraph(attributePaths = {"scenes"})
    Optional<Episode> findWithScenesByIdAndProject(Long id, Project project);
}