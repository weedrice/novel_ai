package com.jwyoo.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/episodes")
public class EpisodeController {

    @GetMapping
    public List<Map<String, Object>> getEpisodes() {
        return List.of(
            Map.of("id", 1, "title", "ep1 - 첫 만남"),
            Map.of("id", 2, "title", "ep2 - 갈등"),
            Map.of("id", 3, "title", "ep3 - 화해")
        );
    }
}
