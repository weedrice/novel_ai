'use client'

import { useState, useEffect, useCallback } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useProject } from '@/contexts/ProjectContext'
import { Project } from '@/types/project'
import { Episode, getEpisodes, createEpisode, updateEpisode, deleteEpisode } from '@/lib/episode'
import { getProject } from '@/lib/project'
import apiClient from '@/lib/api'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'
import Button from '@/components/ui/Button'

export default function ProjectDetailPage() {
  const params = useParams()
  const router = useRouter()
  const projectId = Number(params.id)
  const { selectProject } = useProject()

  const [project, setProject] = useState<Project | null>(null)
  const [episodes, setEpisodes] = useState<Episode[]>([])
  const [selectedEpisode, setSelectedEpisode] = useState<Episode | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  // 에디터 상태
  const [editorContent, setEditorContent] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [saveTimeout, setSaveTimeout] = useState<NodeJS.Timeout | null>(null)

  // 제목 편집 상태
  const [isEditingTitle, setIsEditingTitle] = useState(false)
  const [editedTitle, setEditedTitle] = useState('')

  // 새 에피소드 생성 모달
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newEpisodeTitle, setNewEpisodeTitle] = useState('')
  const [creating, setCreating] = useState(false)

  // 분석 상태
  const [analyzing, setAnalyzing] = useState(false)
  const [analysisResult, setAnalysisResult] = useState<any>(null)
  const [analysisType, setAnalysisType] = useState<string | null>(null)

  useEffect(() => {
    loadProjectAndEpisodes()
  }, [projectId])

  const loadProjectAndEpisodes = async () => {
    setLoading(true)
    try {
      // 프로젝트 정보 로드
      const projectData = await getProject(projectId)
      setProject(projectData)

      // 현재 프로젝트로 설정 (백엔드가 이 프로젝트의 에피소드만 조회하도록)
      selectProject(projectData)

      // 에피소드 목록 로드
      const episodesData = await getEpisodes()
      setEpisodes(episodesData)

      setError(null)
    } catch (err: any) {
      console.error('프로젝트 로딩 실패:', err)
      setError(err.response?.data?.error || '프로젝트를 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleEpisodeSelect = (episode: Episode) => {
    setSelectedEpisode(episode)
    setEditorContent(episode.scriptText || '')
    setEditedTitle(episode.title)
    setIsEditingTitle(false)
  }

  const handleCreateEpisode = async () => {
    if (!newEpisodeTitle.trim()) {
      setError('에피소드 제목을 입력해주세요.')
      return
    }

    setCreating(true)
    try {
      const newEpisode = await createEpisode({
        title: newEpisodeTitle,
        episodeOrder: episodes.length + 1,
      })

      setEpisodes([...episodes, newEpisode])
      setNewEpisodeTitle('')
      setShowCreateModal(false)
      setSelectedEpisode(newEpisode)
      setEditorContent('')
      setError(null)
    } catch (err: any) {
      console.error('에피소드 생성 실패:', err)
      setError(err.response?.data?.error || '에피소드 생성에 실패했습니다.')
    } finally {
      setCreating(false)
    }
  }

  const handleDeleteEpisode = async () => {
    if (!selectedEpisode) return

    if (!confirm(`"${selectedEpisode.title}"을(를) 정말 삭제하시겠습니까?`)) {
      return
    }

    try {
      await deleteEpisode(selectedEpisode.id)

      const updatedEpisodes = episodes.filter(ep => ep.id !== selectedEpisode.id)
      setEpisodes(updatedEpisodes)
      setSelectedEpisode(null)
      setEditorContent('')
      setError(null)
    } catch (err: any) {
      console.error('에피소드 삭제 실패:', err)
      setError(err.response?.data?.error || '에피소드 삭제에 실패했습니다.')
    }
  }

  const handleSaveEpisode = useCallback(async (saveTitle = false) => {
    if (!selectedEpisode) return

    setIsSaving(true)
    try {
      const updated = await updateEpisode(selectedEpisode.id, {
        title: saveTitle ? editedTitle : selectedEpisode.title,
        episodeOrder: selectedEpisode.episodeOrder,
        scriptText: editorContent,
      })

      const updatedEpisodes = episodes.map(ep =>
        ep.id === selectedEpisode.id ? updated : ep
      )
      setEpisodes(updatedEpisodes)
      setSelectedEpisode(updated)
      setError(null)

      if (saveTitle) {
        setIsEditingTitle(false)
      }
    } catch (err: any) {
      console.error('에피소드 저장 실패:', err)
      setError(err.response?.data?.error || '에피소드 저장에 실패했습니다.')
    } finally {
      setIsSaving(false)
    }
  }, [selectedEpisode, editorContent, editedTitle, episodes])

  // 자동 저장 (내용 변경 후 3초 후, 무입력 시에만)
  useEffect(() => {
    if (!selectedEpisode) {
      return
    }

    // 내용이 변경되지 않았으면 저장하지 않음
    if (editorContent === (selectedEpisode.scriptText || '')) {
      return
    }

    if (saveTimeout) {
      clearTimeout(saveTimeout)
    }

    const timeout = setTimeout(() => {
      handleSaveEpisode()
    }, 3000) // 5초 → 3초로 단축

    setSaveTimeout(timeout)

    return () => {
      if (timeout) {
        clearTimeout(timeout)
      }
    }
  }, [editorContent, selectedEpisode, handleSaveEpisode])

  // 분석 함수들
  const handleAnalysis = async (type: 'summary' | 'characters' | 'scenes' | 'dialogues' | 'spell-check') => {
    if (!selectedEpisode) return

    setAnalyzing(true)
    setAnalysisType(type)
    setError(null)

    try {
      const response = await apiClient.post(`/episodes/${selectedEpisode.id}/analysis/${type}`)
      setAnalysisResult(response.data)
      setSuccess(`${getAnalysisTypeName(type)}이 완료되었습니다.`)
    } catch (err: any) {
      console.error(`Failed to analyze ${type}:`, err)
      setError(err.response?.data?.error || `${getAnalysisTypeName(type)}에 실패했습니다.`)
    } finally {
      setAnalyzing(false)
    }
  }

  const getAnalysisTypeName = (type: string) => {
    switch (type) {
      case 'summary': return 'AI 요약 생성'
      case 'characters': return '캐릭터 분석'
      case 'scenes': return '장면 추출'
      case 'dialogues': return '대사 분석'
      case 'spell-check': return '맞춤법 검사'
      default: return '분석'
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center">
        <LoadingSpinner size="lg" message="프로젝트 로딩 중..." />
      </div>
    )
  }

  if (!project) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center">
        <ErrorMessage
          message="프로젝트를 찾을 수 없습니다."
          onRetry={() => router.push('/')}
        />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => router.push('/')}
            >
              ← 돌아가기
            </Button>
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                {project.name}
              </h1>
              {project.description && (
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {project.description}
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex h-[calc(100vh-140px)]">
        {/* 좌측: 에피소드 리스트 */}
        <div className="w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 overflow-y-auto flex-shrink-0">
          <div className="p-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                에피소드 목록
              </h2>
              <button
                onClick={() => setShowCreateModal(true)}
                className="p-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white transition-colors"
                title="새 에피소드 추가"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              </button>
            </div>

            {error && (
              <ErrorMessage
                message={error}
                onDismiss={() => setError(null)}
              />
            )}

            {success && (
              <div className="mb-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                <p className="text-sm text-green-700 dark:text-green-300">{success}</p>
              </div>
            )}

            {episodes.length === 0 ? (
              <div className="text-center py-8 flex flex-col items-center">
                <div className="text-4xl mb-2">📝</div>
                <p className="text-gray-600 dark:text-gray-400 text-sm mb-4">
                  에피소드가 없습니다
                </p>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => setShowCreateModal(true)}
                >
                  첫 에피소드 만들기
                </Button>
              </div>
            ) : (
              <div className="space-y-2">
                {episodes.map((episode) => (
                  <button
                    key={episode.id}
                    onClick={() => handleEpisodeSelect(episode)}
                    className={`w-full text-left px-4 py-3 rounded-lg transition-colors ${
                      selectedEpisode?.id === episode.id
                        ? 'bg-indigo-50 dark:bg-indigo-900 text-indigo-700 dark:text-indigo-300'
                        : 'hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-100'
                    }`}
                  >
                    <div className="font-medium">{episode.title}</div>
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                      {episode.updatedAt ? new Date(episode.updatedAt).toLocaleDateString('ko-KR') : '날짜 없음'}
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 중앙: 에디터 */}
        <div className="flex-1 flex flex-col bg-white dark:bg-gray-800 min-w-0">

          {selectedEpisode ? (
            <>
              {/* 에디터 헤더 */}
              <div className="border-b border-gray-200 dark:border-gray-700 p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3 flex-1">
                    {isEditingTitle ? (
                      <div className="flex items-center gap-2 flex-1">
                        <input
                          type="text"
                          value={editedTitle}
                          onChange={(e) => setEditedTitle(e.target.value)}
                          className="flex-1 px-3 py-1.5 text-xl font-semibold border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                          autoFocus
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                              handleSaveEpisode(true)
                            } else if (e.key === 'Escape') {
                              setEditedTitle(selectedEpisode.title)
                              setIsEditingTitle(false)
                            }
                          }}
                        />
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleSaveEpisode(true)}
                          disabled={isSaving || !editedTitle.trim()}
                        >
                          저장
                        </Button>
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => {
                            setEditedTitle(selectedEpisode.title)
                            setIsEditingTitle(false)
                          }}
                        >
                          취소
                        </Button>
                      </div>
                    ) : (
                      <>
                        <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                          {selectedEpisode.title}
                        </h3>
                        <button
                          onClick={() => setIsEditingTitle(true)}
                          className="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
                          title="제목 편집"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                          </svg>
                        </button>
                      </>
                    )}
                    {isSaving && (
                      <span className="text-xs text-gray-500 dark:text-gray-400">
                        저장 중...
                      </span>
                    )}
                  </div>
                  {!isEditingTitle && (
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={handleDeleteEpisode}
                      className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20"
                    >
                      삭제
                    </Button>
                  )}
                </div>
              </div>

              {/* 에디터 본문 */}
              <div className="flex-1 overflow-y-auto p-6">
                <textarea
                  value={editorContent}
                  onChange={(e) => setEditorContent(e.target.value)}
                  className="w-full h-full min-h-[500px] px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none font-mono"
                  placeholder="에피소드 내용을 입력하세요..."
                />
              </div>
            </>
          ) : (
            <div className="flex-1 flex items-center justify-center text-gray-500 dark:text-gray-400">
              <div className="text-center">
                <div className="text-6xl mb-4">📖</div>
                <p>에피소드를 선택하거나 새로 만들어주세요</p>
              </div>
            </div>
          )}
        </div>

        {/* 우측: 애드온 영역 */}
        {selectedEpisode && (
          <div className="w-80 bg-gray-50 dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700 overflow-y-auto flex-shrink-0">
            <div className="p-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                도구
              </h3>

              <div className="space-y-4">
                {/* 통계 카드 */}
                <div className="bg-white dark:bg-gray-800 rounded-lg p-4 shadow-sm">
                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                    문서 통계
                  </h4>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">글자 수</span>
                      <span className="font-medium text-gray-900 dark:text-white">
                        {editorContent.length.toLocaleString()}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">줄 수</span>
                      <span className="font-medium text-gray-900 dark:text-white">
                        {editorContent.split('\n').length.toLocaleString()}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">단어 수</span>
                      <span className="font-medium text-gray-900 dark:text-white">
                        {editorContent.trim() ? editorContent.trim().split(/\s+/).length.toLocaleString() : 0}
                      </span>
                    </div>
                  </div>
                </div>

                {/* 빠른 액션 */}
                <div className="bg-white dark:bg-gray-800 rounded-lg p-4 shadow-sm">
                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                    빠른 액션
                  </h4>
                  <div className="space-y-2">
                    <button
                      onClick={() => handleAnalysis('summary')}
                      disabled={analyzing || !editorContent.trim()}
                      className="w-full px-3 py-2 text-left text-sm bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {analyzing && analysisType === 'summary' ? '⏳' : '📝'} AI 요약 생성
                    </button>
                    <button
                      onClick={() => handleAnalysis('characters')}
                      disabled={analyzing || !editorContent.trim()}
                      className="w-full px-3 py-2 text-left text-sm bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {analyzing && analysisType === 'characters' ? '⏳' : '🔍'} 캐릭터 분석
                    </button>
                    <button
                      onClick={() => handleAnalysis('scenes')}
                      disabled={analyzing || !editorContent.trim()}
                      className="w-full px-3 py-2 text-left text-sm bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {analyzing && analysisType === 'scenes' ? '⏳' : '🎬'} 장면 추출
                    </button>
                    <button
                      onClick={() => handleAnalysis('dialogues')}
                      disabled={analyzing || !editorContent.trim()}
                      className="w-full px-3 py-2 text-left text-sm bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {analyzing && analysisType === 'dialogues' ? '⏳' : '💬'} 대사 분석
                    </button>
                    <button
                      onClick={() => handleAnalysis('spell-check')}
                      disabled={analyzing || !editorContent.trim()}
                      className="w-full px-3 py-2 text-left text-sm bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {analyzing && analysisType === 'spell-check' ? '⏳' : '✏️'} 맞춤법 검사
                    </button>
                  </div>
                </div>

                {/* 분석 결과 */}
                {analysisResult && (
                  <div className="bg-white dark:bg-gray-800 rounded-lg p-4 shadow-sm">
                    <div className="flex items-center justify-between mb-3">
                      <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300">
                        {getAnalysisTypeName(analysisType || '')} 결과
                      </h4>
                      <button
                        onClick={() => {
                          setAnalysisResult(null)
                          setAnalysisType(null)
                          setSuccess(null)
                        }}
                        className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                      >
                        ✕
                      </button>
                    </div>
                    <div className="max-h-64 overflow-y-auto text-xs">
                      <pre className="whitespace-pre-wrap text-gray-600 dark:text-gray-400">
                        {JSON.stringify(analysisResult, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}

                {/* 메타데이터 */}
                <div className="bg-white dark:bg-gray-800 rounded-lg p-4 shadow-sm">
                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                    정보
                  </h4>
                  <div className="space-y-2 text-xs text-gray-600 dark:text-gray-400">
                    <div>
                      <div className="font-medium mb-1">생성일</div>
                      <div>{selectedEpisode.createdAt ? new Date(selectedEpisode.createdAt).toLocaleString('ko-KR') : '-'}</div>
                    </div>
                    <div>
                      <div className="font-medium mb-1">수정일</div>
                      <div>{selectedEpisode.updatedAt ? new Date(selectedEpisode.updatedAt).toLocaleString('ko-KR') : '-'}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 새 에피소드 생성 모달 */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
              새 에피소드 만들기
            </h2>

            <div className="space-y-4 mb-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  에피소드 제목 *
                </label>
                <input
                  type="text"
                  value={newEpisodeTitle}
                  onChange={(e) => setNewEpisodeTitle(e.target.value)}
                  placeholder="예: 1화 - 시작"
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  disabled={creating}
                  autoFocus
                />
              </div>
            </div>

            <div className="flex gap-3">
              <Button
                variant="primary"
                onClick={handleCreateEpisode}
                loading={creating}
                disabled={creating || !newEpisodeTitle.trim()}
                className="flex-1"
              >
                생성
              </Button>
              <Button
                variant="secondary"
                onClick={() => {
                  setShowCreateModal(false)
                  setNewEpisodeTitle('')
                  setError(null)
                }}
                disabled={creating}
                className="flex-1"
              >
                취소
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
