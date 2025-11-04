package com.jwyoo.api.controller;

import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.EpisodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = EpisodeController.class,
        excludeAutoConfiguration = {CacheAutoConfiguration.class, RedisAutoConfiguration.class})
@ActiveProfiles("test")
class EpisodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EpisodeService episodeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("에피소드 목록 조회 성공")
    @WithMockUser
    void getEpisodes_Success() throws Exception {
        // given
        Episode episode1 = new Episode();
        episode1.setId(1L);
        episode1.setTitle("Episode 1");
        episode1.setDescription("Description 1");
        episode1.setEpisodeOrder(1);

        Episode episode2 = new Episode();
        episode2.setId(2L);
        episode2.setTitle("Episode 2");
        episode2.setDescription(null); // null 처리 테스트
        episode2.setEpisodeOrder(2);

        List<Episode> episodes = Arrays.asList(episode1, episode2);
        when(episodeService.getAllEpisodes()).thenReturn(episodes);

        // when & then
        mockMvc.perform(get("/episodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Episode 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[0].order").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value(""));

        verify(episodeService).getAllEpisodes();
    }

    @Test
    @DisplayName("에피소드 목록 조회 - 빈 목록")
    @WithMockUser
    void getEpisodes_EmptyList() throws Exception {
        // given
        when(episodeService.getAllEpisodes()).thenReturn(Arrays.asList());

        // when & then
        mockMvc.perform(get("/episodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(episodeService).getAllEpisodes();
    }
}
