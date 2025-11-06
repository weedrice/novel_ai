'use client'

import { useState, useEffect, useCallback } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useProject } from '@/contexts/ProjectContext'
import { Project } from '@/types/project'
import { Episode, getEpisodes, createEpisode, updateEpisode, deleteEpisode } from '@/lib/episode'
import { getProject } from '@/lib/project'
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

  // 에디터 상태
  const [editorContent, setEditorContent] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [saveTimeout, setSaveTimeout] = useState<NodeJS.Timeout | null>(null)

  // 새 에피소드 생성 모달
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newEpisodeTitle, setNewEpisodeTitle] = useState('')
  const [creating, setCreating] = useState(false)

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

  const handleSaveEpisode = useCallback(async () => {
    if (!selectedEpisode) return

    setIsSaving(true)
    try {
      const updated = await updateEpisode(selectedEpisode.id, {
        title: selectedEpisode.title,
        episodeOrder: selectedEpisode.episodeOrder,
        scriptText: editorContent,
      })

      const updatedEpisodes = episodes.map(ep =>
        ep.id === selectedEpisode.id ? updated : ep
      )
      setEpisodes(updatedEpisodes)
      setSelectedEpisode(updated)
      setError(null)
    } catch (err: any) {
      console.error('에피소드 저장 실패:', err)
      setError(err.response?.data?.error || '에피소드 저장에 실패했습니다.')
    } finally {
      setIsSaving(false)
    }
  }, [selectedEpisode, editorContent, episodes])

  // 자동 저장 (내용 변경 후 5초 후)
  useEffect(() => {
    if (!selectedEpisode || editorContent === (selectedEpisode.scriptText || '')) {
      return
    }

    if (saveTimeout) {
      clearTimeout(saveTimeout)
    }

    const timeout = setTimeout(() => {
      handleSaveEpisode()
    }, 5000)

    setSaveTimeout(timeout)

    return () => {
      if (saveTimeout) {
        clearTimeout(saveTimeout)
      }
    }
  }, [editorContent])

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
        <div className="w-80 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 overflow-y-auto">
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

        {/* 우측: 에디터 */}
        <div className="flex-1 flex flex-col bg-white dark:bg-gray-800">
          {selectedEpisode ? (
            <>
              {/* 에디터 헤더 */}
              <div className="border-b border-gray-200 dark:border-gray-700 p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                      {selectedEpisode.title}
                    </h3>
                    {isSaving && (
                      <span className="text-xs text-gray-500 dark:text-gray-400">
                        저장 중...
                      </span>
                    )}
                  </div>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={handleDeleteEpisode}
                    className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20"
                  >
                    삭제
                  </Button>
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
