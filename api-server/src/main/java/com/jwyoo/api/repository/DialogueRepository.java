package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Long> {
    List<Dialogue> findBySceneIdOrderByDialogueOrderAsc(Long sceneId);
    List<Dialogue> findByCharacterIdOrderByCreatedAtDesc(Long characterId);
}