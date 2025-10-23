package com.jwyoo.api.controller;

import com.jwyoo.api.dto.ProjectRequest;
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
     * 내 프로젝트 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Project>> getMyProjects() {
        log.info("GET /projects - Fetching my projects");
        List<Project> projects = projectService.getMyProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * 프로젝트 생성
     */
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequest request) {
        log.info("POST /projects - Creating project: name={}", request.getName());

        try {
            Project project = projectService.createProject(request.getName(), request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
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
            return ResponseEntity.ok(project);
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
            return ResponseEntity.ok(project);
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
    public ResponseEntity<List<Project>> searchProjects(@RequestParam String keyword) {
        log.info("GET /projects/search?keyword={}", keyword);
        List<Project> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(projects);
    }
}