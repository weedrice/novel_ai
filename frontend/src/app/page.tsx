'use client'

import { useState, useEffect } from 'react'
import Button from '@/components/ui/Button'
import Card from '@/components/Card'
import ErrorMessage from '@/components/ErrorMessage'
import LoadingSpinner from '@/components/LoadingSpinner'
import apiClient from '@/lib/api'
import { demoEpisodes, demoCandidates, isDemoMode } from '@/data/demoData'
import Link from 'next/link'

export default function Home() {
  const [episodes, setEpisodes] = useState<any[]>([])
  const [candidates, setCandidates] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [provider, setProvider] = useState<string>('openai')
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

  const fetchSuggestions = async () => {
    // 데모 모드일 경우 데모 데이터 사용
    if (isDemo) {
      setLoading(true)
      // 실제 API 호출처럼 보이도록 약간의 딜레이 추가
      setTimeout(() => {
        setCandidates(demoCandidates)
        setLoading(false)
      }, 500)
      return
    }

    setLoading(true)
    setError(null)
    try {
      const response = await apiClient.post('/dialogue/suggest', {
        speakerId: 'char.seha',
        targetIds: ['char.jiho'],
        intent: 'reconcile',
        honorific: 'banmal',
        maxLen: 80,
        nCandidates: 3,
        provider,
      })
      setCandidates(response.data.candidates || [])
    } catch (err: any) {
      setError(`대사 제안 실패: ${err.message}`)
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

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
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
        </div>

        {error && (
          <ErrorMessage
            message={error}
            onRetry={() => {
              setError(null)
              if (episodes.length === 0) fetchEpisodes()
              else fetchSuggestions()
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

        <Card title="대사 제안" className="mb-6">
          <div className="mb-4">
            <label className="block mb-2 font-semibold text-gray-700 dark:text-gray-300">LLM 제공자 선택</label>
            <select
              value={provider}
              onChange={(e) => setProvider(e.target.value)}
              className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 min-w-[200px] transition-colors"
            >
              <option value="openai">OpenAI GPT</option>
              <option value="claude">Anthropic Claude</option>
              <option value="gemini">Google Gemini</option>
            </select>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
              선택된 LLM:{' '}
              {provider === 'openai' ? 'OpenAI GPT' : provider === 'claude' ? 'Anthropic Claude' : 'Google Gemini'}
            </p>
          </div>

        <Button onClick={fetchSuggestions} loading={loading} disabled={loading} variant="success">
            대사 제안 받기
          </Button>

          <div className="mt-3 text-sm text-gray-600 dark:text-gray-400 bg-gray-50 dark:bg-gray-700 p-3 rounded-md transition-colors">
            <strong>요청 설정:</strong> 화자: char.seha | 대상: char.jiho | 의도: reconcile | 톤: banmal
          </div>

          {loading && !error && (
            <div className="mt-6">
              <LoadingSpinner size="lg" message="제안을 생성하는 중입니다..." />
            </div>
          )}

          {candidates.length > 0 && (
            <ol className="mt-6 space-y-3">
              {candidates.map((c: any, i: number) => (
                <li key={i} className="p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg transition-colors">
                  <span className="text-gray-800 dark:text-gray-200">{c.text}</span>{' '}
                  <span className="text-gray-500 dark:text-gray-400 text-sm">(신뢰도: {c.score?.toFixed?.(2)})</span>
                </li>
              ))}
            </ol>
          )}
        </Card>
      </div>
    </main>
  )
}

