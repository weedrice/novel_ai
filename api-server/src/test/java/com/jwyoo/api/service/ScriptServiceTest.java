package com.jwyoo.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Script;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.repository.ScriptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ScriptService 단위 테스트
 * 스크립트 업로드, 조회, 분석, 삭제 테스트
 */
@ExtendWith(MockitoExtension.class)
class ScriptServiceTest {

    @Mock
    private ScriptRepository scriptRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ScriptService scriptService;

    private User testUser;
    private Project testProject;
    private Script testScript;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .build();

        testScript = Script.builder()
                .id(1L)
                .title("Test Script")
                .content("This is a test script content.")
                .formatHint("novel")
                .status("uploaded")
                .project(testProject)
                .build();
    }

    @Test
    @DisplayName("모든 스크립트 조회 성공")
    void getAllScripts_Success() {
        // given
        Script script2 = Script.builder()
                .id(2L)
                .title("Script 2")
                .content("Content 2")
                .status("uploaded")
                .project(testProject)
                .build();
        List<Script> scripts = Arrays.asList(testScript, script2);
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByProjectOrderByCreatedAtDesc(testProject)).thenReturn(scripts);

        // when
        List<Script> result = scriptService.getAllScripts();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Script");
        assertThat(result.get(1).getTitle()).isEqualTo("Script 2");

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByProjectOrderByCreatedAtDesc(testProject);
    }

    @Test
    @DisplayName("ID로 스크립트 조회 성공")
    void getScriptById_Success() {
        // given
        Long scriptId = 1L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByIdAndProject(scriptId, testProject)).thenReturn(Optional.of(testScript));

        // when
        Script result = scriptService.getScriptById(scriptId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(scriptId);
        assertThat(result.getTitle()).isEqualTo("Test Script");

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByIdAndProject(scriptId, testProject);
    }

    @Test
    @DisplayName("ID로 스크립트 조회 실패 - 존재하지 않음")
    void getScriptById_Failure_NotFound() {
        // given
        Long scriptId = 999L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByIdAndProject(scriptId, testProject)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scriptService.getScriptById(scriptId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Script");

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByIdAndProject(scriptId, testProject);
    }

    @Test
    @DisplayName("상태별 스크립트 조회 성공")
    void getScriptsByStatus_Success() {
        // given
        String status = "uploaded";
        List<Script> scripts = Arrays.asList(testScript);
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByProjectAndStatus(testProject, status)).thenReturn(scripts);

        // when
        List<Script> result = scriptService.getScriptsByStatus(status);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByProjectAndStatus(testProject, status);
    }

    @Test
    @DisplayName("스크립트 업로드 성공")
    void uploadScript_Success() {
        // given
        String title = "New Script";
        String content = "New content";
        String formatHint = "scenario";
        Script savedScript = Script.builder()
                .id(2L)
                .title(title)
                .content(content)
                .formatHint(formatHint)
                .status("uploaded")
                .project(testProject)
                .build();
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.save(any(Script.class))).thenReturn(savedScript);

        // when
        Script result = scriptService.uploadScript(title, content, formatHint);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getFormatHint()).isEqualTo(formatHint);
        assertThat(result.getStatus()).isEqualTo("uploaded");

        verify(projectService).getCurrentProject();
        verify(scriptRepository).save(any(Script.class));
    }

    @Test
    @DisplayName("분석 결과 조회 성공 - 결과 있음")
    void getAnalysisResult_Success_WithResult() throws JsonProcessingException {
        // given
        Long scriptId = 1L;
        String analysisResultJson = "{\"characters\": [], \"dialogues\": []}";
        Map<String, Object> expectedResult = Map.of("characters", List.of(), "dialogues", List.of());
        testScript.setAnalysisResult(analysisResultJson);

        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByIdAndProject(scriptId, testProject)).thenReturn(Optional.of(testScript));
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(expectedResult);

        // when
        Map<String, Object> result = scriptService.getAnalysisResult(scriptId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("characters");
        assertThat(result).containsKey("dialogues");

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByIdAndProject(scriptId, testProject);
        verify(objectMapper).readValue(analysisResultJson, Map.class);
    }

    @Test
    @DisplayName("분석 결과 조회 성공 - 결과 없음")
    void getAnalysisResult_Success_NoResult() {
        // given
        Long scriptId = 1L;
        testScript.setAnalysisResult(null);

        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByIdAndProject(scriptId, testProject)).thenReturn(Optional.of(testScript));

        // when
        Map<String, Object> result = scriptService.getAnalysisResult(scriptId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("characters");
        assertThat(result).containsKey("dialogues");
        assertThat(result).containsKey("scenes");
        assertThat(result).containsKey("relationships");
        assertThat(result.get("characters")).isEqualTo(List.of());

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByIdAndProject(scriptId, testProject);
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("분석 결과 조회 실패 - JSON 파싱 에러")
    void getAnalysisResult_Failure_JsonParsingError() throws JsonProcessingException {
        // given
        Long scriptId = 1L;
        String invalidJson = "{invalid json}";
        testScript.setAnalysisResult(invalidJson);

        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByIdAndProject(scriptId, testProject)).thenReturn(Optional.of(testScript));
        when(objectMapper.readValue(anyString(), eq(Map.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // when & then
        assertThatThrownBy(() -> scriptService.getAnalysisResult(scriptId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse analysis result");

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByIdAndProject(scriptId, testProject);
    }

    @Test
    @DisplayName("스크립트 삭제 성공")
    void deleteScript_Success() {
        // given
        Long scriptId = 1L;
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByIdAndProject(scriptId, testProject)).thenReturn(Optional.of(testScript));
        doNothing().when(scriptRepository).delete(testScript);

        // when
        scriptService.deleteScript(scriptId);

        // then
        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByIdAndProject(scriptId, testProject);
        verify(scriptRepository).delete(testScript);
    }

    @Test
    @DisplayName("스크립트 검색 성공")
    void searchScripts_Success() {
        // given
        String keyword = "Test";
        List<Script> scripts = Arrays.asList(testScript);
        when(projectService.getCurrentProject()).thenReturn(testProject);
        when(scriptRepository.findByProjectAndTitleContainingIgnoreCase(testProject, keyword))
                .thenReturn(scripts);

        // when
        List<Script> result = scriptService.searchScripts(keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains(keyword);

        verify(projectService).getCurrentProject();
        verify(scriptRepository).findByProjectAndTitleContainingIgnoreCase(testProject, keyword);
    }
}