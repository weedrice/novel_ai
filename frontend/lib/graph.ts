/**
 * Neo4j GraphDB API 클라이언트
 */

import api from '@/lib/api';

export interface CharacterNode {
  id: number;
  rdbId: number;
  projectId: number;
  characterId: string;
  name: string;
  description?: string;
  personality?: string;
  speakingStyle?: string;
}

export interface CharacterRelationship {
  id: number;
  episodeId: number;
  relationType: string;
  closeness: number;
  description?: string;
  targetCharacter: CharacterNode;
}

export interface PathResult {
  found: boolean;
  path?: any;
  message?: string;
}

/**
 * 모든 캐릭터 조회
 */
export const getAllCharacters = async (): Promise<CharacterNode[]> => {
  const response = await api.get('/graph/characters');
  return response.data;
};

/**
 * 캐릭터 ID로 조회
 */
export const getCharacter = async (characterId: string): Promise<CharacterNode> => {
  const response = await api.get(`/graph/characters/${characterId}`);
  return response.data;
};

/**
 * N단계 친구 찾기
 */
export const getNDegreeFriends = async (characterId: string, depth: number = 2) => {
  const response = await api.get(`/graph/characters/${characterId}/friends`, {
    params: { depth }
  });
  return response.data;
};

/**
 * 특정 관계 유형으로 연결된 캐릭터 찾기
 */
export const getCharactersByRelationType = async (characterId: string, type: string) => {
  const response = await api.get(`/graph/characters/${characterId}/relations`, {
    params: { type }
  });
  return response.data;
};

/**
 * 최단 경로 찾기
 */
export const getShortestPath = async (from: string, to: string): Promise<PathResult> => {
  const response = await api.get('/graph/path', {
    params: { from, to }
  });
  return response.data;
};

/**
 * 모든 관계 조회 (그래프 시각화용)
 */
export const getAllRelationships = async () => {
  const response = await api.get('/graph/relationships');
  return response.data;
};

/**
 * 에피소드별 관계 조회
 */
export const getRelationshipsByEpisode = async (episodeId: number) => {
  const response = await api.get(`/graph/relationships/episode/${episodeId}`);
  return response.data;
};

/**
 * 중심 인물 찾기
 */
export const getCentralCharacters = async (limit: number = 10) => {
  const response = await api.get('/graph/central-characters', {
    params: { limit }
  });
  return response.data;
};

/**
 * 전체 데이터 동기화
 */
export const syncAllData = async () => {
  const response = await api.post('/graph/sync/all');
  return response.data;
};

/**
 * 프로젝트 데이터 동기화
 */
export const syncProjectData = async (projectId: number) => {
  const response = await api.post(`/graph/sync/project/${projectId}`);
  return response.data;
};

/**
 * Centrality 계산 - Degree Centrality
 */
export const getDegreeCentrality = async (limit: number = 10) => {
  const response = await api.get('/graph/centrality/degree', {
    params: { limit }
  });
  return response.data;
};

/**
 * Centrality 계산 - Betweenness Centrality
 */
export const getBetweennessCentrality = async (limit: number = 10) => {
  const response = await api.get('/graph/centrality/betweenness', {
    params: { limit }
  });
  return response.data;
};

/**
 * Centrality 계산 - Closeness Centrality
 */
export const getClosenessCentrality = async (limit: number = 10) => {
  const response = await api.get('/graph/centrality/closeness', {
    params: { limit }
  });
  return response.data;
};

/**
 * Centrality 계산 - Weighted Degree
 */
export const getWeightedDegree = async (limit: number = 10) => {
  const response = await api.get('/graph/centrality/weighted', {
    params: { limit }
  });
  return response.data;
};

/**
 * 모든 Centrality 지표 한번에 조회
 */
export const getAllCentralities = async (limit: number = 10) => {
  const response = await api.get('/graph/centrality/all', {
    params: { limit }
  });
  return response.data;
};

/**
 * 에피소드 범위별 관계 변화 조회
 */
export const getRelationshipsByEpisodeRange = async (start: number, end: number) => {
  const response = await api.get('/graph/timeline/range', {
    params: { start, end }
  });
  return response.data;
};

/**
 * 특정 캐릭터의 관계 진화 추적
 */
export const getCharacterRelationshipEvolution = async (characterId: string) => {
  const response = await api.get(`/graph/timeline/character/${characterId}`);
  return response.data;
};

/**
 * 두 캐릭터 간 관계 타임라인
 */
export const getRelationshipTimeline = async (char1: string, char2: string) => {
  const response = await api.get('/graph/timeline/relationship', {
    params: { char1, char2 }
  });
  return response.data;
};

/**
 * 에피소드별 네트워크 밀도 계산
 */
export const getNetworkDensityByEpisode = async (episodeId: number) => {
  const response = await api.get(`/graph/timeline/density/${episodeId}`);
  return response.data;
};

/**
 * 새로운 관계 추가 현황 조회
 */
export const getNewRelationshipsByEpisode = async () => {
  const response = await api.get('/graph/timeline/new-relationships');
  return response.data;
};
