package com.jwyoo.api.service;

import com.jwyoo.api.entity.Episode;
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

    /**
     * 모든 에피소드 조회
     */
    public List<Episode> getAllEpisodes() {
        log.debug("Fetching all episodes");
        List<Episode> episodes = episodeRepository.findAllByOrderByEpisodeOrderAsc();
        log.info("Fetched {} episodes", episodes.size());
        return episodes;
    }

    /**
     * ID로 에피소드 조회
     */
    public Episode getEpisodeById(Long id) {
        log.debug("Fetching episode by id: {}", id);
        Episode episode = episodeRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Episode not found with id: {}", id);
                return new IllegalArgumentException("Episode not found: " + id);
            });
        log.debug("Found episode: id={}, title={}", episode.getId(), episode.getTitle());
        return episode;
    }

    /**
     * 새로운 에피소드 생성
     */
    @Transactional
    public Episode createEpisode(Episode episode) {
        log.info("Creating new episode: title={}, order={}", episode.getTitle(), episode.getEpisodeOrder());
        Episode saved = episodeRepository.save(episode);
        log.info("Episode created successfully: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 에피소드 수정
     */
    @Transactional
    public Episode updateEpisode(Long id, Episode episode) {
        log.info("Updating episode: id={}, newTitle={}, newOrder={}",
            id, episode.getTitle(), episode.getEpisodeOrder());
        Episode existing = getEpisodeById(id);

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
     * 에피소드 삭제
     */
    @Transactional
    public void deleteEpisode(Long id) {
        log.info("Deleting episode: id={}", id);

        if (!episodeRepository.existsById(id)) {
            log.error("Cannot delete - episode not found: id={}", id);
            throw new IllegalArgumentException("Episode not found: " + id);
        }

        episodeRepository.deleteById(id);
        log.info("Episode deleted successfully: id={}", id);
    }
}