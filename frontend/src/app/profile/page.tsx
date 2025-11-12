'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import apiClient from '@/lib/api'
import Button from '@/components/ui/Button'
import Input from '@/components/ui/Input'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'
import Card from '@/components/Card'

interface UserProfile {
  id: number
  username: string
  name: string
  email: string
  role: string
  createdAt: string
  updatedAt: string
}

export default function ProfilePage() {
  const router = useRouter()
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  // 프로필 편집
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')

  // 비밀번호 변경
  const [showPasswordChange, setShowPasswordChange] = useState(false)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  useEffect(() => {
    loadProfile()
  }, [])

  const loadProfile = async () => {
    setLoading(true)
    try {
      const response = await apiClient.get<UserProfile>('/users/me')
      setProfile(response.data)
      setName(response.data.name || '')
      setEmail(response.data.email || '')
      setError(null)
    } catch (err: any) {
      console.error('Failed to load profile:', err)
      setError(err.response?.data?.error || '프로필을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    setSuccess(null)

    try {
      await apiClient.put('/users/me', { name, email })
      await loadProfile()
      setSuccess('프로필이 업데이트되었습니다.')
    } catch (err: any) {
      console.error('Failed to update profile:', err)
      setError(err.response?.data?.error || '프로필 업데이트에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()

    if (newPassword !== confirmPassword) {
      setError('새 비밀번호가 일치하지 않습니다.')
      return
    }

    if (newPassword.length < 6) {
      setError('비밀번호는 최소 6자 이상이어야 합니다.')
      return
    }

    setSaving(true)
    setError(null)
    setSuccess(null)

    try {
      await apiClient.put('/users/me/password', {
        currentPassword,
        newPassword,
        confirmNewPassword: confirmPassword,
      })
      setSuccess('비밀번호가 변경되었습니다.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setShowPasswordChange(false)
    } catch (err: any) {
      console.error('Failed to change password:', err)
      setError(err.response?.data?.error || '비밀번호 변경에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center">
        <LoadingSpinner size="lg" message="프로필 로딩 중..." />
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center">
        <ErrorMessage
          message="프로필을 찾을 수 없습니다."
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
            <Button variant="secondary" size="sm" onClick={() => router.push('/')}>
              ← 돌아가기
            </Button>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">내 프로필</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-2">
            계정 정보 및 설정을 관리하세요
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
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  사용자명
                </label>
                <Input value={profile.username} disabled />
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  사용자명은 변경할 수 없습니다
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  이름
                </label>
                <Input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="실명을 입력하세요 (선택)"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  이메일 *
                </label>
                <Input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="email@example.com"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  권한
                </label>
                <Input value={profile.role} disabled />
              </div>

              <div className="flex gap-3">
                <Button type="submit" variant="primary" loading={saving} disabled={saving}>
                  저장
                </Button>
              </div>
            </form>
          </Card>

          {/* 비밀번호 변경 */}
          <Card title="보안">
            {!showPasswordChange ? (
              <Button variant="secondary" onClick={() => setShowPasswordChange(true)}>
                비밀번호 변경
              </Button>
            ) : (
              <form onSubmit={handleChangePassword} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    현재 비밀번호 *
                  </label>
                  <Input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    새 비밀번호 *
                  </label>
                  <Input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="최소 6자 이상"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    새 비밀번호 확인 *
                  </label>
                  <Input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="새 비밀번호를 다시 입력하세요"
                    required
                  />
                </div>

                <div className="flex gap-3">
                  <Button type="submit" variant="primary" loading={saving} disabled={saving}>
                    비밀번호 변경
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={() => {
                      setShowPasswordChange(false)
                      setCurrentPassword('')
                      setNewPassword('')
                      setConfirmPassword('')
                      setError(null)
                    }}
                  >
                    취소
                  </Button>
                </div>
              </form>
            )}
          </Card>

          {/* 계정 정보 */}
          <Card title="계정 정보">
            <div className="space-y-3 text-sm">
              <div className="flex justify-between py-2 border-b border-gray-200 dark:border-gray-700">
                <span className="text-gray-600 dark:text-gray-400">가입일</span>
                <span className="text-gray-900 dark:text-white">
                  {new Date(profile.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
              <div className="flex justify-between py-2">
                <span className="text-gray-600 dark:text-gray-400">최종 수정일</span>
                <span className="text-gray-900 dark:text-white">
                  {new Date(profile.updatedAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  )
}
