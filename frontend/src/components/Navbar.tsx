'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useProject } from '@/contexts/ProjectContext';
import { useTheme } from '@/contexts/ThemeContext';
import { getCurrentUser, logout } from '@/lib/auth';
import { createProject } from '@/lib/project';

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { currentProject, projects, selectProject, refreshProjects } = useProject();
  const { theme, toggleTheme, mounted } = useTheme();
  const user = getCurrentUser();

  const [showProjectDropdown, setShowProjectDropdown] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');
  const [newProjectDesc, setNewProjectDesc] = useState('');
  const [creating, setCreating] = useState(false);

  // 로그인/회원가입 페이지에서는 네비게이션 바를 표시하지 않음
  if (pathname === '/login' || pathname === '/signup') {
    return null;
  }

  // 비로그인 상태의 홈 페이지에서는 네비게이션 바를 표시하지 않음
  if (pathname === '/' && !user) {
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
      <nav className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700 transition-colors duration-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            {/* 좌측: 로고 */}
            <div className="flex items-center space-x-2 sm:space-x-4">
              <Link href="/" className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white">
                Novel AI
              </Link>
            </div>

            {/* 우측: 다크 모드 토글, 사용자 정보, 모바일 메뉴 버튼 */}
            <div className="flex items-center space-x-2 sm:space-x-4">
              {/* 다크 모드 토글 - 클라이언트에서만 렌더링 */}
              {mounted && (
                <button
                  onClick={toggleTheme}
                  className="p-2 rounded-lg bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors duration-200"
                  aria-label="테마 전환"
                >
                  {theme === 'light' ? (
                    <svg className="w-5 h-5 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                    </svg>
                  ) : (
                    <svg className="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                    </svg>
                  )}
                </button>
              )}

              {/* Desktop only - 사용자 정보 */}
              {user ? (
                <>
                  <span className="hidden md:inline text-sm text-gray-600 dark:text-gray-300">
                    {user.username}
                  </span>
                  <button
                    onClick={logout}
                    className="hidden sm:block px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                <Link
                  href="/login"
                  className="hidden sm:block px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700"
                >
                  로그인
                </Link>
              )}

              {/* Mobile menu button */}
              <button
                onClick={() => setShowMobileMenu(!showMobileMenu)}
                className="sm:hidden p-2 rounded-lg bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
                aria-label="메뉴"
              >
                {showMobileMenu ? (
                  <svg className="w-6 h-6 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                ) : (
                  <svg className="w-6 h-6 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                )}
              </button>
            </div>
          </div>

          {/* Mobile menu */}
          {showMobileMenu && (
            <div className="sm:hidden py-4 border-t border-gray-200 dark:border-gray-700">
              {user && (
                <>
                  {/* 프로젝트 선택 - Mobile */}
                  <div className="mb-4">
                    <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2 px-2">프로젝트</label>
                    <select
                      value={currentProject?.id || ''}
                      onChange={(e) => {
                        const project = projects.find(p => p.id === Number(e.target.value));
                        if (project) selectProject(project);
                      }}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                      <option value="">프로젝트 선택</option>
                      {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                          {project.name}
                        </option>
                      ))}
                    </select>
                    <button
                      onClick={() => {
                        setShowMobileMenu(false);
                        setShowCreateModal(true);
                      }}
                      className="w-full mt-2 px-3 py-2 text-sm font-medium text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-900/30 hover:bg-indigo-100 dark:hover:bg-indigo-900/50 rounded-lg transition-colors"
                    >
                      + 새 프로젝트 만들기
                    </button>
                  </div>

                  {/* 사용자 정보 - Mobile */}
                  <div className="mb-3 px-2">
                    <p className="text-sm text-gray-600 dark:text-gray-300 mb-2">
                      {user.username}
                    </p>
                    <button
                      onClick={() => {
                        setShowMobileMenu(false);
                        logout();
                      }}
                      className="w-full px-4 py-2 text-sm font-medium text-white bg-gray-600 rounded-lg hover:bg-gray-700"
                    >
                      로그아웃
                    </button>
                  </div>
                </>
              )}

              {!user && (
                <Link
                  href="/login"
                  onClick={() => setShowMobileMenu(false)}
                  className="block w-full px-4 py-2 text-center text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700"
                >
                  로그인
                </Link>
              )}
            </div>
          )}
        </div>
      </nav>

      {/* 프로젝트 생성 모달 */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-4 sm:p-6 w-full max-w-md mx-4">
            <h2 className="text-lg sm:text-xl font-bold mb-4 text-gray-900 dark:text-white">새 프로젝트 만들기</h2>
            <form onSubmit={handleCreateProject} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  프로젝트 이름
                </label>
                <input
                  type="text"
                  required
                  value={newProjectName}
                  onChange={(e) => setNewProjectName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="예: 내 첫 소설"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  설명 (선택)
                </label>
                <textarea
                  value={newProjectDesc}
                  onChange={(e) => setNewProjectDesc(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
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
                  className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
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