"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Card from "@/components/Card";
import LoadingSpinner from "@/components/LoadingSpinner";
import ErrorMessage from "@/components/ErrorMessage";
import apiClient from "@/lib/api";
import { isDemoMode, demoEpisodes, demoScenes } from "@/data/demoData";
import Link from "next/link";

type Episode = { id: number; title: string; description: string };
type Scene = {
  id: number;
  sceneNumber: number;
  location: string;
  mood: string;
  description: string;
  participants: string;
};

export default function ScenesPage() {
  const router = useRouter();
  const [episodes, setEpisodes] = useState<Episode[]>([]);
  const [scenes, setScenes] = useState<Scene[]>([]);
  const [selectedEpisodeId, setSelectedEpisodeId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isDemo, setIsDemo] = useState(false);

  // 에피소드 목록 로드
  useEffect(() => {
    const demo = isDemoMode();
    setIsDemo(demo);

    if (!demo) {
      (async () => {
        await fetchEpisodes();
      })();
    }
  }, []);

  const fetchEpisodes = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get('/episodes');
      const data = res.data;
      setEpisodes(Array.isArray(data) ? data : []);
      // 첫 번째 에피소드 자동 선택
      if (data?.length > 0) {
        setSelectedEpisodeId(data[0].id);
        await fetchScenesByEpisode(data[0].id);
      }
    } catch (e: any) {
      setError(`에피소드 불러오기 실패: ${e?.message || e}`);
    } finally {
      setLoading(false);
    }
  };

  const fetchScenesByEpisode = async (episodeId: number) => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get(`/scenes/episode/${episodeId}`);
      setScenes(Array.isArray(res.data) ? res.data : []);
    } catch (e: any) {
      setError(`장면 불러오기 실패: ${e?.message || e}`);
    } finally {
      setLoading(false);
    }
  };

  const handleEpisodeChange = (episodeId: number) => {
    setSelectedEpisodeId(episodeId);
    fetchScenesByEpisode(episodeId);
  };

  const handleSceneClick = (sceneId: number) => {
    router.push(`/scenes/${sceneId}/edit`);
  };

  return (
    <main className="min-h-screen bg-gray-50 p-6 md:p-10">
      <div className="max-w-6xl mx-auto">
        {/* 데모 모드 배너 */}
        {isDemo && (
          <div className="mb-6 p-4 bg-blue-50 border-l-4 border-blue-500 rounded-r-lg">
            <p className="text-sm text-blue-700">
              <strong className="font-semibold">데모 모드</strong> - 이 기능을 사용하려면{" "}
              <Link href="/login" className="underline hover:text-blue-800">
                로그인
              </Link>
              해주세요.
            </p>
          </div>
        )}

        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900">장면 관리</h1>
            <a href="/" className="text-blue-600 hover:text-blue-800 font-medium">
              홈으로
            </a>
          </div>
          <p className="text-gray-600">에피소드와 장면을 관리하고 시나리오를 편집하세요.</p>
        </div>

        {error && (
          <ErrorMessage
            message={error}
            onRetry={() => {
              setError(null);
              if (selectedEpisodeId) fetchScenesByEpisode(selectedEpisodeId);
              else fetchEpisodes();
            }}
            onDismiss={() => setError(null)}
          />
        )}

        {episodes.length > 0 && (
          <Card className="mb-6">
            <label className="block mb-2 font-semibold text-gray-700">에피소드 선택</label>
            <select
              value={selectedEpisodeId ?? ""}
              onChange={(e) => handleEpisodeChange(Number(e.target.value))}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {episodes.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.title}
                </option>
              ))}
            </select>
          </Card>
        )}

        {loading && (
          <div className="flex justify-center py-12">
            <LoadingSpinner size="lg" message="장면을 불러오는 중..." />
          </div>
        )}

        {!loading && scenes.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {scenes.map((scene) => (
              <Card
                key={scene.id}
                className="cursor-pointer hover:shadow-lg transition-shadow duration-200 border-2 border-transparent hover:border-blue-500"
                onClick={() => handleSceneClick(scene.id)}
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-semibold">
                    장면 {scene.sceneNumber}
                  </div>
                  <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    {scene.location || "장소 미정"}
                  </div>

                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {scene.mood || "분위기 미정"}
                  </div>

                  <p className="text-gray-700 text-sm mt-3">
                    {scene.description || "장면 설명이 없습니다."}
                  </p>

                  {scene.participants && (
                    <div className="mt-3 pt-3 border-t border-gray-200">
                      <p className="text-xs text-gray-500">
                        참여 캐릭터 {scene.participants.split(",").length}명
                      </p>
                    </div>
                  )}
                </div>
              </Card>
            ))}
          </div>
        )}

        {!loading && scenes.length === 0 && selectedEpisodeId && (
          <Card className="text-center py-12">
            <svg className="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 4v16M17 4v16M3 8h4m10 0h4M3 12h18M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z" />
            </svg>
            <p className="text-gray-500 text-lg mb-2">선택한 에피소드에는 장면이 없습니다.</p>
            <p className="text-gray-400 text-sm">우측 상단 UI에서 장면을 추가해보세요.</p>
          </Card>
        )}
      </div>
    </main>
  );
}