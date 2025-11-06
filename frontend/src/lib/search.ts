import apiClient from './api';

export interface Dialogue {
  id: number;
  text: string;
  dialogueOrder: number;
  intent?: string;
  honorific?: string;
  emotion?: string;
  character?: {
    characterId: number;
    name: string;
  };
  scene?: {
    id: number;
    description: string;
    sceneNumber: number;
    episode?: {
      id: number;
      title: string;
    };
  };
}

export interface SearchParams {
  query?: string;
  characterId?: number;
  episodeId?: number;
  sceneId?: number;
}

/**
 * Task 105: 대사 검색 API 호출 (키워드 기반)
 */
export async function searchDialogues(params: SearchParams): Promise<Dialogue[]> {
  const queryParams = new URLSearchParams();

  if (params.query) queryParams.append('query', params.query);
  if (params.characterId) queryParams.append('characterId', params.characterId.toString());
  if (params.episodeId) queryParams.append('episodeId', params.episodeId.toString());
  if (params.sceneId) queryParams.append('sceneId', params.sceneId.toString());

  const response = await apiClient.get(`/dialogue/search?${queryParams.toString()}`);
  return response.data;
}

export interface SemanticSearchResult {
  id: number;
  sourceType: string;
  sourceId: number;
  textChunk: string;
  metadata: string;
  createdAt: string;
}

export interface SemanticSearchParams {
  query: string;
  limit?: number;
  sourceType?: string;
  keyword?: string;
}

/**
 * Phase 7: 의미 기반 검색 (Vector Similarity)
 */
export async function semanticSearch(params: SemanticSearchParams): Promise<SemanticSearchResult[]> {
  const response = await apiClient.post('/search/semantic', {
    query: params.query,
    limit: params.limit || 10,
  });
  return response.data;
}

/**
 * Phase 7: 타입별 의미 검색
 */
export async function semanticSearchByType(params: SemanticSearchParams): Promise<SemanticSearchResult[]> {
  const response = await apiClient.post('/search/semantic/by-type', {
    query: params.query,
    sourceType: params.sourceType,
    limit: params.limit || 10,
  });
  return response.data;
}

/**
 * Phase 7: 하이브리드 검색 (Vector + Keyword)
 */
export async function hybridSearch(params: SemanticSearchParams): Promise<SemanticSearchResult[]> {
  const response = await apiClient.post('/search/hybrid', {
    query: params.query,
    keyword: params.keyword,
    limit: params.limit || 10,
  });
  return response.data;
}
