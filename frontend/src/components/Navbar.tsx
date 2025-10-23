'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useProject } from '@/contexts/ProjectContext';
import { getCurrentUser, logout } from '@/lib/auth';
import { createProject } from '@/lib/project';

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { currentProject, projects, selectProject, refreshProjects } = useProject();
  const user = getCurrentUser();

  const [showProjectDropdown, setShowProjectDropdown] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');
  const [newProjectDesc, setNewProjectDesc] = useState('');
  const [creating, setCreating] = useState(false);

  // 로그인/회원가입 페이지에서는 네비게이션 바를 표시하지 않음
  if (pathname === '/login' || pathname === '/signup') {
    return null;
  }

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);

    try {
      const newProject = await createProject({
        name: newProjectName,
        description: newProjectDesc,
      });

      await refreshProjects();
      selectProject(newProject);
      setShowCreateModal(false);
      setNewProjectName('');
      setNewProjectDesc('');
    } catch (error: any) {
      console.error('Failed to create project:', error);
      alert('프로젝트 생성에 실패했습니다.');
    } finally {
      setCreating(false);
    }
  };

  return (
    <>
      <nav className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            {/* 좌측: 로고 및 프로젝트 선택 */}
            <div className="flex items-center space-x-4">
              <Link href="/" className="text-xl font-bold text-gray-900">
                Novel AI
              </Link>

              {user && (
                <div className="relative">
                  <button
                    onClick={() => setShowProjectDropdown(!showProjectDropdown)}
                    className="flex items-center space-x-2 px-3 py-2 rounded-lg bg-gray-100 hover:bg-gray-200 transition-colors"
                  >
                    <span className="text-sm font-medium text-gray-700">
                      {currentProject ? currentProject.name : '프로젝트 선택'}
                    </span>
                    <svg
                      className={`w-4 h-4 transition-transform ${showProjectDropdown ? 'rotate-180' : ''}`}
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </button>

                  {showProjectDropdown && (
                    <div className="absolute left-0 mt-2 w-64 bg-white rounded-lg shadow-lg border z-50">
                      <div className="p-2 max-h-64 overflow-y-auto">
                        {projects.map((project) => (
                          <button
                            key={project.id}
                            onClick={() => {
                              selectProject(project);
                              setShowProjectDropdown(false);
                            }}
                            className={`w-full text-left px-3 py-2 rounded-md transition-colors ${
                              currentProject?.id === project.id
                                ? 'bg-indigo-50 text-indigo-700'
                                : 'hover:bg-gray-100'
                            }`}
                          >
                            <div className="font-medium">{project.name}</div>
                            {project.description && (
                              <div className="text-xs text-gray-500 truncate">{project.description}</div>
                            )}
                          </button>
                        ))}
                      </div>
                      <div className="border-t p-2">
                        <button
                          onClick={() => {
                            setShowProjectDropdown(false);
                            setShowCreateModal(true);
                          }}
                          className="w-full px-3 py-2 text-sm font-medium text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors"
                        >
                          + 새 프로젝트 만들기
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 우측: 사용자 정보 */}
            <div className="flex items-center space-x-4">
              {user ? (
                <>
                  <span className="text-sm text-gray-600">
                    {user.username}
                  </span>
                  <button
                    onClick={logout}
                    className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900"
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                <Link
                  href="/login"
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700"
                >
                  로그인
                </Link>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* 프로젝트 생성 모달 */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">새 프로젝트 만들기</h2>
            <form onSubmit={handleCreateProject} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  프로젝트 이름
                </label>
                <input
                  type="text"
                  required
                  value={newProjectName}
                  onChange={(e) => setNewProjectName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="예: 내 첫 소설"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명 (선택)
                </label>
                <textarea
                  value={newProjectDesc}
                  onChange={(e) => setNewProjectDesc(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  rows={3}
                  placeholder="프로젝트에 대한 간단한 설명"
                />
              </div>
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    setNewProjectName('');
                    setNewProjectDesc('');
                  }}
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
                  disabled={creating}
                >
                  취소
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg disabled:opacity-50"
                  disabled={creating}
                >
                  {creating ? '생성 중...' : '만들기'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}