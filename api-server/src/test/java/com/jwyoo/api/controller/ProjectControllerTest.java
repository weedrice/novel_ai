package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.ProjectRequest;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.security.JwtTokenProvider;
import com.jwyoo.api.service.ProjectService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProjectController.class,
        excludeAutoConfiguration = {CacheAutoConfiguration.class, RedisAutoConfiguration.class})
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("내 프로젝트 목록 조회 성공")
    @WithMockUser
    void getMyProjects_Success() throws Exception {
        // given
        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("Project 1");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");

        List<Project> projects = Arrays.asList(project1, project2);
        when(projectService.getMyProjects()).thenReturn(projects);

        // when & then
        mockMvc.perform(get("/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Project 1"))
                .andExpect(jsonPath("$[1].name").value("Project 2"));

        verify(projectService).getMyProjects();
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    @WithMockUser
    void createProject_Success() throws Exception {
        // given
        ProjectRequest request = new ProjectRequest();
        request.setName("New Project");
        request.setDescription("Description");

        Project createdProject = new Project();
        createdProject.setId(1L);
        createdProject.setName("New Project");
        createdProject.setDescription("Description");

        when(projectService.createProject(anyString(), anyString())).thenReturn(createdProject);

        // when & then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Project"));

        verify(projectService).createProject("New Project", "Description");
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - Validation 오류")
    @WithMockUser
    void createProject_ValidationError() throws Exception {
        // given - name이 없는 잘못된 요청
        ProjectRequest request = new ProjectRequest();
        request.setDescription("Description only");

        // when & then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).createProject(anyString(), anyString());
    }

    @Test
    @DisplayName("프로젝트 상세 조회 성공")
    @WithMockUser
    void getProject_Success() throws Exception {
        // given
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        when(projectService.getProject(1L)).thenReturn(project);

        // when & then
        mockMvc.perform(get("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService).getProject(1L);
    }

    @Test
    @DisplayName("프로젝트 상세 조회 실패 - 존재하지 않음")
    @WithMockUser
    void getProject_NotFound() throws Exception {
        // given
        when(projectService.getProject(999L)).thenThrow(new ResourceNotFoundException("Project not found"));

        // when & then
        mockMvc.perform(get("/projects/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService).getProject(999L);
    }

    @Test
    @DisplayName("프로젝트 수정 성공")
    @WithMockUser
    void updateProject_Success() throws Exception {
        // given
        ProjectRequest request = new ProjectRequest();
        request.setName("Updated Project");
        request.setDescription("Updated Description");

        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Updated Project");
        updatedProject.setDescription("Updated Description");

        when(projectService.updateProject(eq(1L), anyString(), anyString())).thenReturn(updatedProject);

        // when & then
        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"));

        verify(projectService).updateProject(1L, "Updated Project", "Updated Description");
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    @WithMockUser
    void deleteProject_Success() throws Exception {
        // given
        doNothing().when(projectService).deleteProject(1L);

        // when & then
        mockMvc.perform(delete("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(1L);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 존재하지 않음")
    @WithMockUser
    void deleteProject_NotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Project not found")).when(projectService).deleteProject(999L);

        // when & then
        mockMvc.perform(delete("/projects/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService).deleteProject(999L);
    }

    @Test
    @DisplayName("프로젝트 검색 성공")
    @WithMockUser
    void searchProjects_Success() throws Exception {
        // given
        Project project = new Project();
        project.setId(1L);
        project.setName("Search Result");

        when(projectService.searchProjects("search")).thenReturn(Arrays.asList(project));

        // when & then
        mockMvc.perform(get("/projects/search")
                        .param("keyword", "search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Search Result"));

        verify(projectService).searchProjects("search");
    }
}
