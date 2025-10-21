'use client'

import { useEffect, useState } from 'react'

const API = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080'

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

  useEffect(() => {
    ;(async () => {
      try {
        const res = await fetch(`${API}/characters`)
        const data = await res.json()
        setCharacters(Array.isArray(data) ? data : [])
      } catch (e) {
        setError('Failed to load characters')
      }
    })()
  }, [])

  const loadProfile = async (c: Character) => {
    setSelectedCharacter(c)
    setLoading(true)
    setError(null)
    setSuccess(null)
    try {
      const res = await fetch(`${API}/characters/${c.id}/speaking-profile`)
      const data = await res.json()
      setProfile(data as SpeakingProfile)
    } catch (e) {
      setError('Failed to load speaking profile')
    } finally {
      setLoading(false)
    }
  }

  const saveProfile = async () => {
    if (!selectedCharacter || !profile) return
    setLoading(true)
    setError(null)
    setSuccess(null)
    try {
      const res = await fetch(`${API}/characters/${selectedCharacter.id}/speaking-profile`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profile),
      })
      if (!res.ok) throw new Error('Save failed')
      const updated = await res.json()
      setProfile(updated as SpeakingProfile)
      setSuccess('Saved successfully')
    } catch (e) {
      setError('Failed to save speaking profile')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-gray-50 p-6 md:p-10">
      <div className="max-w-6xl mx-auto">
        <div className="flex items-center gap-4 mb-6">
          <button
            onClick={() => window.location.href = '/'}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-colors shadow-sm"
          >
            홈으로
          </button>
          <h1 className="text-3xl font-bold text-gray-900">말투 프로필 관리</h1>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded bg-red-50 border border-red-200 text-red-700">{error}</div>
        )}
        {success && (
          <div className="mb-4 p-3 rounded bg-green-50 border border-green-200 text-green-700">{success}</div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <aside className="md:col-span-1">
            <div className="bg-white rounded-lg shadow p-4">
              <h2 className="text-lg font-semibold mb-3">캐릭터 목록</h2>
              <div className="space-y-2">
                {characters.map((c) => (
                  <button
                    key={c.id}
                    onClick={() => loadProfile(c)}
                    className={`w-full text-left px-3 py-2 rounded border transition ${
                      selectedCharacter?.id === c.id
                        ? 'border-blue-600 bg-gray-100'
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <div className="font-medium">{c.name}</div>
                    <div className="text-xs text-gray-500">{c.characterId}</div>
                  </button>
                ))}
              </div>
            </div>
          </aside>

          <section className="md:col-span-2">
            {!selectedCharacter ? (
              <div className="p-10 text-center text-gray-500 bg-white rounded-lg shadow">
                왼쪽에서 캐릭터를 선택하세요.
              </div>
            ) : loading ? (
              <div className="p-10 text-center text-gray-500 bg-white rounded-lg shadow">로딩 중...</div>
            ) : profile ? (
              <div className="bg-white rounded-lg shadow p-5">
                <h2 className="text-xl font-semibold mb-4">{selectedCharacter.name}</h2>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">말투 특징</label>
                    <textarea
                      className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={3}
                      value={profile.speakingStyle}
                      onChange={(e) => setProfile({ ...profile, speakingStyle: e.target.value })}
                      placeholder="예: 친근하고 밝은 캐주얼 톤"
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">자주 쓰는 어휘</label>
                      <textarea
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        rows={3}
                        value={profile.vocabulary || ''}
                        onChange={(e) => setProfile({ ...profile, vocabulary: e.target.value })}
                        placeholder="예: 진짜, 완전, 약간"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">톤 키워드</label>
                      <textarea
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        rows={3}
                        value={profile.toneKeywords || ''}
                        onChange={(e) => setProfile({ ...profile, toneKeywords: e.target.value })}
                        placeholder="예: 밝음, 명랑함, 친근함"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">예시 대사 (한 줄에 하나씩)</label>
                    <textarea
                      className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={5}
                      value={profile.examples || ''}
                      onChange={(e) => setProfile({ ...profile, examples: e.target.value })}
                      placeholder={'안녕! 반가워요.\n같이 가자!\n정말 좋다!'}
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">금지 어휘</label>
                      <textarea
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        rows={3}
                        value={profile.prohibitedWords || ''}
                        onChange={(e) => setProfile({ ...profile, prohibitedWords: e.target.value })}
                        placeholder="예: 과도한 경어, 비속어"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">문장 패턴 (한 줄에 하나씩)</label>
                      <textarea
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                    className={`px-4 py-2 rounded text-white font-medium ${
                      loading ? 'bg-gray-400' : 'bg-blue-600 hover:bg-blue-700'
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
