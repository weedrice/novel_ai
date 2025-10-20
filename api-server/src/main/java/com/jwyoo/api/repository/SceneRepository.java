package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByEpisodeIdOrderBySceneNumberAsc(Long episodeId);
}