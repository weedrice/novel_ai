'use client'

import { useState, useEffect } from 'react'
import { getPlotAnalysis, PlotAnalysis } from '@/lib/plot'
import apiClient from '@/lib/api'
import Card from '@/components/Card'
import Select from '@/components/ui/Select'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts'

interface Episode {
  id: number
  title: string
}

export default function PlotStructurePage() {
  const [selectedEpisodeId, setSelectedEpisodeId] = useState<number | null>(null)
  const [episodes, setEpisodes] = useState<Episode[]>([])
  const [plotData, setPlotData] = useState<PlotAnalysis | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadEpisodes()
  }, [])

  const loadEpisodes = async () => {
    try {
      const response = await apiClient.get('/episodes')
      setEpisodes(response.data)

      // 첫 번째 에피소드 자동 선택
      if (response.data.length > 0) {
        setSelectedEpisodeId(response.data[0].id)
      }
    } catch (err: any) {
      console.error('Failed to load episodes:', err)
      setError(`에피소드 로드 실패: ${err.message}`)
    }
  }

  useEffect(() => {
    if (selectedEpisodeId) {
      loadPlotAnalysis(selectedEpisodeId)
    }
  }, [selectedEpisodeId])

  const loadPlotAnalysis = async (episodeId: number) => {
    setLoading(true)
    setError(null)

    try {
      const analysis = await getPlotAnalysis(episodeId)
      setPlotData(analysis)
    } catch (err: any) {
      setError(`플롯 분석 실패: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  // 스토리 아크 데이터 준비 (갈등 강도 곡선)
  const storyArcData = plotData?.scenes.map((scene) => ({
    name: `장면 ${scene.sceneNumber}`,
    sceneNumber: scene.sceneNumber,
    tensionLevel: scene.tensionLevel,
    dialogueCount: scene.dialogueCount,
  })) || []

  // 캐릭터 등장 빈도 데이터
  const characterFrequencyData = plotData?.characterFrequencies.slice(0, 10) || [] // 상위 10명만

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6 md:p-10 transition-colors duration-200">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-2">
            📊 플롯 구조 시각화
          </h1>
          <p className="text-gray-600 dark:text-gray-400 text-lg">
            에피소드별 스토리 아크, 갈등 강도, 캐릭터 등장 빈도를 분석합니다
          </p>
        </div>

        {error && (
          <ErrorMessage
            message={error}
            onDismiss={() => setError(null)}
          />
        )}

        {/* 에피소드 선택 */}
        <Card title="에피소드 선택" className="mb-6">
          <div className="max-w-md">
            <Select
              value={selectedEpisodeId?.toString() || ''}
              onChange={(e) => setSelectedEpisodeId(Number(e.target.value))}
            >
              {episodes.map((ep) => (
                <option key={ep.id} value={ep.id}>
                  {ep.title}
                </option>
              ))}
            </Select>
          </div>
        </Card>

        {loading && (
          <div className="mt-6">
            <LoadingSpinner size="lg" message="플롯 분석 중..." />
          </div>
        )}

        {!loading && plotData && (
          <>
            {/* 기본 통계 */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
              <Card title="총 장면 수">
                <div className="text-3xl font-bold text-blue-600 dark:text-blue-400">
                  {plotData.totalScenes}
                </div>
              </Card>
              <Card title="총 대사 수">
                <div className="text-3xl font-bold text-green-600 dark:text-green-400">
                  {plotData.totalDialogues}
                </div>
              </Card>
              <Card title="평균 갈등 강도">
                <div className="text-3xl font-bold text-orange-600 dark:text-orange-400">
                  {(plotData.averageTensionLevel * 100).toFixed(1)}%
                </div>
              </Card>
              <Card title="등장 캐릭터">
                <div className="text-3xl font-bold text-purple-600 dark:text-purple-400">
                  {plotData.characterFrequencies.length}
                </div>
              </Card>
            </div>

            {/* 스토리 아크 곡선 (갈등 강도) */}
            <Card title="🎭 스토리 아크 (갈등 강도 곡선)" className="mb-6">
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                장면별 갈등 강도를 시각화하여 기승전결 구조를 파악합니다
              </p>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={storyArcData}>
                  <defs>
                    <linearGradient id="colorTension" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#f59e0b" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 12 }}
                  />
                  <YAxis
                    domain={[0, 1]}
                    tick={{ fontSize: 12 }}
                    label={{ value: '갈등 강도', angle: -90, position: 'insideLeft' }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'rgba(255, 255, 255, 0.95)',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                    }}
                    formatter={(value: any) => `${(value * 100).toFixed(1)}%`}
                  />
                  <Area
                    type="monotone"
                    dataKey="tensionLevel"
                    stroke="#f59e0b"
                    fillOpacity={1}
                    fill="url(#colorTension)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </Card>

            {/* 장면별 대사 수 */}
            <Card title="💬 장면별 대사 수" className="mb-6">
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                각 장면의 대사 수를 비교하여 장면별 비중을 파악합니다
              </p>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={storyArcData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 12 }}
                  />
                  <YAxis
                    tick={{ fontSize: 12 }}
                    label={{ value: '대사 수', angle: -90, position: 'insideLeft' }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'rgba(255, 255, 255, 0.95)',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                    }}
                  />
                  <Bar dataKey="dialogueCount" fill="#3b82f6" />
                </BarChart>
              </ResponsiveContainer>
            </Card>

            {/* 캐릭터 등장 빈도 */}
            <Card title="👥 캐릭터 등장 빈도 (상위 10명)" className="mb-6">
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                캐릭터별 등장 장면 수와 대사 수를 비교합니다
              </p>
              <ResponsiveContainer width="100%" height={400}>
                <BarChart data={characterFrequencyData} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" tick={{ fontSize: 12 }} />
                  <YAxis
                    type="category"
                    dataKey="characterName"
                    tick={{ fontSize: 12 }}
                    width={120}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'rgba(255, 255, 255, 0.95)',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                    }}
                  />
                  <Legend />
                  <Bar dataKey="appearanceCount" fill="#8b5cf6" name="등장 장면 수" />
                  <Bar dataKey="dialogueCount" fill="#10b981" name="대사 수" />
                </BarChart>
              </ResponsiveContainer>
            </Card>

            {/* 장면 상세 정보 */}
            <Card title="🎬 장면별 상세 정보" className="mb-6">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        장면
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        설명
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        대사 수
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        갈등 강도
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        참여 캐릭터
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                    {plotData.scenes.map((scene) => (
                      <tr key={scene.sceneId}>
                        <td className="px-4 py-3 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
                          {scene.sceneNumber}
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-700 dark:text-gray-300">
                          {scene.sceneTitle || scene.location}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-700 dark:text-gray-300">
                          {scene.dialogueCount}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-700 dark:text-gray-300">
                          <div className="flex items-center gap-2">
                            <div className="w-24 bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                              <div
                                className="bg-orange-500 h-2 rounded-full"
                                style={{ width: `${scene.tensionLevel * 100}%` }}
                              />
                            </div>
                            <span className="text-xs">
                              {(scene.tensionLevel * 100).toFixed(0)}%
                            </span>
                          </div>
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-700 dark:text-gray-300">
                          {scene.participants.join(', ')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          </>
        )}

        {!loading && !plotData && selectedEpisodeId && (
          <div className="text-center py-12 text-gray-500 dark:text-gray-400">
            플롯 데이터를 불러올 수 없습니다.
          </div>
        )}
      </div>
    </main>
  )
}
