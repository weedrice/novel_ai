package com.jwyoo.api.service;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 에피소드 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final ProjectService projectService;

    /**
     * 모든 에피소드 조회 (프로젝트별)
     */
    public List<Episode> getAllEpisodes() {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching all episodes for project: {}", currentProject.getId());
        List<Episode> episodes = episodeRepository.findByProjectOrderByEpisodeOrderAsc(currentProject);
        log.info("Fetched {} episodes", episodes.size());
        return episodes;
    }

    /**
     * ID로 에피소드 조회 (프로젝트별)
     */
    public Episode getEpisodeById(Long id) {
        Project currentProject = projectService.getCurrentProject();
        log.debug("Fetching episode by id: {}, project: {}", id, currentProject.getId());
        Episode episode = episodeRepository.findByIdAndProject(id, currentProject)
            .orElseThrow(() -> {
                log.error("Episode not found with id: {} in project: {}", id, currentProject.getId());
                return new IllegalArgumentException("Episode not found: " + id);
            });
        log.debug("Found episode: id={}, title={}", episode.getId(), episode.getTitle());
        return episode;
    }

    /**
     * 새로운 에피소드 생성 (현재 프로젝트에 자동 연결)
     */
    @Transactional
    public Episode createEpisode(Episode episode) {
        Project currentProject = projectService.getCurrentProject();
        log.info("Creating new episode: title={}, order={}, project={}",
                episode.getTitle(), episode.getEpisodeOrder(), currentProject.getId());

        // 현재 프로젝트 자동 설정
        episode.setProject(currentProject);

        Episode saved = episodeRepository.save(episode);
        log.info("Episode created successfully: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 에피소드 수정 (프로젝트별)
     */
    @Transactional
    public Episode updateEpisode(Long id, Episode episode) {
        log.info("Updating episode: id={}, newTitle={}, newOrder={}",
            id, episode.getTitle(), episode.getEpisodeOrder());
        Episode existing = getEpisodeById(id); // 이미 프로젝트 확인 포함

        String oldTitle = existing.getTitle();
        Integer oldOrder = existing.getEpisodeOrder();

        existing.setTitle(episode.getTitle());
        existing.setDescription(episode.getDescription());
        existing.setEpisodeOrder(episode.getEpisodeOrder());

        Episode updated = episodeRepository.save(existing);
        log.info("Episode updated: id={}, title: {} -> {}, order: {} -> {}",
            id, oldTitle, updated.getTitle(), oldOrder, updated.getEpisodeOrder());
        return updated;
    }

    /**
     * 에피소드 삭제 (프로젝트별)
     */
    @Transactional
    public void deleteEpisode(Long id) {
        log.info("Deleting episode: id={}", id);

        Episode episode = getEpisodeById(id); // 이미 프로젝트 확인 포함

        episodeRepository.delete(episode);
        log.info("Episode deleted successfully: id={}", id);
    }
}