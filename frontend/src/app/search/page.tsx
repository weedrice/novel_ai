'use client'

import { useState, useEffect } from 'react'
import { searchDialogues, Dialogue, semanticSearch, SemanticSearchResult } from '@/lib/search'
import apiClient from '@/lib/api'
import Card from '@/components/Card'
import Button from '@/components/ui/Button'
import Input from '@/components/ui/Input'
import Select from '@/components/ui/Select'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'
import { isDemoMode } from '@/data/demoData'
import Link from 'next/link'

interface Character {
  characterId: number
  name: string
}

interface Episode {
  id: number
  title: string
}

interface Scene {
  id: number
  description: string
  sceneNumber: number
}

export default function SearchPage() {
  const [searchMode, setSearchMode] = useState<'keyword' | 'semantic'>('keyword')
  const [query, setQuery] = useState('')
  const [characterId, setCharacterId] = useState<number | undefined>()
  const [episodeId, setEpisodeId] = useState<number | undefined>()
  const [sceneId, setSceneId] = useState<number | undefined>()

  const [results, setResults] = useState<Dialogue[]>([])
  const [semanticResults, setSemanticResults] = useState<SemanticSearchResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [characters, setCharacters] = useState<Character[]>([])
  const [episodes, setEpisodes] = useState<Episode[]>([])
  const [scenes, setScenes] = useState<Scene[]>([])
  const [isDemo, setIsDemo] = useState(false)

  // 필터 옵션 로드
  useEffect(() => {
    const demo = isDemoMode()
    setIsDemo(demo)

    if (!demo) {
      loadCharacters()
      loadEpisodes()
    }
  }, [])

  // 에피소드 선택 시 장면 로드
  useEffect(() => {
    if (episodeId) {
      loadScenes(episodeId)
    } else {
      setScenes([])
      setSceneId(undefined)
    }
  }, [episodeId])

  const loadCharacters = async () => {
    try {
      const response = await apiClient.get('/characters')
      setCharacters(response.data)
    } catch (err) {
      console.error('Failed to load characters:', err)
    }
  }

  const loadEpisodes = async () => {
    try {
      const response = await apiClient.get('/episodes')
      setEpisodes(response.data)
    } catch (err) {
      console.error('Failed to load episodes:', err)
    }
  }

  const loadScenes = async (episodeId: number) => {
    try {
      const response = await apiClient.get(`/scenes/episode/${episodeId}`)
      setScenes(response.data)
    } catch (err) {
      console.error('Failed to load scenes:', err)
    }
  }

  const handleSearch = async () => {
    if (searchMode === 'keyword') {
      // 키워드 검색
      if (!query && !characterId && !episodeId && !sceneId) {
        setError('검색어 또는 최소한 하나의 필터를 선택해주세요.')
        return
      }

      setLoading(true)
      setError(null)

      try {
        const results = await searchDialogues({
          query: query || undefined,
          characterId,
          episodeId,
          sceneId,
        })
        setResults(results)
        setSemanticResults([])
      } catch (err: any) {
        setError(`검색 실패: ${err.message}`)
      } finally {
        setLoading(false)
      }
    } else {
      // 의미 검색
      if (!query) {
        setError('검색어를 입력해주세요.')
        return
      }

      setLoading(true)
      setError(null)

      try {
        const results = await semanticSearch({
          query,
          limit: 20,
        })
        setSemanticResults(results)
        setResults([])
      } catch (err: any) {
        setError(`의미 검색 실패: ${err.message}`)
      } finally {
        setLoading(false)
      }
    }
  }

  const handleReset = () => {
    setQuery('')
    setCharacterId(undefined)
    setEpisodeId(undefined)
    setSceneId(undefined)
    setResults([])
    setSemanticResults([])
    setError(null)
  }

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6 md:p-10 transition-colors duration-200">
      <div className="max-w-6xl mx-auto">
        {/* 데모 모드 배너 */}
        {isDemo && (
          <div className="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 border-l-4 border-blue-500 dark:border-blue-400 rounded-r-lg">
            <p className="text-sm text-blue-700 dark:text-blue-300">
              <strong className="font-semibold">데모 모드</strong> - 이 기능을 사용하려면{" "}
              <Link href="/login" className="underline hover:text-blue-800 dark:hover:text-blue-200">
                로그인
              </Link>
              해주세요.
            </p>
          </div>
        )}

        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-2">
            🔍 대사 검색
          </h1>
          <p className="text-gray-600 dark:text-gray-400 text-lg mb-4">
            텍스트 검색 및 필터를 사용하여 대사를 찾아보세요
          </p>

          {/* 검색 모드 토글 */}
          <div className="flex gap-2">
            <button
              onClick={() => setSearchMode('keyword')}
              className={`px-4 py-2 rounded-lg font-semibold transition-all ${
                searchMode === 'keyword'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
            >
              📝 키워드 검색
            </button>
            <button
              onClick={() => setSearchMode('semantic')}
              className={`px-4 py-2 rounded-lg font-semibold transition-all ${
                searchMode === 'semantic'
                  ? 'bg-purple-600 text-white shadow-md'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
            >
              🧠 의미 검색 (AI)
            </button>
          </div>
        </div>

        {error && (
          <ErrorMessage
            message={error}
            onDismiss={() => setError(null)}
          />
        )}

        <Card
          title={searchMode === 'keyword' ? '검색 조건' : '의미 검색'}
          className="mb-6"
        >
          <div className="space-y-4">
            {/* 검색 모드 설명 */}
            {searchMode === 'semantic' && (
              <div className="p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg border border-purple-200 dark:border-purple-800">
                <p className="text-sm text-purple-800 dark:text-purple-200">
                  <strong>AI 의미 검색:</strong> 입력한 문장의 의미를 이해하여 유사한 대사를 찾습니다.
                  정확한 키워드가 없어도 비슷한 맥락의 대사를 찾을 수 있습니다.
                </p>
              </div>
            )}

            {/* 텍스트 검색 */}
            <div>
              <label className="block mb-2 font-semibold text-gray-700 dark:text-gray-300">
                {searchMode === 'keyword' ? '검색어' : '의미 검색 쿼리'}
              </label>
              <Input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder={
                  searchMode === 'keyword'
                    ? '대사 내용 검색...'
                    : '예: "사랑 고백하는 장면", "화해하는 대화" 등...'
                }
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleSearch()
                }}
              />
            </div>

            {/* 필터 옵션 (키워드 검색 전용) */}
            {searchMode === 'keyword' && (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* 캐릭터 필터 */}
              <div>
                <label className="block mb-2 font-semibold text-gray-700 dark:text-gray-300">
                  캐릭터
                </label>
                <Select
                  value={characterId?.toString() || ''}
                  onChange={(e) => setCharacterId(e.target.value ? Number(e.target.value) : undefined)}
                >
                  <option value="">전체</option>
                  {characters.map((char) => (
                    <option key={char.characterId} value={char.characterId}>
                      {char.name}
                    </option>
                  ))}
                </Select>
              </div>

              {/* 에피소드 필터 */}
              <div>
                <label className="block mb-2 font-semibold text-gray-700 dark:text-gray-300">
                  에피소드
                </label>
                <Select
                  value={episodeId?.toString() || ''}
                  onChange={(e) => setEpisodeId(e.target.value ? Number(e.target.value) : undefined)}
                >
                  <option value="">전체</option>
                  {episodes.map((ep) => (
                    <option key={ep.id} value={ep.id}>
                      {ep.title}
                    </option>
                  ))}
                </Select>
              </div>

              {/* 장면 필터 */}
              <div>
                <label className="block mb-2 font-semibold text-gray-700 dark:text-gray-300">
                  장면
                </label>
                <Select
                  value={sceneId?.toString() || ''}
                  onChange={(e) => setSceneId(e.target.value ? Number(e.target.value) : undefined)}
                  disabled={!episodeId}
                >
                  <option value="">전체</option>
                  {scenes.map((scene) => (
                    <option key={scene.id} value={scene.id}>
                      장면 {scene.sceneNumber}: {scene.description}
                    </option>
                  ))}
                </Select>
              </div>
            </div>
            )}

            {/* 버튼 */}
            <div className="flex gap-3">
              <Button onClick={handleSearch} loading={loading} disabled={loading}>
                검색
              </Button>
              <Button onClick={handleReset} variant="secondary">
                초기화
              </Button>
            </div>
          </div>
        </Card>

        {/* 검색 결과 */}
        {loading && (
          <div className="mt-6">
            <LoadingSpinner size="lg" message="검색 중..." />
          </div>
        )}

        {!loading && results.length > 0 && (
          <Card title={`검색 결과 (${results.length}개)`} className="mb-6">
            <div className="space-y-4">
              {results.map((dialogue) => (
                <div
                  key={dialogue.id}
                  className="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-semibold text-blue-600 dark:text-blue-400">
                          {dialogue.character?.name || 'Unknown'}
                        </span>
                        {dialogue.honorific && (
                          <span className="text-xs px-2 py-1 bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300 rounded">
                            {dialogue.honorific}
                          </span>
                        )}
                        {dialogue.emotion && (
                          <span className="text-xs px-2 py-1 bg-pink-100 dark:bg-pink-900/30 text-pink-700 dark:text-pink-300 rounded">
                            {dialogue.emotion}
                          </span>
                        )}
                      </div>
                      <p className="text-gray-800 dark:text-gray-200 leading-relaxed">
                        {dialogue.text}
                      </p>
                    </div>
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                    {dialogue.scene?.episode?.title && (
                      <span className="mr-3">
                        📖 {dialogue.scene.episode.title}
                      </span>
                    )}
                    {dialogue.scene && (
                      <span>
                        🎬 장면 {dialogue.scene.sceneNumber}: {dialogue.scene.description}
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}

        {/* 의미 검색 결과 */}
        {!loading && semanticResults.length > 0 && (
          <Card title={`AI 의미 검색 결과 (${semanticResults.length}개)`} className="mb-6">
            <div className="space-y-4">
              {semanticResults.map((result) => {
                let metadata: any = {}
                try {
                  metadata = JSON.parse(result.metadata)
                } catch (e) {
                  // Ignore parse errors
                }

                return (
                  <div
                    key={result.id}
                    className="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg border border-purple-200 dark:border-purple-700"
                  >
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="text-xs px-2 py-1 bg-purple-600 text-white rounded font-semibold">
                            {result.sourceType}
                          </span>
                          {metadata.characterName && (
                            <span className="font-semibold text-purple-600 dark:text-purple-400">
                              {metadata.characterName}
                            </span>
                          )}
                          {metadata.emotion && (
                            <span className="text-xs px-2 py-1 bg-pink-100 dark:bg-pink-900/30 text-pink-700 dark:text-pink-300 rounded">
                              {metadata.emotion}
                            </span>
                          )}
                          {metadata.honorific && (
                            <span className="text-xs px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 rounded">
                              {metadata.honorific}
                            </span>
                          )}
                        </div>
                        <p className="text-gray-800 dark:text-gray-200 leading-relaxed">
                          {result.textChunk}
                        </p>
                      </div>
                    </div>
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                      {metadata.episodeTitle && (
                        <span className="mr-3">
                          📖 {metadata.episodeTitle}
                        </span>
                      )}
                      {metadata.sceneNumber && (
                        <span className="mr-3">
                          🎬 장면 {metadata.sceneNumber}
                          {metadata.sceneLocation && `: ${metadata.sceneLocation}`}
                        </span>
                      )}
                      {metadata.intent && (
                        <span className="mr-3">
                          💡 {metadata.intent}
                        </span>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          </Card>
        )}

        {!loading && results.length === 0 && semanticResults.length === 0 && query && (
          <div className="text-center py-12 text-gray-500 dark:text-gray-400">
            검색 결과가 없습니다. 다른 검색어나 필터를 시도해보세요.
          </div>
        )}
      </div>
    </main>
  )
}
