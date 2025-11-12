'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useProject } from '@/contexts/ProjectContext'
import apiClient from '@/lib/api'
import Button from '@/components/ui/Button'
import Input from '@/components/ui/Input'
import Textarea from '@/components/ui/Textarea'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'
import Card from '@/components/Card'

interface Project {
  id: number
  name: string
  description: string
  owner?: {
    id: number
    username: string
    email: string
  }
  createdAt: string
  updatedAt: string
}

export default function ProjectSettingsPage() {
  const params = useParams()
  const router = useRouter()
  const projectId = Number(params.id)

  const [project, setProject] = useState<Project | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  // 폼 상태
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  // 삭제 확인
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [deleteConfirmText, setDeleteConfirmText] = useState('')

  useEffect(() => {
    loadProject()
  }, [projectId])

  const loadProject = async () => {
    setLoading(true)
    try {
      const response = await apiClient.get<Project>(`/projects/${projectId}`)
      setProject(response.data)
      setName(response.data.name)
      setDescription(response.data.description || '')
      setError(null)
    } catch (err: any) {
      console.error('Failed to load project:', err)
      setError(err.response?.data?.error || '프로젝트를 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleSaveProject = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!name.trim()) {
      setError('프로젝트 이름을 입력해주세요.')
      return
    }

    setSaving(true)
    setError(null)
    setSuccess(null)

    try {
      const response = await apiClient.put(`/projects/${projectId}`, {
        name: name.trim(),
        description: description.trim(),
      })

      setProject(response.data)
      setSuccess('프로젝트 설정이 저장되었습니다.')
    } catch (err: any) {
      console.error('Failed to update project:', err)
      setError(err.response?.data?.error || '프로젝트 업데이트에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteProject = async () => {
    if (deleteConfirmText !== project?.name) {
      setError('프로젝트 이름이 일치하지 않습니다.')
      return
    }

    setDeleting(true)
    setError(null)

    try {
      await apiClient.delete(`/projects/${projectId}`)
      router.push('/')
    } catch (err: any) {
      console.error('Failed to delete project:', err)
      setError(err.response?.data?.error || '프로젝트 삭제에 실패했습니다.')
    } finally {
      setDeleting(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center">
        <LoadingSpinner size="lg" message="프로젝트 설정 로딩 중..." />
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
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-2">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => router.push(`/projects/${projectId}`)}
            >
              ← 프로젝트로 돌아가기
            </Button>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
            프로젝트 설정
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-2">
            프로젝트 정보를 수정하고 관리하세요
          </p>
        </div>

        {error && <ErrorMessage message={error} onDismiss={() => setError(null)} />}
        {success && (
          <div className="mb-6 p-4 bg-green-50 dark:bg-green-900/20 border-l-4 border-green-500 dark:border-green-400 rounded-r-lg">
            <p className="text-sm text-green-700 dark:text-green-300">{success}</p>
          </div>
        )}

        <div className="space-y-6">
          {/* 기본 정보 */}
          <Card title="기본 정보">
            <form onSubmit={handleSaveProject} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  프로젝트 이름 *
                </label>
                <Input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="프로젝트 이름을 입력하세요"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  프로젝트 설명
                </label>
                <Textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="프로젝트에 대한 설명을 입력하세요 (선택)"
                  rows={4}
                />
              </div>

              <div className="flex gap-3">
                <Button type="submit" variant="primary" loading={saving} disabled={saving}>
                  저장
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={loadProject}
                  disabled={saving}
                >
                  되돌리기
                </Button>
              </div>
            </form>
          </Card>

          {/* 프로젝트 정보 */}
          <Card title="프로젝트 정보">
            <div className="space-y-3 text-sm">
              <div className="flex justify-between py-2 border-b border-gray-200 dark:border-gray-700">
                <span className="text-gray-600 dark:text-gray-400">프로젝트 ID</span>
                <span className="text-gray-900 dark:text-white font-mono">#{project.id}</span>
              </div>
              {project.owner && (
                <div className="flex justify-between py-2 border-b border-gray-200 dark:border-gray-700">
                  <span className="text-gray-600 dark:text-gray-400">소유자</span>
                  <span className="text-gray-900 dark:text-white font-medium">{project.owner.username}</span>
                </div>
              )}
              <div className="flex justify-between py-2 border-b border-gray-200 dark:border-gray-700">
                <span className="text-gray-600 dark:text-gray-400">생성일</span>
                <span className="text-gray-900 dark:text-white">
                  {new Date(project.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
              <div className="flex justify-between py-2">
                <span className="text-gray-600 dark:text-gray-400">최종 수정일</span>
                <span className="text-gray-900 dark:text-white">
                  {new Date(project.updatedAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
            </div>
          </Card>

          {/* 위험 구역 */}
          <Card title="위험 구역" className="border-red-200 dark:border-red-800">
            <div className="space-y-4">
              <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <div className="flex items-start gap-3">
                  <div className="text-red-500 dark:text-red-400 text-xl">⚠️</div>
                  <div>
                    <h3 className="text-sm font-medium text-red-800 dark:text-red-300 mb-2">
                      프로젝트 삭제
                    </h3>
                    <p className="text-sm text-red-700 dark:text-red-400 mb-3">
                      프로젝트를 삭제하면 모든 에피소드, 캐릭터, 장면 데이터가 영구적으로 삭제됩니다.
                      이 작업은 되돌릴 수 없습니다.
                    </p>

                    {!showDeleteConfirm ? (
                      <Button
                        variant="secondary"
                        onClick={() => setShowDeleteConfirm(true)}
                        className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/30"
                      >
                        프로젝트 삭제
                      </Button>
                    ) : (
                      <div className="space-y-3">
                        <div>
                          <label className="block text-sm font-medium text-red-700 dark:text-red-300 mb-2">
                            삭제를 확인하려면 프로젝트 이름 "{project.name}"을 입력하세요:
                          </label>
                          <Input
                            value={deleteConfirmText}
                            onChange={(e) => setDeleteConfirmText(e.target.value)}
                            placeholder={project.name}
                            className="border-red-300 dark:border-red-600"
                          />
                        </div>
                        <div className="flex gap-3">
                          <Button
                            variant="primary"
                            onClick={handleDeleteProject}
                            loading={deleting}
                            disabled={deleting || deleteConfirmText !== project.name}
                            className="bg-red-600 hover:bg-red-700 text-white"
                          >
                            영구 삭제
                          </Button>
                          <Button
                            variant="secondary"
                            onClick={() => {
                              setShowDeleteConfirm(false)
                              setDeleteConfirmText('')
                              setError(null)
                            }}
                            disabled={deleting}
                          >
                            취소
                          </Button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  )
}