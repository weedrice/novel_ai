import apiClient from './api';

export interface PlotAnalysis {
  episodeId: number;
  episodeTitle: string;
  episodeDescription: string;
  scenes: SceneAnalysis[];
  characterFrequencies: CharacterFrequency[];
  totalScenes: number;
  totalDialogues: number;
  averageTensionLevel: number;
}

export interface SceneAnalysis {
  sceneId: number;
  sceneTitle: string;
  location: string;
  mood: string;
  sceneNumber: number;
  dialogueCount: number;
  tensionLevel: number;
  participants: string[];
  cumulativeDialogueCount: number;
}

export interface CharacterFrequency {
  characterId: string;  // String 타입 (예: "char.seha")
  characterName: string;
  appearanceCount: number;
  dialogueCount: number;
  appearanceRate: number;
}

/**
 * Task 99: 플롯 분석 API 호출
 */
export async function getPlotAnalysis(episodeId: number): Promise<PlotAnalysis> {
  const response = await apiClient.get(`/episodes/${episodeId}/plot-analysis`);
  return response.data;
}
