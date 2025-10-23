package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    List<Relationship> findByFromCharacterId(Long fromCharacterId);
    List<Relationship> findByToCharacterId(Long toCharacterId);

    // 프로젝트별 조회 (Character를 통한 간접 필터링)
    @Query("SELECT r FROM Relationship r WHERE r.fromCharacter.project = :project")
    List<Relationship> findByProject(@Param("project") Project project);

    @Query("SELECT r FROM Relationship r WHERE r.id = :id AND r.fromCharacter.project = :project")
    Optional<Relationship> findByIdAndProject(@Param("id") Long id, @Param("project") Project project);

    @Query("SELECT r FROM Relationship r WHERE r.fromCharacter.id = :characterId AND r.fromCharacter.project = :project")
    List<Relationship> findByFromCharacterIdAndProject(@Param("characterId") Long characterId, @Param("project") Project project);

    @Query("SELECT r FROM Relationship r WHERE r.toCharacter.id = :characterId AND r.toCharacter.project = :project")
    List<Relationship> findByToCharacterIdAndProject(@Param("characterId") Long characterId, @Param("project") Project project);
}