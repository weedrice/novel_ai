package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByCharacterId(String characterId);
    boolean existsByCharacterId(String characterId);

    // 프로젝트별 조회
    List<Character> findByProject(Project project);
    Optional<Character> findByIdAndProject(Long id, Project project);
    Optional<Character> findByCharacterIdAndProject(String characterId, Project project);
    boolean existsByCharacterIdAndProject(String characterId, Project project);

    /**
     * N+1 문제 해결을 위한 IN 쿼리 지원
     * SceneService.getParticipants()에서 사용
     */
    List<Character> findByCharacterIdIn(Collection<String> characterIds);
    List<Character> findByCharacterIdInAndProject(Collection<String> characterIds, Project project);
}