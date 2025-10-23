'use client'

import { useState } from 'react'
import Button from '@/components/ui/Button'
import Card from '@/components/Card'
import ErrorMessage from '@/components/ErrorMessage'
import LoadingSpinner from '@/components/LoadingSpinner'

const API = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080'

export default function Home() {
  const [episodes, setEpisodes] = useState<any[]>([])
  const [candidates, setCandidates] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [provider, setProvider] = useState<string>('openai')

  const fetchEpisodes = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch(`${API}/episodes`)
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      setEpisodes(data)
    } catch (err: any) {
      setError(`에피소드 불러오기 실패: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  const fetchSuggestions = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch(`${API}/dialogue/suggest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          speakerId: 'char.seha',
          targetIds: ['char.jiho'],
          intent: 'reconcile',
          honorific: 'banmal',
          maxLen: 80,
          nCandidates: 3,
          provider,
        }),
      })
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      setCandidates(data.candidates || [])
    } catch (err: any) {
      setError(`대사 제안 실패: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-gray-50 p-6 md:p-10">
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">캐릭터 대사 톤 보조 시스템</h1>
          <p className="text-gray-600 text-lg">Character Dialogue Tone Assistant System</p>
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
                <li key={e.id} className="p-3 bg-gray-50 rounded-md">
                  <strong className="text-blue-600">ID {e.id}:</strong>{' '}
                  <span className="text-gray-800">{e.title}</span>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title="대사 제안" className="mb-6">
          <div className="mb-4">
            <label className="block mb-2 font-semibold text-gray-700">LLM 제공자 선택</label>
            <select
              value={provider}
              onChange={(e) => setProvider(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[200px]"
            >
              <option value="openai">OpenAI GPT</option>
              <option value="claude">Anthropic Claude</option>
              <option value="gemini">Google Gemini</option>
            </select>
            <p className="text-xs text-gray-500 mt-2">
              선택된 LLM:{' '}
              {provider === 'openai' ? 'OpenAI GPT' : provider === 'claude' ? 'Anthropic Claude' : 'Google Gemini'}
            </p>
          </div>

        <Button onClick={fetchSuggestions} loading={loading} disabled={loading} variant="success">
            대사 제안 받기
          </Button>

          <div className="mt-3 text-sm text-gray-600 bg-gray-50 p-3 rounded-md">
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
                <li key={i} className="p-4 bg-green-50 border border-green-200 rounded-lg">
                  <span className="text-gray-800">{c.text}</span>{' '}
                  <span className="text-gray-500 text-sm">(신뢰도: {c.score?.toFixed?.(2)})</span>
                </li>
              ))}
            </ol>
          )}
        </Card>
      </div>
    </main>
  )
}

