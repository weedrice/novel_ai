package com.jwyoo.api.service;

import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 프로젝트 서비스
 * 프로젝트 생성, 조회, 수정, 삭제 등의 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자 조회
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 현재 사용자의 기본 프로젝트 조회
     * (첫 번째 프로젝트를 기본 프로젝트로 사용, 없으면 자동 생성)
     *
     * @return 기본 프로젝트
     */
    @Transactional
    public Project getCurrentProject() {
        User currentUser = getCurrentUser();
        List<Project> projects = projectRepository.findByOwnerOrderByCreatedAtDesc(currentUser);

        if (projects.isEmpty()) {
            // 프로젝트가 없으면 기본 프로젝트 생성
            log.info("No projects found for user: {}. Creating default project.", currentUser.getUsername());
            return createProject("기본 프로젝트", "자동 생성된 기본 프로젝트");
        }

        // 첫 번째 프로젝트를 기본 프로젝트로 사용
        Project defaultProject = projects.get(0);
        log.debug("Using default project: id={}, name={}", defaultProject.getId(), defaultProject.getName());
        return defaultProject;
    }

    /**
     * 프로젝트 생성
     *
     * @param name 프로젝트 이름
     * @param description 프로젝트 설명
     * @return 생성된 프로젝트
     */
    @Transactional
    public Project createProject(String name, String description) {
        User currentUser = getCurrentUser();
        log.info("Creating project: name={}, owner={}", name, currentUser.getUsername());

        Project project = Project.builder()
                .name(name)
                .description(description)
                .owner(currentUser)
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully: id={}", savedProject.getId());

        return savedProject;
    }

    /**
     * 내 프로젝트 목록 조회
     *
     * @return 프로젝트 목록
     */
    @Transactional(readOnly = true)
    public List<Project> getMyProjects() {
        User currentUser = getCurrentUser();
        log.info("Fetching projects for user: {}", currentUser.getUsername());

        return projectRepository.findByOwnerOrderByCreatedAtDesc(currentUser);
    }

    /**
     * 프로젝트 상세 조회
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트
     * @throws RuntimeException 프로젝트를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public Project getProject(Long projectId) {
        User currentUser = getCurrentUser();
        log.info("Fetching project: projectId={}, user={}", projectId, currentUser.getUsername());

        return projectRepository.findByIdAndOwner(projectId, currentUser)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없거나 접근 권한이 없습니다."));
    }

    /**
     * 프로젝트 수정
     *
     * @param projectId 프로젝트 ID
     * @param name 새 이름
     * @param description 새 설명
     * @return 수정된 프로젝트
     * @throws RuntimeException 프로젝트를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public Project updateProject(Long projectId, String name, String description) {
        User currentUser = getCurrentUser();
        log.info("Updating project: projectId={}, user={}", projectId, currentUser.getUsername());

        Project project = projectRepository.findByIdAndOwner(projectId, currentUser)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없거나 접근 권한이 없습니다."));

        if (name != null && !name.isBlank()) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully: id={}", updatedProject.getId());

        return updatedProject;
    }

    /**
     * 프로젝트 삭제
     *
     * @param projectId 프로젝트 ID
     * @throws RuntimeException 프로젝트를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public void deleteProject(Long projectId) {
        User currentUser = getCurrentUser();
        log.info("Deleting project: projectId={}, user={}", projectId, currentUser.getUsername());

        Project project = projectRepository.findByIdAndOwner(projectId, currentUser)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없거나 접근 권한이 없습니다."));

        projectRepository.delete(project);
        log.info("Project deleted successfully: id={}", projectId);
    }

    /**
     * 프로젝트 검색
     *
     * @param keyword 검색 키워드
     * @return 검색 결과
     */
    @Transactional(readOnly = true)
    public List<Project> searchProjects(String keyword) {
        User currentUser = getCurrentUser();
        log.info("Searching projects: keyword={}, user={}", keyword, currentUser.getUsername());

        return projectRepository.findByOwnerAndNameContainingIgnoreCase(currentUser, keyword);
    }
}