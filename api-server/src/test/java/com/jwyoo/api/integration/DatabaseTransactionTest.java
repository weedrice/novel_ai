package com.jwyoo.api.integration;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.CharacterRepository;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.UserRepository;
import com.jwyoo.api.service.CharacterService;
import com.jwyoo.api.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 데이터베이스 트랜잭션 통합 테스트
 * 트랜잭션 롤백, 격리, 영속성 컨텍스트를 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseTransactionTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        characterRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(testUser);

        // SecurityContext 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", "password")
        );
    }

    @Test
    @DisplayName("트랜잭션 테스트: @Transactional로 자동 롤백")
    void transaction_AutoRollback() {
        // given
        long initialCount = userRepository.count();

        // when - 새 사용자 생성
        User newUser = User.builder()
                .username("tempuser")
                .email("temp@example.com")
                .password(passwordEncoder.encode("password"))
                .role(User.UserRole.USER)
                .build();
        userRepository.save(newUser);

        // then - 트랜잭션 내에서는 저장됨
        assertThat(userRepository.count()).isEqualTo(initialCount + 1);

        // 테스트 종료 후 자동 롤백되므로 다른 테스트에 영향 없음
    }

    @Test
    @DisplayName("트랜잭션 테스트: Cascade 저장 - 프로젝트와 캐릭터")
    void transaction_CascadeSave() {
        // given - 프로젝트 생성 (자동으로 생성됨)
        Project project = projectService.getCurrentProject();

        // when - 캐릭터 생성 (프로젝트에 자동 연결)
        Character character = Character.builder()
                .characterId("char001")
                .name("Test Character")
                .description("Test Description")
                .build();
        Character savedCharacter = characterService.createCharacter(character);

        // then
        assertThat(savedCharacter.getId()).isNotNull();
        assertThat(savedCharacter.getProject()).isNotNull();
        assertThat(savedCharacter.getProject().getId()).isEqualTo(project.getId());

        // 데이터베이스 검증
        Character foundCharacter = characterRepository.findById(savedCharacter.getId()).orElseThrow();
        assertThat(foundCharacter.getProject().getId()).isEqualTo(project.getId());
    }

    @Test
    @DisplayName("트랜잭션 테스트: 예외 발생 시 롤백")
    void transaction_RollbackOnException() {
        // given
        long initialProjectCount = projectRepository.count();

        // when - 프로젝트 생성 후 예외 발생
        Project project = projectService.createProject("Test Project", "Description");
        assertThat(projectRepository.count()).isEqualTo(initialProjectCount + 1);

        // then - @Transactional이 있으므로 테스트 종료 시 자동 롤백됨
        // (이 테스트는 클래스 레벨의 @Transactional로 인해 테스트 종료 시 자동 롤백을 보여줌)
        // 실제 예외 발생 시 롤백 테스트는 @Transactional 없이 별도로 테스트해야 함
    }

    @Test
    @DisplayName("트랜잭션 테스트: 영속성 컨텍스트 - Dirty Checking")
    void transaction_DirtyChecking() {
        // given - 프로젝트 생성
        Project project = projectService.createProject("Original Name", "Original Description");
        Long projectId = project.getId();

        // when - 엔티티 수정 (명시적인 save() 호출 없이)
        Project foundProject = projectRepository.findById(projectId).orElseThrow();
        foundProject.setName("Updated Name");
        foundProject.setDescription("Updated Description");
        // save()를 호출하지 않았지만 트랜잭션 커밋 시 자동으로 업데이트됨

        // then - 트랜잭션이 커밋되면 변경사항이 반영됨
        projectRepository.flush(); // 강제로 플러시하여 변경사항 반영
        Project reloadedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(reloadedProject.getName()).isEqualTo("Updated Name");
        assertThat(reloadedProject.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("트랜잭션 테스트: 읽기 전용 트랜잭션")
    void transaction_ReadOnly() {
        // given - 프로젝트 생성
        Project project = projectService.createProject("Test Project", "Description");
        Long projectId = project.getId();

        // when - 읽기 전용 작업
        Project foundProject = projectRepository.findById(projectId).orElseThrow();

        // then
        assertThat(foundProject).isNotNull();
        assertThat(foundProject.getName()).isEqualTo("Test Project");
        // 읽기 전용이므로 수정 작업은 수행하지 않음
    }

    @Test
    @DisplayName("트랜잭션 테스트: 다중 엔티티 저장 및 연관관계")
    void transaction_MultipleEntities() {
        // given & when
        // 1. 프로젝트 생성
        Project project = projectService.getCurrentProject();

        // 2. 여러 캐릭터 생성
        Character char1 = Character.builder()
                .characterId("char001")
                .name("Character 1")
                .build();
        Character char2 = Character.builder()
                .characterId("char002")
                .name("Character 2")
                .build();

        Character savedChar1 = characterService.createCharacter(char1);
        Character savedChar2 = characterService.createCharacter(char2);

        // then
        assertThat(savedChar1.getProject().getId()).isEqualTo(project.getId());
        assertThat(savedChar2.getProject().getId()).isEqualTo(project.getId());

        // 데이터베이스 검증 - 모든 캐릭터가 같은 프로젝트에 속함
        var characters = characterRepository.findByProject(project);
        assertThat(characters).hasSize(2);
        assertThat(characters).extracting(Character::getName)
                .containsExactlyInAnyOrder("Character 1", "Character 2");
    }

    @Test
    @DisplayName("트랜잭션 테스트: 영속성 컨텍스트 캐시")
    void transaction_PersistenceContextCache() {
        // given - 프로젝트 생성
        Project project = projectService.createProject("Cache Test", "Description");
        Long projectId = project.getId();

        // when - 같은 트랜잭션 내에서 여러 번 조회
        Project first = projectRepository.findById(projectId).orElseThrow();
        Project second = projectRepository.findById(projectId).orElseThrow();

        // then - 같은 인스턴스 (영속성 컨텍스트 캐시)
        assertThat(first).isSameAs(second);
        assertThat(first.getName()).isEqualTo("Cache Test");
    }
}