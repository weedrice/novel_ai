'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { isAuthenticated, getCurrentUser, User } from '@/lib/auth'
import { Project, CreateProjectRequest } from '@/types/project'
import apiClient from '@/lib/api'
import Button from '@/components/ui/Button'
import Card from '@/components/Card'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'

export default function Home() {
  const router = useRouter()
  const [user, setUser] = useState<User | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newProject, setNewProject] = useState<CreateProjectRequest>({ name: '', description: '' })
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    checkAuthAndLoadProjects()
  }, [])

  const checkAuthAndLoadProjects = async () => {
    setLoading(true)
    const authenticated = isAuthenticated()

    if (authenticated) {
      const currentUser = getCurrentUser()
      setUser(currentUser)
      await loadProjects()
    } else {
      setUser(null)
    }

    setLoading(false)
  }

  const loadProjects = async () => {
    try {
      const response = await apiClient.get<Project[]>('/projects')
      setProjects(response.data)
      setError(null)
    } catch (err: any) {
      console.error('프로젝트 로딩 실패:', err)
      setError(err.response?.data?.error || '프로젝트를 불러오는데 실패했습니다.')
    }
  }

  const handleCreateProject = async () => {
    if (!newProject.name.trim()) {
      setError('프로젝트 이름을 입력해주세요.')
      return
    }

    setCreating(true)
    setError(null)

    try {
      const response = await apiClient.post<Project>('/projects', newProject)
      setProjects([...projects, response.data])
      setShowCreateModal(false)
      setNewProject({ name: '', description: '' })
    } catch (err: any) {
      console.error('프로젝트 생성 실패:', err)
      setError(err.response?.data?.error || '프로젝트 생성에 실패했습니다.')
    } finally {
      setCreating(false)
    }
  }

  const handleProjectClick = (projectId: number) => {
    // TODO: 프로젝트 상세 페이지로 이동
    router.push(`/projects/${projectId}`)
  }

  // 로딩 중
  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center transition-colors duration-200">
        <LoadingSpinner size="lg" message="로딩 중..." />
      </div>
    )
  }

  // 로그인하지 않은 사용자: 웰컴 스크린
  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 transition-colors duration-200">
        <div className="container mx-auto px-4 py-16">
          <div className="max-w-4xl mx-auto text-center">
            {/* Hero Section */}
            <div className="mb-12">
              <h1 className="text-5xl md:text-6xl font-bold text-gray-900 dark:text-white mb-4">
                Novel AI
              </h1>
              <p className="text-xl md:text-2xl text-gray-700 dark:text-gray-300 mb-2">
                캐릭터 대사 톤 보조 시스템
              </p>
              <p className="text-lg text-gray-600 dark:text-gray-400">
                Character Dialogue Tone Assistant System
              </p>
            </div>

            {/* Features Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 transition-colors">
                <div className="text-4xl mb-4">🎭</div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                  캐릭터 관리
                </h3>
                <p className="text-gray-600 dark:text-gray-400">
                  캐릭터의 말투, 성격, 관계를 체계적으로 관리하세요
                </p>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 transition-colors">
                <div className="text-4xl mb-4">✨</div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                  AI 대사 생성
                </h3>
                <p className="text-gray-600 dark:text-gray-400">
                  LLM을 활용한 캐릭터별 맞춤 대사 자동 생성
                </p>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 transition-colors">
                <div className="text-4xl mb-4">📊</div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                  플롯 분석
                </h3>
                <p className="text-gray-600 dark:text-gray-400">
                  스토리 구조와 캐릭터 관계를 시각적으로 분석
                </p>
              </div>
            </div>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
              <Link href="/signup">
                <Button variant="primary" size="lg" className="w-full sm:w-auto">
                  회원가입하고 시작하기
                </Button>
              </Link>
              <Link href="/login">
                <Button variant="secondary" size="lg" className="w-full sm:w-auto">
                  로그인
                </Button>
              </Link>
            </div>

            {/* Demo Link */}
            <div className="mt-8">
              <p className="text-gray-600 dark:text-gray-400">
                먼저 살펴보고 싶으신가요?{' '}
                <Link href="/graph" className="text-indigo-600 dark:text-indigo-400 hover:underline font-semibold">
                  데모 모드로 둘러보기
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    )
  }

  // 로그인한 사용자: 프로젝트 목록
  return (
    <main className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 p-6 md:p-10 transition-colors duration-200">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-4">
            <div>
              <h1 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-2">
                내 프로젝트
              </h1>
              <p className="text-gray-600 dark:text-gray-400">
                안녕하세요, <span className="font-semibold text-indigo-600 dark:text-indigo-400">{user.username}</span>님!
              </p>
            </div>
            <Button
              variant="primary"
              size="lg"
              onClick={() => setShowCreateModal(true)}
            >
              + 새 프로젝트
            </Button>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <ErrorMessage
            message={error}
            onRetry={loadProjects}
            onDismiss={() => setError(null)}
          />
        )}

        {/* Project Grid */}
        {projects.length === 0 ? (
          <Card className="text-center py-12">
            <div className="text-6xl mb-4">📁</div>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
              프로젝트가 없습니다
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              새 프로젝트를 만들어서 시작하세요!
            </p>
            <Button variant="primary" onClick={() => setShowCreateModal(true)}>
              첫 프로젝트 만들기
            </Button>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects.map((project) => (
              <div
                key={project.id}
                onClick={() => handleProjectClick(project.id)}
                className="bg-white dark:bg-gray-800 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 cursor-pointer overflow-hidden group"
              >
                <div className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                        {project.name}
                      </h3>
                      {project.description && (
                        <p className="text-gray-600 dark:text-gray-400 text-sm line-clamp-2">
                          {project.description}
                        </p>
                      )}
                    </div>
                    <div className="text-3xl opacity-50 group-hover:opacity-100 transition-opacity">
                      📚
                    </div>
                  </div>

                  <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
                    <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                      <span>생성일: {new Date(project.createdAt).toLocaleDateString()}</span>
                      <span className="flex items-center gap-1">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Create Project Modal */}
        {showCreateModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl max-w-md w-full p-6 transition-colors">
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
                새 프로젝트 만들기
              </h2>

              <div className="space-y-4 mb-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    프로젝트 이름 *
                  </label>
                  <input
                    type="text"
                    value={newProject.name}
                    onChange={(e) => setNewProject({ ...newProject, name: e.target.value })}
                    placeholder="예: 내 첫 소설"
                    className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    disabled={creating}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    설명 (선택)
                  </label>
                  <textarea
                    value={newProject.description}
                    onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
                    placeholder="프로젝트에 대한 간단한 설명을 입력하세요"
                    rows={3}
                    className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
                    disabled={creating}
                  />
                </div>
              </div>

              <div className="flex gap-3">
                <Button
                  variant="primary"
                  onClick={handleCreateProject}
                  loading={creating}
                  disabled={creating || !newProject.name.trim()}
                  className="flex-1"
                >
                  생성
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => {
                    setShowCreateModal(false)
                    setNewProject({ name: '', description: '' })
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
    </main>
  )
}
