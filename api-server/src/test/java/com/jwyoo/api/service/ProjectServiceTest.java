package com.jwyoo.api.service;

import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.entity.User.UserRole;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ProjectService 단위 테스트
 * 프로젝트 CRUD 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    private User testUser;
    private Project testProject;

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

        // SecurityContext 모킹 설정
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    @Test
    @DisplayName("현재 프로젝트 조회 성공 - 기존 프로젝트 존재")
    void getCurrentProject_Success_ExistingProject() {
        // given
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findByOwnerOrderByCreatedAtDesc(testUser)).thenReturn(projects);

        // when
        Project result = projectService.getCurrentProject();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Project");

        verify(projectRepository).findByOwnerOrderByCreatedAtDesc(testUser);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("현재 프로젝트 조회 성공 - 프로젝트 없으면 자동 생성")
    void getCurrentProject_Success_AutoCreate() {
        // given
        when(projectRepository.findByOwnerOrderByCreatedAtDesc(testUser)).thenReturn(new ArrayList<>());
        Project newProject = Project.builder()
                .id(2L)
                .name("기본 프로젝트")
                .description("자동 생성된 기본 프로젝트")
                .owner(testUser)
                .build();
        when(projectRepository.save(any(Project.class))).thenReturn(newProject);

        // when
        Project result = projectService.getCurrentProject();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("기본 프로젝트");

        verify(projectRepository).findByOwnerOrderByCreatedAtDesc(testUser);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    void createProject_Success() {
        // given
        String name = "New Project";
        String description = "New Description";
        Project savedProject = Project.builder()
                .id(2L)
                .name(name)
                .description(description)
                .owner(testUser)
                .build();
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // when
        Project result = projectService.createProject(name, description);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getOwner()).isEqualTo(testUser);

        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 성공")
    void getMyProjects_Success() {
        // given
        Project project2 = Project.builder()
                .id(2L)
                .name("Project 2")
                .description("Description 2")
                .owner(testUser)
                .build();
        List<Project> projects = Arrays.asList(testProject, project2);
        when(projectRepository.findByOwnerOrderByCreatedAtDesc(testUser)).thenReturn(projects);

        // when
        List<Project> result = projectService.getMyProjects();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Test Project");
        assertThat(result.get(1).getName()).isEqualTo("Project 2");

        verify(projectRepository).findByOwnerOrderByCreatedAtDesc(testUser);
    }

    @Test
    @DisplayName("프로젝트 상세 조회 성공")
    void getProject_Success() {
        // given
        Long projectId = 1L;
        when(projectRepository.findByIdAndOwner(projectId, testUser)).thenReturn(Optional.of(testProject));

        // when
        Project result = projectService.getProject(projectId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(projectId);
        assertThat(result.getName()).isEqualTo("Test Project");

        verify(projectRepository).findByIdAndOwner(projectId, testUser);
    }

    @Test
    @DisplayName("프로젝트 상세 조회 실패 - 존재하지 않거나 권한 없음")
    void getProject_Failure_NotFoundOrNoPermission() {
        // given
        Long projectId = 999L;
        when(projectRepository.findByIdAndOwner(projectId, testUser)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getProject(projectId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없거나 접근 권한이 없습니다");

        verify(projectRepository).findByIdAndOwner(projectId, testUser);
    }

    @Test
    @DisplayName("프로젝트 수정 성공")
    void updateProject_Success() {
        // given
        Long projectId = 1L;
        String newName = "Updated Project";
        String newDescription = "Updated Description";
        when(projectRepository.findByIdAndOwner(projectId, testUser)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // when
        Project result = projectService.updateProject(projectId, newName, newDescription);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(newDescription);

        verify(projectRepository).findByIdAndOwner(projectId, testUser);
        verify(projectRepository).save(testProject);
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 존재하지 않거나 권한 없음")
    void updateProject_Failure_NotFoundOrNoPermission() {
        // given
        Long projectId = 999L;
        when(projectRepository.findByIdAndOwner(projectId, testUser)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.updateProject(projectId, "New Name", "New Desc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없거나 접근 권한이 없습니다");

        verify(projectRepository).findByIdAndOwner(projectId, testUser);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_Success() {
        // given
        Long projectId = 1L;
        when(projectRepository.findByIdAndOwner(projectId, testUser)).thenReturn(Optional.of(testProject));
        doNothing().when(projectRepository).delete(testProject);

        // when
        projectService.deleteProject(projectId);

        // then
        verify(projectRepository).findByIdAndOwner(projectId, testUser);
        verify(projectRepository).delete(testProject);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 존재하지 않거나 권한 없음")
    void deleteProject_Failure_NotFoundOrNoPermission() {
        // given
        Long projectId = 999L;
        when(projectRepository.findByIdAndOwner(projectId, testUser)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.deleteProject(projectId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없거나 접근 권한이 없습니다");

        verify(projectRepository).findByIdAndOwner(projectId, testUser);
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 검색 성공")
    void searchProjects_Success() {
        // given
        String keyword = "Test";
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findByOwnerAndNameContainingIgnoreCase(testUser, keyword)).thenReturn(projects);

        // when
        List<Project> result = projectService.searchProjects(keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains(keyword);

        verify(projectRepository).findByOwnerAndNameContainingIgnoreCase(testUser, keyword);
    }

    @Test
    @DisplayName("getCurrentUser 실패 - 사용자를 찾을 수 없음")
    void getCurrentUser_Failure_UserNotFound() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getCurrentProject())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userRepository).findByUsername("testuser");
    }
}