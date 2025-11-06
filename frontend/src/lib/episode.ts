import apiClient from './api';

export interface Episode {
  id: number;
  title: string;
  description?: string;
  episodeOrder: number;
  scriptText?: string;
  scriptFormat?: string;
  analysisStatus?: string;
  analysisResult?: string;
  llmProvider?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EpisodeRequest {
  title: string;
  description?: string;
  episodeOrder: number;
  scriptText?: string;
  scriptFormat?: string;
}

/**
 * 에피소드 목록 조회 (현재 프로젝트)
 */
export const getEpisodes = async (): Promise<Episode[]> => {
  const response = await apiClient.get<Episode[]>('/episodes');
  return response.data;
};

/**
 * 에피소드 상세 조회
 */
export const getEpisode = async (id: number): Promise<Episode> => {
  const response = await apiClient.get<Episode>(`/episodes/${id}`);
  return response.data;
};

/**
 * 에피소드 생성
 */
export const createEpisode = async (data: EpisodeRequest): Promise<Episode> => {
  const response = await apiClient.post<Episode>('/episodes', data);
  return response.data;
};

/**
 * 에피소드 수정
 */
export const updateEpisode = async (id: number, data: EpisodeRequest): Promise<Episode> => {
  const response = await apiClient.put<Episode>(`/episodes/${id}`, data);
  return response.data;
};

/**
 * 에피소드 삭제
 */
export const deleteEpisode = async (id: number): Promise<void> => {
  await apiClient.delete(`/episodes/${id}`);
};
