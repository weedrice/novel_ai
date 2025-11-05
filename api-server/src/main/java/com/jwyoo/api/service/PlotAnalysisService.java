package com.jwyoo.api.service;

import com.jwyoo.api.dto.PlotAnalysisDto;
import com.jwyoo.api.dto.PlotAnalysisDto.CharacterFrequency;
import com.jwyoo.api.dto.PlotAnalysisDto.SceneAnalysis;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.entity.Dialogue;
import com.jwyoo.api.entity.Episode;
import com.jwyoo.api.entity.Scene;
import com.jwyoo.api.repository.DialogueRepository;
import com.jwyoo.api.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Task 99: 플롯 구조 분석 서비스
 * 에피소드별로 장면, 대사, 캐릭터를 분석하여 스토리 아크 데이터를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlotAnalysisService {

    private final EpisodeRepository episodeRepository;
    private final DialogueRepository dialogueRepository;
    private final ProjectService projectService;

    /**
     * 에피소드 플롯 분석
     */
    public PlotAnalysisDto analyzeEpisode(Long episodeId) {
        log.info("Analyzing plot for episode: {}", episodeId);

        // 에피소드와 장면들을 함께 조회 (N+1 문제 방지)
        Episode episode = episodeRepository.findWithScenesByIdAndProject(
                episodeId,
                projectService.getCurrentProject()
        ).orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));

        List<Scene> scenes = episode.getScenes();
        scenes.sort(Comparator.comparingInt(Scene::getSceneNumber));

        // 장면별 분석
        List<SceneAnalysis> sceneAnalyses = new ArrayList<>();
        int cumulativeDialogues = 0;
        Map<String, Set<Long>> characterSceneMap = new HashMap<>(); // 캐릭터별 등장 장면
        Map<String, Integer> characterDialogueCount = new HashMap<>(); // 캐릭터별 대사 수

        for (Scene scene : scenes) {
            List<Dialogue> dialogues = dialogueRepository.findBySceneIdOrderByDialogueOrderAsc(scene.getId());
            cumulativeDialogues += dialogues.size();

            // 참여 캐릭터 추출
            Set<Character> participants = dialogues.stream()
                    .map(Dialogue::getCharacter)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<String> participantNames = participants.stream()
                    .map(Character::getName)
                    .collect(Collectors.toList());

            // 캐릭터별 통계 업데이트
            for (Character character : participants) {
                characterSceneMap.computeIfAbsent(character.getCharacterId(), k -> new HashSet<>())
                        .add(scene.getId());

                long dialoguesByCharacter = dialogues.stream()
                        .filter(d -> d.getCharacter() != null && d.getCharacter().getCharacterId().equals(character.getCharacterId()))
                        .count();
                characterDialogueCount.merge(character.getCharacterId(), (int) dialoguesByCharacter, Integer::sum);
            }

            // 갈등 강도 계산
            double tensionLevel = calculateTensionLevel(scene, dialogues, participants.size());

            SceneAnalysis sceneAnalysis = SceneAnalysis.builder()
                    .sceneId(scene.getId())
                    .sceneTitle(scene.getDescription())
                    .location(scene.getLocation())
                    .mood(scene.getMood())
                    .sceneNumber(scene.getSceneNumber())
                    .dialogueCount(dialogues.size())
                    .tensionLevel(tensionLevel)
                    .participants(participantNames)
                    .cumulativeDialogueCount(cumulativeDialogues)
                    .build();

            sceneAnalyses.add(sceneAnalysis);
        }

        // 캐릭터별 등장 빈도 계산
        List<CharacterFrequency> characterFrequencies = calculateCharacterFrequencies(
                characterSceneMap,
                characterDialogueCount,
                scenes.size()
        );

        // 평균 갈등 강도
        double averageTension = sceneAnalyses.stream()
                .mapToDouble(SceneAnalysis::getTensionLevel)
                .average()
                .orElse(0.0);

        PlotAnalysisDto result = PlotAnalysisDto.builder()
                .episodeId(episode.getId())
                .episodeTitle(episode.getTitle())
                .episodeDescription(episode.getDescription())
                .scenes(sceneAnalyses)
                .characterFrequencies(characterFrequencies)
                .totalScenes(scenes.size())
                .totalDialogues(cumulativeDialogues)
                .averageTensionLevel(averageTension)
                .build();

        log.info("Plot analysis complete: {} scenes, {} dialogues, avg tension: {:.2f}",
                result.getTotalScenes(), result.getTotalDialogues(), result.getAverageTensionLevel());

        return result;
    }

    /**
     * 갈등 강도 계산 (0.0 ~ 1.0)
     * 요소:
     * - 대사 수: 많을수록 강도 증가
     * - 참여 캐릭터 수: 많을수록 강도 증가
     * - 분위기 키워드: 긴장, 갈등 등의 키워드가 있으면 강도 증가
     */
    private double calculateTensionLevel(Scene scene, List<Dialogue> dialogues, int participantCount) {
        double tension = 0.0;

        // 대사 수 기여도 (0~0.4)
        double dialogueContribution = Math.min(dialogues.size() / 50.0, 0.4);

        // 참여 캐릭터 수 기여도 (0~0.3)
        double participantContribution = Math.min(participantCount / 5.0, 0.3);

        // 분위기 키워드 기여도 (0~0.3)
        double moodContribution = 0.0;
        if (scene.getMood() != null) {
            String mood = scene.getMood().toLowerCase();
            if (mood.contains("긴장") || mood.contains("갈등") || mood.contains("위기")) {
                moodContribution = 0.3;
            } else if (mood.contains("격렬") || mood.contains("치열") || mood.contains("심각")) {
                moodContribution = 0.25;
            } else if (mood.contains("불안") || mood.contains("혼란")) {
                moodContribution = 0.2;
            } else if (mood.contains("평화") || mood.contains("평온") || mood.contains("안정")) {
                moodContribution = 0.05;
            } else {
                moodContribution = 0.15; // 기본값
            }
        }

        tension = dialogueContribution + participantContribution + moodContribution;

        return Math.min(tension, 1.0); // 최대 1.0으로 제한
    }

    /**
     * 캐릭터별 등장 빈도 계산
     */
    private List<CharacterFrequency> calculateCharacterFrequencies(
            Map<String, Set<Long>> characterSceneMap,
            Map<String, Integer> characterDialogueCount,
            int totalScenes
    ) {
        List<CharacterFrequency> frequencies = new ArrayList<>();

        for (Map.Entry<String, Set<Long>> entry : characterSceneMap.entrySet()) {
            String characterId = entry.getKey();
            int appearanceCount = entry.getValue().size();
            int dialogueCount = characterDialogueCount.getOrDefault(characterId, 0);
            double appearanceRate = totalScenes > 0 ? (appearanceCount * 100.0 / totalScenes) : 0.0;

            // 캐릭터 이름 조회 (첫 번째 장면에서)
            // TODO: 나중에 Character 엔티티를 직접 조회하도록 개선
            String characterName = characterId; // 일단 characterId를 이름으로 사용

            CharacterFrequency frequency = CharacterFrequency.builder()
                    .characterId(characterId)
                    .characterName(characterName)
                    .appearanceCount(appearanceCount)
                    .dialogueCount(dialogueCount)
                    .appearanceRate(Math.round(appearanceRate * 100.0) / 100.0) // 소수점 2자리
                    .build();

            frequencies.add(frequency);
        }

        // 등장 빈도 순으로 정렬
        frequencies.sort((a, b) -> Integer.compare(b.getAppearanceCount(), a.getAppearanceCount()));

        return frequencies;
    }
}
