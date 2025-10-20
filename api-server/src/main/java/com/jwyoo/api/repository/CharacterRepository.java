package com.jwyoo.api.repository;

import com.jwyoo.api.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByCharacterId(String characterId);
    boolean existsByCharacterId(String characterId);
}