package com.jwyoo.api.service;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    public List<Episode> getAllEpisodes() {
        return episodeRepository.findAllByOrderByEpisodeOrderAsc();
    }

    public Episode getEpisodeById(Long id) {
        return episodeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + id));
    }

    @Transactional
    public Episode createEpisode(Episode episode) {
        return episodeRepository.save(episode);
    }

    @Transactional
    public Episode updateEpisode(Long id, Episode episode) {
        Episode existing = getEpisodeById(id);
        existing.setTitle(episode.getTitle());
        existing.setDescription(episode.getDescription());
        existing.setEpisodeOrder(episode.getEpisodeOrder());
        return episodeRepository.save(existing);
    }

    @Transactional
    public void deleteEpisode(Long id) {
        episodeRepository.deleteById(id);
    }
}