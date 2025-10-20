package com.jwyoo.api.controller;

import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Relationship;
import com.jwyoo.api.service.CharacterService;
import com.jwyoo.api.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;
    private final CharacterService characterService;

    @GetMapping
    public List<Relationship> getAllRelationships() {
        return relationshipService.getAllRelationships();
    }

    @GetMapping("/{id}")
    public Relationship getRelationshipById(@PathVariable Long id) {
        return relationshipService.getRelationshipById(id);
    }

    @GetMapping("/character/{characterId}")
    public List<Relationship> getRelationshipsForCharacter(@PathVariable Long characterId) {
        return relationshipService.getAllRelationshipsForCharacter(characterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Relationship createRelationship(@RequestBody Relationship relationship) {
        return relationshipService.createRelationship(relationship);
    }

    @PutMapping("/{id}")
    public Relationship updateRelationship(@PathVariable Long id, @RequestBody Relationship relationship) {
        return relationshipService.updateRelationship(id, relationship);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRelationship(@PathVariable Long id) {
        relationshipService.deleteRelationship(id);
    }

    /**
     * 그래프 시각화를 위한 노드-엣지 데이터 반환
     * 형식: { nodes: [...], edges: [...] }
     */
    @GetMapping("/graph")
    public Map<String, Object> getGraphData() {
        List<Character> characters = characterService.getAllCharacters();
        List<Relationship> relationships = relationshipService.getAllRelationships();

        // 노드 데이터 생성 (캐릭터)
        List<Map<String, Object>> nodes = characters.stream()
                .map(character -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", String.valueOf(character.getId()));
                    node.put("label", character.getName());
                    node.put("characterId", character.getCharacterId());
                    node.put("description", character.getDescription());
                    node.put("personality", character.getPersonality());
                    return node;
                })
                .collect(Collectors.toList());

        // 엣지 데이터 생성 (관계)
        List<Map<String, Object>> edges = relationships.stream()
                .map(relationship -> {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("id", String.valueOf(relationship.getId()));
                    edge.put("source", String.valueOf(relationship.getFromCharacter().getId()));
                    edge.put("target", String.valueOf(relationship.getToCharacter().getId()));
                    edge.put("label", relationship.getRelationType());
                    edge.put("closeness", relationship.getCloseness());
                    edge.put("description", relationship.getDescription());
                    return edge;
                })
                .collect(Collectors.toList());

        Map<String, Object> graphData = new HashMap<>();
        graphData.put("nodes", nodes);
        graphData.put("edges", edges);

        return graphData;
    }
}