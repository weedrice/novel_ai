package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, Long> {

    /**
     * 프로젝트별 개념 조회
     */
    List<Concept> findByProjectId(Long projectId);

    /**
     * 프로젝트별 + 유형별 개념 조회
     */
    List<Concept> findByProjectIdAndType(Long projectId, String type);

    /**
     * 에피소드별 개념 조회
     */
    List<Concept> findByEpisodeId(Long episodeId);

    /**
     * 이름으로 개념 조회 (프로젝트 내)
     */
    Optional<Concept> findByProjectIdAndName(Long projectId, String name);

    /**
     * 유형별 개념 조회 (프로젝트 내)
     */
    @Query("SELECT c FROM Concept c WHERE c.project.id = :projectId AND c.type = :type ORDER BY c.importance DESC")
    List<Concept> findByProjectIdAndTypeOrderByImportanceDesc(@Param("projectId") Long projectId, @Param("type") String type);

    /**
     * 중요한 개념 상위 N개 조회
     */
    @Query("SELECT c FROM Concept c WHERE c.project.id = :projectId ORDER BY c.importance DESC")
    List<Concept> findTopConceptsByImportance(@Param("projectId") Long projectId);

    /**
     * 개념 이름 검색 (LIKE)
     */
    @Query("SELECT c FROM Concept c WHERE c.project.id = :projectId AND c.name LIKE %:keyword%")
    List<Concept> searchByName(@Param("projectId") Long projectId, @Param("keyword") String keyword);

    /**
     * 개념 설명 검색 (LIKE)
     */
    @Query("SELECT c FROM Concept c WHERE c.project.id = :projectId AND c.description LIKE %:keyword%")
    List<Concept> searchByDescription(@Param("projectId") Long projectId, @Param("keyword") String keyword);
}
