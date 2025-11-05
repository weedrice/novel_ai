package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Long> {

    @Query("SELECT d FROM Dialogue d LEFT JOIN FETCH d.character WHERE d.scene.id = :sceneId ORDER BY d.dialogueOrder ASC")
    List<Dialogue> findBySceneIdOrderByDialogueOrderAsc(@Param("sceneId") Long sceneId);

    List<Dialogue> findByCharacterIdOrderByCreatedAtDesc(Long characterId);

    // 프로젝트별 조회 (Scene → Episode → Project를 통한 간접 필터링)
    @Query("SELECT d FROM Dialogue d WHERE d.scene.episode.project = :project ORDER BY d.scene.episode.episodeOrder, d.scene.sceneNumber, d.dialogueOrder")
    List<Dialogue> findByProject(@Param("project") Project project);

    @Query("SELECT d FROM Dialogue d WHERE d.id = :id AND d.scene.episode.project = :project")
    Optional<Dialogue> findByIdAndProject(@Param("id") Long id, @Param("project") Project project);

    @Query("SELECT d FROM Dialogue d WHERE d.character.id = :characterId AND d.character.project = :project ORDER BY d.createdAt DESC")
    List<Dialogue> findByCharacterIdAndProject(@Param("characterId") Long characterId, @Param("project") Project project);

    @Query("SELECT d FROM Dialogue d WHERE d.scene.id = :sceneId AND d.scene.episode.project = :project ORDER BY d.dialogueOrder ASC")
    List<Dialogue> findBySceneIdAndProject(@Param("sceneId") Long sceneId, @Param("project") Project project);

    /**
     * Task 105: 대사 검색 (텍스트, 캐릭터, 에피소드, 장면 필터)
     * 프로젝트별 필터링 포함
     */
    @Query("SELECT DISTINCT d FROM Dialogue d " +
            "WHERE d.scene.episode.project = :project " +
            "AND (:query IS NULL OR LOWER(d.text) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:characterId IS NULL OR d.character.id = :characterId) " +
            "AND (:episodeId IS NULL OR d.scene.episode.id = :episodeId) " +
            "AND (:sceneId IS NULL OR d.scene.id = :sceneId) " +
            "ORDER BY d.scene.episode.episodeOrder, d.scene.sceneNumber, d.dialogueOrder")
    List<Dialogue> searchDialogues(
            @Param("project") Project project,
            @Param("query") String query,
            @Param("characterId") Long characterId,
            @Param("episodeId") Long episodeId,
            @Param("sceneId") Long sceneId
    );
}