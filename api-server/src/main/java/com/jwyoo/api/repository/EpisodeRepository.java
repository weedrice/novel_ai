package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findAllByOrderByEpisodeOrderAsc();

    // 프로젝트별 조회
    List<Episode> findByProjectOrderByEpisodeOrderAsc(Project project);
    Optional<Episode> findByIdAndProject(Long id, Project project);
}