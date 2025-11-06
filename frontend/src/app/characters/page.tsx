'use client'

import { useEffect, useState } from 'react'
import apiClient from '@/lib/api'
import { demoCharacters, isDemoMode } from '@/data/demoData'
import Link from 'next/link'

type Character = {
  id: number
  characterId: string
  name: string
  description?: string
  personality?: string
}

type SpeakingProfile = {
  speakingStyle: string
  vocabulary: string
  toneKeywords: string
  examples: string
  prohibitedWords: string
  sentencePatterns: string
}

export default function CharactersPage() {
  const [characters, setCharacters] = useState<Character[]>([])
  const [selectedCharacter, setSelectedCharacter] = useState<Character | null>(null)
  const [profile, setProfile] = useState<SpeakingProfile | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [isDemo, setIsDemo] = useState(false)

  useEffect(() => {
    const demo = isDemoMode()
    setIsDemo(demo)

    if (demo) {
      // 데모 모드: 데모 캐릭터 사용
      setCharacters(demoCharacters as any)
    } else {
      // 일반 모드: API에서 캐릭터 로드
      ;(async () => {
        try {
          const res = await apiClient.get('/characters')
          setCharacters(Array.isArray(res.data) ? res.data : [])
        } catch (e) {
          setError('Failed to load characters')
        }
      })()
    }
  }, [])

  const loadProfile = async (c: Character) => {
    setSelectedCharacter(c)
    setLoading(true)
    setError(null)
    setSuccess(null)

    if (isDemo) {
      // 데모 모드: 데모 데이터에서 프로필 찾기
      setTimeout(() => {
        const demoChar = demoCharacters.find((dc: any) => dc.id === c.id)
        if (demoChar) {
          setProfile({
            speakingStyle: (demoChar as any).speakingStyle || '',
            vocabulary: (demoChar as any).vocabulary || '',
            toneKeywords: (demoChar as any).toneKeywords || '',
            examples: (demoChar as any).examples || '',
            prohibitedWords: (demoChar as any).prohibitedWords || '',
            sentencePatterns: (demoChar as any).sentencePatterns || '',
          })
        }
        setLoading(false)
      }, 300)
      return
    }

    try {
      const res = await apiClient.get(`/characters/${c.id}/speaking-profile`)
      setProfile(res.data as SpeakingProfile)
    } catch (e) {
      setError('Failed to load speaking profile')
    } finally {
      setLoading(false)
    }
  }

  const saveProfile = async () => {
    if (!selectedCharacter || !profile) return

    if (isDemo) {
      // 데모 모드: 저장할 수 없음을 알림
      setError('데모 모드에서는 변경사항을 저장할 수 없습니다. 로그인하여 데이터를 저장하세요.')
      return
    }

    setLoading(true)
    setError(null)
    setSuccess(null)
    try {
      const res = await apiClient.put(`/characters/${selectedCharacter.id}/speaking-profile`, profile)
      setProfile(res.data as SpeakingProfile)
      setSuccess('Saved successfully')
    } catch (e) {
      setError('Failed to save speaking profile')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6 md:p-10 transition-colors duration-200">
      <div className="max-w-6xl mx-auto">
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
                  </Link>하여 나만의 캐릭터를 만들고 저장하세요.
                </p>
              </div>
            </div>
          </div>
        )}

        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">말투 프로필 관리</h1>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400">{error}</div>
        )}
        {success && (
          <div className="mb-4 p-3 rounded bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-400">{success}</div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <aside className="md:col-span-1">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-4 transition-colors">
              <h2 className="text-lg font-semibold mb-3 text-gray-900 dark:text-white">캐릭터 목록</h2>
              <div className="space-y-2">
                {characters.map((c) => (
                  <button
                    key={c.id}
                    onClick={() => loadProfile(c)}
                    className={`w-full text-left px-3 py-2 rounded border transition ${
                      selectedCharacter?.id === c.id
                        ? 'border-blue-600 dark:border-blue-400 bg-gray-100 dark:bg-gray-700'
                        : 'border-gray-200 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
                    }`}
                  >
                    <div className="font-medium text-gray-900 dark:text-white">{c.name}</div>
                    <div className="text-xs text-gray-500 dark:text-gray-400">{c.characterId}</div>
                  </button>
                ))}
              </div>
            </div>
          </aside>

          <section className="md:col-span-2">
            {!selectedCharacter ? (
              <div className="p-10 text-center text-gray-500 dark:text-gray-400 bg-white dark:bg-gray-800 rounded-lg shadow transition-colors">
                왼쪽에서 캐릭터를 선택하세요.
              </div>
            ) : loading ? (
              <div className="p-10 text-center text-gray-500 dark:text-gray-400 bg-white dark:bg-gray-800 rounded-lg shadow transition-colors">로딩 중...</div>
            ) : profile ? (
              <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-5 transition-colors">
                <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-white">{selectedCharacter.name}</h2>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">말투 특징</label>
                    <textarea
                      className="w-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
                      rows={3}
                      value={profile.speakingStyle}
                      onChange={(e) => setProfile({ ...profile, speakingStyle: e.target.value })}
                      placeholder="예: 친근하고 밝은 캐주얼 톤"
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">자주 쓰는 어휘</label>
                      <textarea
                        className="w-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
                        rows={3}
                        value={profile.vocabulary || ''}
                        onChange={(e) => setProfile({ ...profile, vocabulary: e.target.value })}
                        placeholder="예: 진짜, 완전, 약간"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">톤 키워드</label>
                      <textarea
                        className="w-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
                        rows={3}
                        value={profile.toneKeywords || ''}
                        onChange={(e) => setProfile({ ...profile, toneKeywords: e.target.value })}
                        placeholder="예: 밝음, 명랑함, 친근함"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">예시 대사 (한 줄에 하나씩)</label>
                    <textarea
                      className="w-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
                      rows={5}
                      value={profile.examples || ''}
                      onChange={(e) => setProfile({ ...profile, examples: e.target.value })}
                      placeholder={'안녕! 반가워요.\n같이 가자!\n정말 좋다!'}
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">금지 어휘</label>
                      <textarea
                        className="w-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
                        rows={3}
                        value={profile.prohibitedWords || ''}
                        onChange={(e) => setProfile({ ...profile, prohibitedWords: e.target.value })}
                        placeholder="예: 과도한 경어, 비속어"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">문장 패턴 (한 줄에 하나씩)</label>
                      <textarea
                        className="w-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
                        rows={3}
                        value={profile.sentencePatterns || ''}
                        onChange={(e) => setProfile({ ...profile, sentencePatterns: e.target.value })}
                        placeholder={'~같아\n진짜 ~\n~해보자'}
                      />
                    </div>
                  </div>
                </div>

                <div className="pt-4 flex justify-end">
                  <button
                    onClick={saveProfile}
                    disabled={loading}
                    className={`px-4 py-2 rounded text-white font-medium transition-colors ${
                      loading ? 'bg-gray-400 dark:bg-gray-600' : 'bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600'
                    }`}
                  >
                    {loading ? '저장 중...' : '프로필 저장'}
                  </button>
                </div>
              </div>
            ) : null}
          </section>
        </div>
      </div>
    </main>
  )
}
