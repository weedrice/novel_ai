package com.jwyoo.api.controller;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/episodes")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    @GetMapping
    public List<Map<String, Object>> getEpisodes() {
        return episodeService.getAllEpisodes().stream()
            .map(episode -> Map.of(
                "id", (Object) episode.getId(),
                "title", episode.getTitle(),
                "description", episode.getDescription() != null ? episode.getDescription() : "",
                "order", episode.getEpisodeOrder()
            ))
            .collect(Collectors.toList());
    }
}
