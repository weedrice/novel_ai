package com.jwyoo.api.service;

import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.entity.ScenarioVersion;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.ScenarioVersionRepository;
import com.jwyoo.api.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시나리오 버전 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioVersionService {

    private final ScenarioVersionRepository scenarioVersionRepository;
    private final SceneRepository sceneRepository;

    /**
     * 시나리오 버전 저장
     *
     * @param sceneId  장면 ID
     * @param title    버전 제목
     * @param content  시나리오 내용 (JSON)
     * @param createdBy 생성자
     * @return 저장된 시나리오 버전
     */
    @Transactional
    public ScenarioVersion saveVersion(Long sceneId, String title, String content, String createdBy) {
        log.info("Saving new scenario version for scene: {}", sceneId);

        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new ResourceNotFoundException("장면", sceneId));

        // 다음 버전 번호 계산
        Integer nextVersion = scenarioVersionRepository.findMaxVersionBySceneId(sceneId)
                .map(v -> v + 1)
                .orElse(1);

        ScenarioVersion scenarioVersion = ScenarioVersion.builder()
                .scene(scene)
                .version(nextVersion)
                .title(title)
                .content(content)
                .createdBy(createdBy)
                .build();

        ScenarioVersion saved = scenarioVersionRepository.save(scenarioVersion);
        log.info("Saved scenario version: {} (version: {})", saved.getId(), saved.getVersion());

        return saved;
    }

    /**
     * 특정 장면의 모든 버전 조회
     *
     * @param sceneId 장면 ID
     * @return 버전 목록 (최신순)
     */
    @Transactional(readOnly = true)
    public List<ScenarioVersion> getVersionsBySceneId(Long sceneId) {
        log.debug("Fetching all versions for scene: {}", sceneId);
        return scenarioVersionRepository.findBySceneIdOrderByVersionDesc(sceneId);
    }

    /**
     * 특정 버전 조회
     *
     * @param versionId 버전 ID
     * @return 시나리오 버전
     */
    @Transactional(readOnly = true)
    public ScenarioVersion getVersionById(Long versionId) {
        log.debug("Fetching scenario version: {}", versionId);
        return scenarioVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("시나리오 버전", versionId));
    }

    /**
     * 특정 장면의 특정 버전 번호로 조회
     *
     * @param sceneId 장면 ID
     * @param version 버전 번호
     * @return 시나리오 버전
     */
    @Transactional(readOnly = true)
    public ScenarioVersion getVersionBySceneIdAndVersion(Long sceneId, Integer version) {
        log.debug("Fetching scenario version: scene={}, version={}", sceneId, version);
        return scenarioVersionRepository.findBySceneIdAndVersion(sceneId, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("장면 %d의 버전 %d", sceneId, version)));
    }

    /**
     * 버전 삭제
     *
     * @param versionId 버전 ID
     */
    @Transactional
    public void deleteVersion(Long versionId) {
        log.info("Deleting scenario version: {}", versionId);
        ScenarioVersion version = getVersionById(versionId);
        scenarioVersionRepository.delete(version);
        log.info("Deleted scenario version: {}", versionId);
    }

    /**
     * 특정 장면의 버전 개수 조회
     *
     * @param sceneId 장면 ID
     * @return 버전 개수
     */
    @Transactional(readOnly = true)
    public long countVersions(Long sceneId) {
        return scenarioVersionRepository.countBySceneId(sceneId);
    }
}