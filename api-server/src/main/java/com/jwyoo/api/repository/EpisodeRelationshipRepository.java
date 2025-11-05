package com.jwyoo.api.repository;

import com.jwyoo.api.entity.EpisodeRelationship;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRelationshipRepository extends JpaRepository<EpisodeRelationship, Long> {

    /**
     * 특정 에피소드의 모든 관계 조회
     */
    List<EpisodeRelationship> findByEpisodeId(Long episodeId);

    /**
     * 특정 에피소드와 캐릭터 ID로 관계 조회
     */
    @Query("SELECT er FROM EpisodeRelationship er WHERE er.episode.id = :episodeId " +
           "AND (er.fromCharacter.id = :characterId OR er.toCharacter.id = :characterId)")
    List<EpisodeRelationship> findByEpisodeIdAndCharacterId(
        @Param("episodeId") Long episodeId,
        @Param("characterId") Long characterId
    );

    /**
     * 프로젝트별 에피소드 관계 조회
     */
    @Query("SELECT er FROM EpisodeRelationship er WHERE er.episode.project = :project")
    List<EpisodeRelationship> findByProject(@Param("project") Project project);

    /**
     * 특정 에피소드와 두 캐릭터 간의 관계 조회
     */
    @Query("SELECT er FROM EpisodeRelationship er WHERE er.episode.id = :episodeId " +
           "AND ((er.fromCharacter.id = :char1Id AND er.toCharacter.id = :char2Id) " +
           "OR (er.fromCharacter.id = :char2Id AND er.toCharacter.id = :char1Id))")
    Optional<EpisodeRelationship> findByEpisodeAndCharacters(
        @Param("episodeId") Long episodeId,
        @Param("char1Id") Long char1Id,
        @Param("char2Id") Long char2Id
    );

    /**
     * 두 캐릭터 간의 모든 에피소드 관계 조회 (시간순)
     */
    @Query("SELECT er FROM EpisodeRelationship er " +
           "WHERE ((er.fromCharacter.id = :char1Id AND er.toCharacter.id = :char2Id) " +
           "OR (er.fromCharacter.id = :char2Id AND er.toCharacter.id = :char1Id)) " +
           "ORDER BY er.episode.episodeOrder ASC")
    List<EpisodeRelationship> findRelationshipHistory(
        @Param("char1Id") Long char1Id,
        @Param("char2Id") Long char2Id
    );

    /**
     * 프로젝트와 에피소드로 조회
     */
    @Query("SELECT er FROM EpisodeRelationship er WHERE er.episode.id = :episodeId " +
           "AND er.episode.project = :project")
    List<EpisodeRelationship> findByEpisodeIdAndProject(
        @Param("episodeId") Long episodeId,
        @Param("project") Project project
    );
}
