package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Long> {

    @Query("SELECT d FROM Dialogue d LEFT JOIN FETCH d.character WHERE d.scene.id = :sceneId ORDER BY d.dialogueOrder ASC")
    List<Dialogue> findBySceneIdOrderByDialogueOrderAsc(@Param("sceneId") Long sceneId);

    List<Dialogue> findByCharacterIdOrderByCreatedAtDesc(Long characterId);
}