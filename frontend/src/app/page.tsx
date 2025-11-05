'use client'

import { useState, useEffect } from 'react'
import Button from '@/components/ui/Button'
import Card from '@/components/Card'
import ErrorMessage from '@/components/ErrorMessage'
import apiClient from '@/lib/api'
import { demoEpisodes, isDemoMode } from '@/data/demoData'
import Link from 'next/link'

export default function Home() {
  const [episodes, setEpisodes] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isDemo, setIsDemo] = useState(false)

  // 컴포넌트 마운트 시 데모 모드 확인
  useEffect(() => {
    setIsDemo(isDemoMode())
  }, [])

  const fetchEpisodes = async () => {
    // 데모 모드일 경우 데모 데이터 사용
    if (isDemo) {
      setLoading(true)
      // 실제 API 호출처럼 보이도록 약간의 딜레이 추가
      setTimeout(() => {
        setEpisodes(demoEpisodes)
        setLoading(false)
      }, 300)
      return
    }

    setLoading(true)
    setError(null)
    try {
      const response = await apiClient.get('/episodes')
      setEpisodes(response.data)
    } catch (err: any) {
      setError(`에피소드 불러오기 실패: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6 md:p-10 transition-colors duration-200">
      <div className="max-w-4xl mx-auto">
        {/* 데모 모드 배너 */}
        {isDemo && (
          <div className="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 border-l-4 border-blue-500 dark:border-blue-400 rounded-r-lg">
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-blue-500 dark:text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3 flex-1">
                <p className="text-sm text-blue-700 dark:text-blue-300">
                  <strong className="font-semibold">데모 모드</strong> - 현재 예시 데이터를 보고 계십니다.
                  <Link href="/login" className="underline ml-1 hover:text-blue-800 dark:hover:text-blue-200">
                    로그인
                  </Link>하여 나만의 프로젝트를 만들고 저장하세요.
                </p>
              </div>
            </div>
          </div>
        )}

        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-2">캐릭터 대사 톤 보조 시스템</h1>
          <p className="text-gray-600 dark:text-gray-400 text-lg">Character Dialogue Tone Assistant System</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          <a href="/graph" className="block p-6 bg-purple-600 hover:bg-purple-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">캐릭터 관계 그래프</div>
            <p className="text-purple-100 text-sm">캐릭터 간 관계를 시각적으로 확인합니다.</p>
          </a>
          <a href="/characters" className="block p-6 bg-amber-600 hover:bg-amber-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">말투 프로필 관리</div>
            <p className="text-amber-100 text-sm">캐릭터의 말투·어휘·톤 키워드를 관리합니다.</p>
          </a>
          <a href="/scenes" className="block p-6 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">시나리오 편집</div>
            <p className="text-emerald-100 text-sm">장면별 시나리오를 생성·편집합니다.</p>
          </a>
          <a href="/script-analyzer" className="block p-6 bg-pink-600 hover:bg-pink-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">📝 스크립트 분석</div>
            <p className="text-pink-100 text-sm">소설·시나리오에서 캐릭터와 관계를 추출합니다.</p>
          </a>
          <a href="/dialogue-stream" className="block p-6 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">⚡ 실시간 대사 생성</div>
            <p className="text-indigo-100 text-sm">스트리밍으로 LLM 대사 생성을 실시간 확인합니다.</p>
          </a>
          <a href="/search" className="block p-6 bg-cyan-600 hover:bg-cyan-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">🔍 대사 검색</div>
            <p className="text-cyan-100 text-sm">텍스트 검색 및 필터로 대사를 빠르게 찾습니다.</p>
          </a>
          <a href="/plot-structure" className="block p-6 bg-orange-600 hover:bg-orange-700 text-white rounded-lg shadow-md transition-colors duration-200">
            <div className="text-xl font-semibold mb-2">📊 플롯 구조 시각화</div>
            <p className="text-orange-100 text-sm">스토리 아크, 갈등 강도, 캐릭터 등장 빈도를 분석합니다.</p>
          </a>
        </div>

        {error && (
          <ErrorMessage
            message={error}
            onRetry={() => {
              setError(null)
              fetchEpisodes()
            }}
            onDismiss={() => setError(null)}
          />
        )}

        <Card title="에피소드 목록" className="mb-6">
          <Button onClick={fetchEpisodes} loading={loading} disabled={loading}>
            에피소드 불러오기
          </Button>
          {episodes.length > 0 && (
            <ul className="mt-4 space-y-2">
              {episodes.map((e: any) => (
                <li key={e.id} className="p-3 bg-gray-50 dark:bg-gray-700 rounded-md">
                  <strong className="text-blue-600 dark:text-blue-400">ID {e.id}:</strong>{' '}
                  <span className="text-gray-800 dark:text-gray-200">{e.title}</span>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </main>
  )
}

