'use client';

import React, { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import {
  getRelationshipsByEpisodeRange,
  getCharacterRelationshipEvolution,
  getRelationshipTimeline,
  getNetworkDensityByEpisode,
  getNewRelationshipsByEpisode,
} from '@/lib/graph';
import { getAllEpisodes } from '@/lib/episode';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface Episode {
  id: number;
  title: string;
  episodeNumber: number;
}

export default function GraphTimelinePage() {
  const [episodes, setEpisodes] = useState<Episode[]>([]);
  const [selectedRange, setSelectedRange] = useState<[number, number]>([1, 10]);
  const [selectedCharacter, setSelectedCharacter] = useState<string>('');
  const [timelineData, setTimelineData] = useState<any>(null);
  const [densityData, setDensityData] = useState<any>(null);
  const [newRelationships, setNewRelationships] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // 에피소드 목록 로드
  useEffect(() => {
    loadEpisodes();
    loadNewRelationships();
  }, []);

  const loadEpisodes = async () => {
    try {
      const data = await getAllEpisodes();
      setEpisodes(data.episodes || []);

      if (data.episodes && data.episodes.length > 0) {
        const maxEpisode = Math.max(...data.episodes.map((e: Episode) => e.episodeNumber));
        setSelectedRange([1, Math.min(10, maxEpisode)]);
      }
    } catch (error) {
      console.error('Failed to load episodes:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadNewRelationships = async () => {
    try {
      const data = await getNewRelationshipsByEpisode();
      setNewRelationships(data.newRelationships || []);
    } catch (error) {
      console.error('Failed to load new relationships:', error);
    }
  };

  // 에피소드 범위별 네트워크 밀도 로드
  const loadDensityData = async () => {
    try {
      const densities = [];
      for (let i = selectedRange[0]; i <= selectedRange[1]; i++) {
        const episodeId = episodes.find((e) => e.episodeNumber === i)?.id;
        if (episodeId) {
          const data = await getNetworkDensityByEpisode(episodeId);
          densities.push({
            episode: i,
            density: data.density?.density || 0,
          });
        }
      }

      setDensityData({
        labels: densities.map((d) => `Episode ${d.episode}`),
        datasets: [
          {
            label: 'Network Density',
            data: densities.map((d) => d.density),
            borderColor: 'rgb(59, 130, 246)',
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            fill: true,
            tension: 0.4,
          },
        ],
      });
    } catch (error) {
      console.error('Failed to load density data:', error);
    }
  };

  // 캐릭터 관계 진화 로드
  const loadCharacterEvolution = async (characterId: string) => {
    try {
      const data = await getCharacterRelationshipEvolution(characterId);

      // 에피소드별 관계 개수 집계
      const episodeCounts: Record<number, number> = {};
      data.evolution?.forEach((item: any) => {
        const episodeId = item.episodeId;
        episodeCounts[episodeId] = (episodeCounts[episodeId] || 0) + 1;
      });

      const sortedEpisodes = Object.keys(episodeCounts).map(Number).sort((a, b) => a - b);

      setTimelineData({
        labels: sortedEpisodes.map((ep) => `Episode ${ep}`),
        datasets: [
          {
            label: `${characterId} Relationships`,
            data: sortedEpisodes.map((ep) => episodeCounts[ep]),
            borderColor: 'rgb(236, 72, 153)',
            backgroundColor: 'rgba(236, 72, 153, 0.1)',
            fill: true,
            tension: 0.4,
          },
        ],
      });
    } catch (error) {
      console.error('Failed to load character evolution:', error);
    }
  };

  useEffect(() => {
    if (episodes.length > 0) {
      loadDensityData();
    }
  }, [selectedRange, episodes]);

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: true,
        text: 'Relationship Evolution Timeline',
      },
    },
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
      {/* 헤더 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          관계 변화 타임라인
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-2">
          에피소드별로 캐릭터 관계가 어떻게 변화하는지 추적합니다
        </p>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-600 dark:text-gray-400">로딩 중...</div>
        </div>
      ) : (
        <div className="space-y-6">
          {/* 에피소드 범위 선택 */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              에피소드 범위 선택
            </h2>
            <div className="flex gap-4 items-center">
              <div>
                <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">
                  시작
                </label>
                <input
                  type="number"
                  min="1"
                  max={selectedRange[1]}
                  value={selectedRange[0]}
                  onChange={(e) =>
                    setSelectedRange([parseInt(e.target.value), selectedRange[1]])
                  }
                  className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">
                  종료
                </label>
                <input
                  type="number"
                  min={selectedRange[0]}
                  value={selectedRange[1]}
                  onChange={(e) =>
                    setSelectedRange([selectedRange[0], parseInt(e.target.value)])
                  }
                  className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
            </div>
          </div>

          {/* 네트워크 밀도 차트 */}
          {densityData && (
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                네트워크 밀도 변화
              </h2>
              <Line options={chartOptions} data={densityData} />
            </div>
          )}

          {/* 캐릭터 선택 및 관계 진화 */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              캐릭터 관계 진화
            </h2>
            <div className="flex gap-4 items-end mb-6">
              <div className="flex-1">
                <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">
                  캐릭터 ID
                </label>
                <input
                  type="text"
                  value={selectedCharacter}
                  onChange={(e) => setSelectedCharacter(e.target.value)}
                  placeholder="예: alice"
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
              <button
                onClick={() => loadCharacterEvolution(selectedCharacter)}
                disabled={!selectedCharacter}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                조회
              </button>
            </div>
            {timelineData && (
              <Line options={chartOptions} data={timelineData} />
            )}
          </div>

          {/* 새로운 관계 목록 */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              새로운 관계 형성
            </h2>
            <div className="space-y-2">
              {newRelationships.length > 0 ? (
                newRelationships.slice(0, 10).map((rel, idx) => (
                  <div
                    key={idx}
                    className="flex justify-between items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
                  >
                    <div className="flex gap-2">
                      <span className="font-mono text-sm text-blue-600 dark:text-blue-400">
                        {rel.char1}
                      </span>
                      <span className="text-gray-500">↔</span>
                      <span className="font-mono text-sm text-pink-600 dark:text-pink-400">
                        {rel.char2}
                      </span>
                    </div>
                    <div className="flex gap-4 text-sm text-gray-600 dark:text-gray-400">
                      <span>Episode {rel.firstAppearance}</span>
                      <span>{rel.interactionCount} interactions</span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-gray-500 dark:text-gray-400 text-center py-4">
                  데이터가 없습니다
                </p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
