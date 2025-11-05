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
 * Task 105: 대사 검색 API 호출
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
