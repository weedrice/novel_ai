'use client'

import { useState, useEffect } from 'react'
import apiClient from '@/lib/api'
import Card from '@/components/Card'
import Button from '@/components/ui/Button'
import Input from '@/components/ui/Input'
import Select from '@/components/ui/Select'
import Textarea from '@/components/ui/Textarea'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'
import { isDemoMode } from '@/data/demoData'
import Link from 'next/link'

interface Episode {
  id: number
  title: string
  description?: string
  order: number
  scriptText?: string
  scriptFormat?: string
  analysisStatus?: string
  llmProvider?: string
}

interface AnalysisResult {
  characters?: any[]
  dialogues?: any[]
  scenes?: any[]
  relationships?: any[]
}

export default function EpisodesPage() {
  const [episodes, setEpisodes] = useState<Episode[]>([])
  const [selectedEpisode, setSelectedEpisode] = useState<Episode | null>(null)
  const [scriptText, setScriptText] = useState('')
  const [scriptFormat, setScriptFormat] = useState('novel')
  const [provider, setProvider] = useState('openai')
  const [loading, setLoading] = useState(false)
  const [analyzing, setAnalyzing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null)
  const [isDemo, setIsDemo] = useState(false)

  useEffect(() => {
    const demo = isDemoMode()
    setIsDemo(demo)

    if (!demo) {
      loadEpisodes()
    }
  }, [])

  const loadEpisodes = async () => {
    setLoading(true)
    setError(null)

    try {
      const response = await apiClient.get('/episodes')
      setEpisodes(response.data)
    } catch (err: any) {
      console.error('Failed to load episodes:', err)
      setError(`에피소드 로드 실패: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  const handleSelectEpisode = (episode: Episode) => {
    setSelectedEpisode(episode)
    setScriptText(episode.scriptText || '')
    setScriptFormat(episode.scriptFormat || 'novel')
    setProvider(episode.llmProvider || 'openai')
    setAnalysisResult(null)
  }

  const handleUploadAndAnalyze = async () => {
    if (!selectedEpisode) {
      setError('에피소드를 선택해주세요')
      return
    }

    if (!scriptText.trim()) {
      setError('스크립트 내용을 입력해주세요')
      return
    }

    setAnalyzing(true)
    setError(null)

    try {
      const response = await apiClient.post(
        `/episodes/${selectedEpisode.id}/upload-and-analyze-script`,
        {
          scriptText,
          scriptFormat,
          provider,
        }
      )

      setAnalysisResult(response.data.analysis)

      // 에피소드 목록 새로고침
      await loadEpisodes()

      alert('스크립트 분석이 완료되었습니다!')
    } catch (err: any) {
      console.error('Failed to analyze script:', err)
      setError(`스크립트 분석 실패: ${err.message}`)
    } finally {
      setAnalyzing(false)
    }
  }

  const handleLoadAnalysisResult = async () => {
    if (!selectedEpisode) return

    setLoading(true)
    setError(null)

    try {
      const response = await apiClient.get(`/episodes/${selectedEpisode.id}/script-analysis`)
      setAnalysisResult(response.data)
    } catch (err: any) {
      console.error('Failed to load analysis result:', err)
      setError(`분석 결과 로드 실패: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6 md:p-10 transition-colors duration-200">
      <div className="max-w-7xl mx-auto">
        {/* 데모 모드 배너 */}
        {isDemo && (
          <div className="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 border-l-4 border-blue-500 dark:border-blue-400 rounded-r-lg">
            <p className="text-sm text-blue-700 dark:text-blue-300">
              <strong className="font-semibold">데모 모드</strong> - 이 기능을 사용하려면{' '}
              <Link href="/login" className="underline hover:text-blue-800 dark:hover:text-blue-200">
                로그인
              </Link>
              해주세요.
            </p>
          </div>
        )}

        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-2">
            📚 에피소드 관리
          </h1>
          <p className="text-gray-600 dark:text-gray-400 text-lg">
            에피소드별 스크립트를 업로드하고 LLM으로 자동 분석하세요
          </p>
        </div>

        {error && <ErrorMessage message={error} onDismiss={() => setError(null)} />}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 왼쪽: 에피소드 목록 */}
          <div className="lg:col-span-1">
            <Card title="에피소드 목록">
              {loading && <LoadingSpinner size="md" message="로딩 중..." />}

              {!loading && episodes.length === 0 && (
                <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                  등록된 에피소드가 없습니다
                </div>
              )}

              {!loading && episodes.length > 0 && (
                <div className="space-y-2">
                  {episodes.map((episode) => (
                    <button
                      key={episode.id}
                      onClick={() => handleSelectEpisode(episode)}
                      className={`w-full text-left p-3 rounded-lg border transition-colors ${
                        selectedEpisode?.id === episode.id
                          ? 'bg-blue-50 dark:bg-blue-900/20 border-blue-500 dark:border-blue-400'
                          : 'bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750'
                      }`}
                    >
                      <div className="font-semibold text-gray-900 dark:text-white">
                        {episode.order}화: {episode.title}
                      </div>
                      {episode.description && (
                        <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                          {episode.description}
                        </div>
                      )}
                      {episode.analysisStatus && (
                        <div className="text-xs mt-2">
                          <span
                            className={`px-2 py-1 rounded ${
                              episode.analysisStatus === 'analyzed'
                                ? 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300'
                                : episode.analysisStatus === 'analyzing'
                                ? 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300'
                                : 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300'
                            }`}
                          >
                            {episode.analysisStatus === 'analyzed'
                              ? '✓ 분석 완료'
                              : episode.analysisStatus === 'analyzing'
                              ? '⏳ 분석 중'
                              : '미분석'}
                          </span>
                        </div>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </Card>
          </div>

          {/* 오른쪽: 스크립트 입력 및 분석 */}
          <div className="lg:col-span-2">
            {!selectedEpisode ? (
              <Card title="스크립트 업로드">
                <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                  왼쪽에서 에피소드를 선택해주세요
                </div>
              </Card>
            ) : (
              <div className="space-y-6">
                <Card title={`${selectedEpisode.order}화: ${selectedEpisode.title} - 스크립트`}>
                  <div className="space-y-4">
                    {/* 형식 선택 */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        스크립트 형식
                      </label>
                      <Select
                        value={scriptFormat}
                        onChange={(e) => setScriptFormat(e.target.value)}
                      >
                        <option value="novel">소설 (Novel)</option>
                        <option value="screenplay">시나리오 (Screenplay)</option>
                        <option value="description">묘사 (Description)</option>
                        <option value="dialogue">대화 (Dialogue)</option>
                      </Select>
                    </div>

                    {/* LLM 프로바이더 선택 */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        LLM 프로바이더
                      </label>
                      <Select
                        value={provider}
                        onChange={(e) => setProvider(e.target.value)}
                      >
                        <option value="openai">OpenAI (GPT-4)</option>
                        <option value="claude">Claude (Sonnet)</option>
                        <option value="gemini">Google Gemini</option>
                      </Select>
                    </div>

                    {/* 스크립트 입력 */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        스크립트 내용
                      </label>
                      <Textarea
                        value={scriptText}
                        onChange={(e) => setScriptText(e.target.value)}
                        placeholder="에피소드의 스크립트를 입력하세요..."
                        rows={15}
                        className="font-mono text-sm"
                      />
                    </div>

                    {/* 버튼 */}
                    <div className="flex gap-3">
                      <Button
                        onClick={handleUploadAndAnalyze}
                        disabled={analyzing || !scriptText.trim()}
                        variant="primary"
                        className="flex-1"
                      >
                        {analyzing ? (
                          <>
                            <LoadingSpinner size="sm" />
                            <span className="ml-2">분석 중...</span>
                          </>
                        ) : (
                          '스크립트 업로드 및 분석'
                        )}
                      </Button>

                      {selectedEpisode.analysisStatus === 'analyzed' && (
                        <Button
                          onClick={handleLoadAnalysisResult}
                          disabled={loading}
                          variant="secondary"
                        >
                          분석 결과 보기
                        </Button>
                      )}
                    </div>
                  </div>
                </Card>

                {/* 분석 결과 */}
                {analysisResult && (
                  <Card title="📊 분석 결과">
                    <div className="space-y-4">
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="text-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                          <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                            {analysisResult.characters?.length || 0}
                          </div>
                          <div className="text-sm text-gray-600 dark:text-gray-400">캐릭터</div>
                        </div>
                        <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                          <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                            {analysisResult.dialogues?.length || 0}
                          </div>
                          <div className="text-sm text-gray-600 dark:text-gray-400">대사</div>
                        </div>
                        <div className="text-center p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                          <div className="text-2xl font-bold text-purple-600 dark:text-purple-400">
                            {analysisResult.scenes?.length || 0}
                          </div>
                          <div className="text-sm text-gray-600 dark:text-gray-400">장면</div>
                        </div>
                        <div className="text-center p-4 bg-orange-50 dark:bg-orange-900/20 rounded-lg">
                          <div className="text-2xl font-bold text-orange-600 dark:text-orange-400">
                            {analysisResult.relationships?.length || 0}
                          </div>
                          <div className="text-sm text-gray-600 dark:text-gray-400">관계</div>
                        </div>
                      </div>

                      {/* 캐릭터 목록 */}
                      {analysisResult.characters && analysisResult.characters.length > 0 && (
                        <div>
                          <h3 className="text-lg font-semibold mb-2 text-gray-900 dark:text-white">
                            추출된 캐릭터
                          </h3>
                          <div className="space-y-2">
                            {analysisResult.characters.map((char: any, idx: number) => (
                              <div
                                key={idx}
                                className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg"
                              >
                                <div className="font-semibold text-gray-900 dark:text-white">
                                  {char.name}
                                </div>
                                {char.personality && (
                                  <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                                    성격: {char.personality}
                                  </div>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  </Card>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </main>
  )
}
