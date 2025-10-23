package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로젝트 Repository
 * 프로젝트 데이터 접근 계층
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 특정 사용자의 모든 프로젝트 조회
     */
    List<Project> findByOwner(User owner);

    /**
     * 특정 사용자의 프로젝트를 생성일 역순으로 조회
     */
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * 프로젝트 ID와 소유자로 프로젝트 조회
     * 권한 확인 용도
     */
    Optional<Project> findByIdAndOwner(Long id, User owner);

    /**
     * 프로젝트 이름으로 검색 (소유자 기준)
     */
    List<Project> findByOwnerAndNameContainingIgnoreCase(User owner, String name);
}