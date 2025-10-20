package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    List<Relationship> findByFromCharacterId(Long fromCharacterId);
    List<Relationship> findByToCharacterId(Long toCharacterId);
}