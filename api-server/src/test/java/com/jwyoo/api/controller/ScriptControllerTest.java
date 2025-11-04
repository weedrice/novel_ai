package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Script;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.ScriptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ScriptController.class,
        excludeAutoConfiguration = {CacheAutoConfiguration.class, RedisAutoConfiguration.class})
class ScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScriptService scriptService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("모든 스크립트 조회 성공")
    @WithMockUser
    void getAllScripts_Success() throws Exception {
        // given
        Script script = new Script();
        script.setId(1L);
        script.setTitle("Test Script");

        when(scriptService.getAllScripts()).thenReturn(Arrays.asList(script));

        // when & then
        mockMvc.perform(get("/scripts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Script"));

        verify(scriptService).getAllScripts();
    }

    @Test
    @DisplayName("ID로 스크립트 조회 성공")
    @WithMockUser
    void getScriptById_Success() throws Exception {
        // given
        Script script = new Script();
        script.setId(1L);
        script.setTitle("Test Script");

        when(scriptService.getScriptById(1L)).thenReturn(script);

        // when & then
        mockMvc.perform(get("/scripts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Script"));

        verify(scriptService).getScriptById(1L);
    }

    @Test
    @DisplayName("상태별 스크립트 조회 성공")
    @WithMockUser
    void getScriptsByStatus_Success() throws Exception {
        // given
        Script script = new Script();
        script.setId(1L);
        script.setStatus("pending");

        when(scriptService.getScriptsByStatus("pending")).thenReturn(Arrays.asList(script));

        // when & then
        mockMvc.perform(get("/scripts/status/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(scriptService).getScriptsByStatus("pending");
    }

    @Test
    @DisplayName("스크립트 업로드 성공")
    @WithMockUser
    void uploadScript_Success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("title", "New Script");
        request.put("content", "Script content");
        request.put("formatHint", "novel");

        Script uploadedScript = new Script();
        uploadedScript.setId(1L);
        uploadedScript.setTitle("New Script");
        uploadedScript.setContent("Script content");

        when(scriptService.uploadScript(anyString(), anyString(), anyString())).thenReturn(uploadedScript);

        // when & then
        mockMvc.perform(post("/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Script"));

        verify(scriptService).uploadScript("New Script", "Script content", "novel");
    }

    @Test
    @DisplayName("스크립트 분석 시작 성공")
    @WithMockUser
    void analyzeScript_Success() throws Exception {
        // given
        Script analyzedScript = new Script();
        analyzedScript.setId(1L);
        analyzedScript.setStatus("analyzed");

        when(scriptService.analyzeScript(1L, "openai")).thenReturn(analyzedScript);

        // when & then
        mockMvc.perform(post("/scripts/1/analyze")
                        .param("provider", "openai")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("analyzed"));

        verify(scriptService).analyzeScript(1L, "openai");
    }

    @Test
    @DisplayName("분석 결과 조회 성공")
    @WithMockUser
    void getAnalysisResult_Success() throws Exception {
        // given
        Map<String, Object> analysisResult = new HashMap<>();
        analysisResult.put("characters", Arrays.asList());
        analysisResult.put("dialogues", Arrays.asList());

        when(scriptService.getAnalysisResult(1L)).thenReturn(analysisResult);

        // when & then
        mockMvc.perform(get("/scripts/1/analysis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters").isArray())
                .andExpect(jsonPath("$.dialogues").isArray());

        verify(scriptService).getAnalysisResult(1L);
    }

    @Test
    @DisplayName("스크립트 삭제 성공")
    @WithMockUser
    void deleteScript_Success() throws Exception {
        // given
        doNothing().when(scriptService).deleteScript(1L);

        // when & then
        mockMvc.perform(delete("/scripts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(scriptService).deleteScript(1L);
    }

    @Test
    @DisplayName("스크립트 검색 성공")
    @WithMockUser
    void searchScripts_Success() throws Exception {
        // given
        Script script = new Script();
        script.setId(1L);
        script.setTitle("Search Result");

        when(scriptService.searchScripts("keyword")).thenReturn(Arrays.asList(script));

        // when & then
        mockMvc.perform(get("/scripts/search")
                        .param("keyword", "keyword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Search Result"));

        verify(scriptService).searchScripts("keyword");
    }

    @Test
    @DisplayName("업로드 및 즉시 분석 성공")
    @WithMockUser
    void uploadAndAnalyze_Success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("title", "New Script");
        request.put("content", "Content");
        request.put("formatHint", "novel");
        request.put("provider", "openai");

        Script script = new Script();
        script.setId(1L);
        script.setTitle("New Script");

        Map<String, Object> analysisResult = new HashMap<>();
        analysisResult.put("characters", Arrays.asList());

        when(scriptService.uploadScript(anyString(), anyString(), anyString())).thenReturn(script);
        when(scriptService.analyzeScript(anyLong(), anyString())).thenReturn(script);
        when(scriptService.getAnalysisResult(anyLong())).thenReturn(analysisResult);

        // when & then
        mockMvc.perform(post("/scripts/upload-and-analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.script").exists())
                .andExpect(jsonPath("$.analysis").exists())
                .andExpect(jsonPath("$.analysis.characters").isArray());

        verify(scriptService).uploadScript("New Script", "Content", "novel");
        verify(scriptService).analyzeScript(1L, "openai");
        verify(scriptService).getAnalysisResult(1L);
    }
}
