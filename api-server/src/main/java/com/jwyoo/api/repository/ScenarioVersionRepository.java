package com.jwyoo.api.repository;

import com.jwyoo.api.entity.ScenarioVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 시나리오 버전 Repository
 */
@Repository
public interface ScenarioVersionRepository extends JpaRepository<ScenarioVersion, Long> {

    /**
     * 특정 장면의 모든 버전 조회 (최신순)
     */
    List<ScenarioVersion> findBySceneIdOrderByVersionDesc(Long sceneId);

    /**
     * 특정 장면의 특정 버전 조회
     */
    Optional<ScenarioVersion> findBySceneIdAndVersion(Long sceneId, Integer version);

    /**
     * 특정 장면의 최신 버전 번호 조회
     */
    @Query("SELECT MAX(sv.version) FROM ScenarioVersion sv WHERE sv.scene.id = :sceneId")
    Optional<Integer> findMaxVersionBySceneId(Long sceneId);

    /**
     * 특정 장면의 버전 개수 조회
     */
    long countBySceneId(Long sceneId);
}