package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Script 엔티티에 대한 데이터 접근 레이어
 */
@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {

    /**
     * 상태별 스크립트 목록 조회
     */
    List<Script> findByStatus(String status);

    /**
     * 생성일 기준 최신순 정렬
     */
    List<Script> findAllByOrderByCreatedAtDesc();

    /**
     * 제목으로 검색
     */
    List<Script> findByTitleContainingIgnoreCase(String keyword);

    // 프로젝트별 조회
    List<Script> findByProjectOrderByCreatedAtDesc(Project project);
    Optional<Script> findByIdAndProject(Long id, Project project);
    List<Script> findByProjectAndStatus(Project project, String status);
    List<Script> findByProjectAndTitleContainingIgnoreCase(Project project, String keyword);
}