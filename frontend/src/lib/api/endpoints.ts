/**
 * API 엔드포인트 정의
 * 모든 API 호출을 타입 안전하게 관리
 */

import { apiClient } from './client';
import type {
  Episode,
  Scene,
  Dialogue,
  Character,
  Relationship,
  API,
  GraphData,
} from '@/types';

/**
 * Episode API
 */
export const episodesApi = {
  /**
   * 모든 에피소드 조회
   */
  getAll: () => apiClient.get<Episode[]>('/episodes'),

  /**
   * 특정 에피소드 조회
   */
  getById: (id: number) => apiClient.get<Episode>(`/episodes/${id}`),

  /**
   * 에피소드 생성
   */
  create: (data: Omit<Episode, 'id'>) => apiClient.post<Episode>('/episodes', data),

  /**
   * 에피소드 수정
   */
  update: (id: number, data: Partial<Episode>) =>
    apiClient.put<Episode>(`/episodes/${id}`, data),

  /**
   * 에피소드 삭제
   */
  delete: (id: number) => apiClient.delete<void>(`/episodes/${id}`),
};

/**
 * Scene API
 */
export const scenesApi = {
  /**
   * 모든 장면 조회
   */
  getAll: () => apiClient.get<Scene[]>('/scenes'),

  /**
   * 특정 장면 조회
   */
  getById: (id: number) => apiClient.get<Scene>(`/scenes/${id}`),

  /**
   * 에피소드별 장면 조회
   */
  getByEpisode: (episodeId: number) =>
    apiClient.get<Scene[]>(`/scenes/episode/${episodeId}`),

  /**
   * 장면 생성
   */
  create: (data: Omit<Scene, 'id'>) => apiClient.post<Scene>('/scenes', data),

  /**
   * 장면 수정
   */
  update: (id: number, data: Partial<Scene>) =>
    apiClient.put<Scene>(`/scenes/${id}`, data),

  /**
   * 장면 삭제
   */
  delete: (id: number) => apiClient.delete<void>(`/scenes/${id}`),

  /**
   * 장면의 대사 목록 조회
   */
  getDialogues: (sceneId: number) =>
    apiClient.get<Dialogue[]>(`/scenes/${sceneId}/dialogues`),

  /**
   * 시나리오 생성
   */
  generateScenario: (sceneId: number, request: API.GenerateScenarioRequest) =>
    apiClient.post<API.GenerateScenarioResponse>(
      `/scenes/${sceneId}/generate-scenario`,
      request
    ),

  /**
   * 시나리오 버전 저장
   */
  saveScenarioVersion: (sceneId: number, versionName: string, dialogues: Dialogue[]) =>
    apiClient.post<API.ScenarioVersion>(`/scenes/${sceneId}/scenarios`, {
      versionName,
      dialogues,
    }),

  /**
   * 시나리오 버전 목록 조회
   */
  getScenarioVersions: (sceneId: number) =>
    apiClient.get<API.ScenarioVersion[]>(`/scenes/${sceneId}/scenarios`),

  /**
   * 특정 시나리오 버전 조회
   */
  getScenarioVersion: (versionId: number) =>
    apiClient.get<API.ScenarioVersion>(`/scenes/scenarios/${versionId}`),
};

/**
 * Dialogue API
 */
export const dialoguesApi = {
  /**
   * 대사 생성
   */
  create: (data: API.CreateDialogueRequest) =>
    apiClient.post<Dialogue>('/dialogue', data),

  /**
   * 대사 수정
   */
  update: (id: number, data: API.UpdateDialogueRequest) =>
    apiClient.put<Dialogue>(`/dialogue/${id}`, data),

  /**
   * 대사 삭제
   */
  delete: (id: number) => apiClient.delete<void>(`/dialogue/${id}`),

  /**
   * 대사 제안 생성
   */
  suggest: (request: API.SuggestRequest) =>
    apiClient.post<API.SuggestResponse>('/dialogue/suggest', request),
};

/**
 * Character API
 */
export const charactersApi = {
  /**
   * 모든 캐릭터 조회
   */
  getAll: () => apiClient.get<Character[]>('/characters'),

  /**
   * 특정 캐릭터 조회
   */
  getById: (id: number) => apiClient.get<Character>(`/characters/${id}`),

  /**
   * 캐릭터 ID로 조회
   */
  getByCharacterId: (characterId: string) =>
    apiClient.get<Character>(`/characters/cid/${characterId}`),

  /**
   * 캐릭터 생성
   */
  create: (data: Omit<Character, 'id'>) =>
    apiClient.post<Character>('/characters', data),

  /**
   * 캐릭터 수정
   */
  update: (id: number, data: Partial<Character>) =>
    apiClient.put<Character>(`/characters/${id}`, data),

  /**
   * 캐릭터 삭제
   */
  delete: (id: number) => apiClient.delete<void>(`/characters/${id}`),

  /**
   * 말투 프로필 조회
   */
  getSpeakingProfile: (id: number) =>
    apiClient.get<Character>(`/characters/${id}/speaking-profile`),

  /**
   * 말투 프로필 수정
   */
  updateSpeakingProfile: (id: number, data: API.UpdateSpeakingProfileRequest) =>
    apiClient.put<Character>(`/characters/${id}/speaking-profile`, data),
};

/**
 * Episode Relationship API
 * 에피소드별 캐릭터 관계 관리
 */
export const episodeRelationshipsApi = {
  /**
   * 특정 에피소드의 관계 그래프 데이터 조회
   */
  getEpisodeGraph: (episodeId: number) =>
    apiClient.get<GraphData>(`/episode-relationships/episode/${episodeId}/graph`),

  /**
   * 특정 에피소드의 모든 관계 조회
   */
  getByEpisode: (episodeId: number) =>
    apiClient.get<Relationship[]>(`/episode-relationships/episode/${episodeId}`),

  /**
   * 두 캐릭터 간의 관계 변화 히스토리 조회
   */
  getHistory: (char1Id: number, char2Id: number) =>
    apiClient.get<Relationship[]>(
      `/episode-relationships/history?char1Id=${char1Id}&char2Id=${char2Id}`
    ),

  /**
   * 에피소드 관계 생성
   */
  create: (data: Omit<Relationship, 'id'>) =>
    apiClient.post<Relationship>('/episode-relationships', data),

  /**
   * 에피소드 관계 수정
   */
  update: (id: number, data: Partial<Relationship>) =>
    apiClient.put<Relationship>(`/episode-relationships/${id}`, data),

  /**
   * 에피소드 관계 삭제
   */
  delete: (id: number) => apiClient.delete<void>(`/episode-relationships/${id}`),
};

/**
 * Health Check API
 */
export const healthApi = {
  /**
   * API 서버 헬스체크
   */
  check: () => apiClient.get<API.HealthCheckResponse>('/health'),

  /**
   * 프론트엔드 헬스체크 (Next.js API Route)
   */
  checkFrontend: () => fetch('/api/health').then((res) => res.json()),
};