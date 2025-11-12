package com.jwyoo.api.controller;

import com.jwyoo.api.dto.ProjectRequest;
import com.jwyoo.api.dto.ProjectResponse;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프로젝트 관리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Project 엔티티를 ProjectResponse DTO로 변환
     */
    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(ProjectResponse.OwnerInfo.builder()
                        .id(project.getOwner().getId())
                        .username(project.getOwner().getUsername())
                        .email(project.getOwner().getEmail())
                        .build())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    /**
     * 내 프로젝트 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        log.info("GET /projects - Fetching my projects");
        List<Project> projects = projectService.getMyProjects();
        List<ProjectResponse> responses = projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 프로젝트 생성
     */
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequest request) {
        log.info("POST /projects - Creating project: name={}", request.getName());

        try {
            Project project = projectService.createProject(request.getName(), request.getDescription());
            ProjectResponse response = toProjectResponse(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Failed to create project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 프로젝트 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        log.info("GET /projects/{} - Fetching project details", id);

        try {
            Project project = projectService.getProject(id);
            ProjectResponse response = toProjectResponse(project);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to fetch project: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 프로젝트 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request
    ) {
        log.info("PUT /projects/{} - Updating project", id);

        try {
            Project project = projectService.updateProject(id, request.getName(), request.getDescription());
            ProjectResponse response = toProjectResponse(project);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to update project: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 프로젝트 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        log.info("DELETE /projects/{} - Deleting project", id);

        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Failed to delete project: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 프로젝트 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(@RequestParam String keyword) {
        log.info("GET /projects/search?keyword={}", keyword);
        List<Project> projects = projectService.searchProjects(keyword);
        List<ProjectResponse> responses = projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}