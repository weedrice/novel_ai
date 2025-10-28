'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useTheme } from '@/contexts/ThemeContext';
import { useProject } from '@/contexts/ProjectContext';
import { useEscapeKey } from '@/hooks/useKeyboardShortcut';

interface Command {
  id: string;
  title: string;
  subtitle?: string;
  icon?: string;
  action: () => void;
  category: 'navigation' | 'action' | 'project';
}

interface CommandPaletteProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function CommandPalette({ isOpen, onClose }: CommandPaletteProps) {
  const router = useRouter();
  const { toggleTheme, theme } = useTheme();
  const { projects, selectProject, currentProject } = useProject();
  const [search, setSearch] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  // ESC 키로 닫기
  useEscapeKey(onClose, isOpen);

  // 포커스 관리
  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
      setSearch('');
      setSelectedIndex(0);
    }
  }, [isOpen]);

  // 커맨드 목록
  const commands: Command[] = [
    // Navigation
    {
      id: 'nav-home',
      title: '홈',
      subtitle: '메인 페이지로 이동',
      icon: '🏠',
      action: () => {
        router.push('/');
        onClose();
      },
      category: 'navigation',
    },
    {
      id: 'nav-graph',
      title: '캐릭터 관계 그래프',
      subtitle: '관계도 보기',
      icon: '🔗',
      action: () => {
        router.push('/graph');
        onClose();
      },
      category: 'navigation',
    },
    {
      id: 'nav-characters',
      title: '말투 프로필 관리',
      subtitle: '캐릭터 편집',
      icon: '👤',
      action: () => {
        router.push('/characters');
        onClose();
      },
      category: 'navigation',
    },
    {
      id: 'nav-scenes',
      title: '시나리오 편집',
      subtitle: '장면 관리',
      icon: '🎬',
      action: () => {
        router.push('/scenes');
        onClose();
      },
      category: 'navigation',
    },
    {
      id: 'nav-script',
      title: '스크립트 분석',
      subtitle: '텍스트 분석',
      icon: '📝',
      action: () => {
        router.push('/script-analyzer');
        onClose();
      },
      category: 'navigation',
    },
    // Actions
    {
      id: 'action-theme',
      title: theme === 'light' ? '다크 모드로 전환' : '라이트 모드로 전환',
      subtitle: 'Ctrl/Cmd+D',
      icon: theme === 'light' ? '🌙' : '☀️',
      action: () => {
        toggleTheme();
        onClose();
      },
      category: 'action',
    },
    // Projects
    ...projects.map((project) => ({
      id: `project-${project.id}`,
      title: project.name,
      subtitle: project.description || '프로젝트 전환',
      icon: currentProject?.id === project.id ? '✓' : '📁',
      action: () => {
        selectProject(project);
        onClose();
      },
      category: 'project' as const,
    })),
  ];

  // 검색 필터링
  const filteredCommands = commands.filter((cmd) =>
    cmd.title.toLowerCase().includes(search.toLowerCase()) ||
    cmd.subtitle?.toLowerCase().includes(search.toLowerCase())
  );

  // 키보드 네비게이션
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!isOpen) return;

      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev + 1) % filteredCommands.length);
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev - 1 + filteredCommands.length) % filteredCommands.length);
      } else if (e.key === 'Enter' && filteredCommands[selectedIndex]) {
        e.preventDefault();
        filteredCommands[selectedIndex].action();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, selectedIndex, filteredCommands]);

  // 검색어 변경 시 선택 인덱스 리셋
  useEffect(() => {
    setSelectedIndex(0);
  }, [search]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/50 dark:bg-black/70 flex items-start justify-center z-50 pt-20 px-4"
      onClick={onClose}
    >
      <div
        className="bg-white dark:bg-gray-800 rounded-lg shadow-2xl w-full max-w-2xl max-h-[60vh] overflow-hidden animate-slide-in"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 검색 입력 */}
        <div className="border-b border-gray-200 dark:border-gray-700 p-4">
          <div className="flex items-center space-x-3">
            <span className="text-gray-400 text-xl">🔍</span>
            <input
              ref={inputRef}
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="명령어 검색 또는 페이지 이동..."
              className="flex-1 bg-transparent outline-none text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500"
            />
            <kbd className="hidden sm:inline-block px-2 py-1 text-xs font-semibold text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 rounded">
              ESC
            </kbd>
          </div>
        </div>

        {/* 커맨드 목록 */}
        <div className="overflow-y-auto max-h-96 scrollbar-thin">
          {filteredCommands.length === 0 ? (
            <div className="p-8 text-center text-gray-500 dark:text-gray-400">
              검색 결과가 없습니다.
            </div>
          ) : (
            <div className="p-2">
              {['navigation', 'action', 'project'].map((category) => {
                const categoryCommands = filteredCommands.filter((cmd) => cmd.category === category);
                if (categoryCommands.length === 0) return null;

                return (
                  <div key={category} className="mb-4">
                    <div className="px-3 py-2 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wide">
                      {category === 'navigation' && '페이지 이동'}
                      {category === 'action' && '명령'}
                      {category === 'project' && '프로젝트'}
                    </div>
                    <div className="space-y-1">
                      {categoryCommands.map((cmd, index) => {
                        const globalIndex = filteredCommands.indexOf(cmd);
                        return (
                          <button
                            key={cmd.id}
                            onClick={cmd.action}
                            className={`w-full flex items-center space-x-3 px-3 py-3 rounded-lg text-left transition-colors ${
                              selectedIndex === globalIndex
                                ? 'bg-indigo-50 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300'
                                : 'hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-100'
                            }`}
                          >
                            <span className="text-2xl">{cmd.icon}</span>
                            <div className="flex-1 min-w-0">
                              <div className="font-medium truncate">{cmd.title}</div>
                              {cmd.subtitle && (
                                <div className="text-sm text-gray-500 dark:text-gray-400 truncate">
                                  {cmd.subtitle}
                                </div>
                              )}
                            </div>
                            {selectedIndex === globalIndex && (
                              <kbd className="hidden sm:inline-block px-2 py-1 text-xs font-semibold bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded">
                                ⏎
                              </kbd>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* 하단 힌트 */}
        <div className="border-t border-gray-200 dark:border-gray-700 p-3 bg-gray-50 dark:bg-gray-900/50">
          <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-1">
                <kbd className="px-2 py-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded">↑</kbd>
                <kbd className="px-2 py-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded">↓</kbd>
                <span>이동</span>
              </div>
              <div className="flex items-center space-x-1">
                <kbd className="px-2 py-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded">⏎</kbd>
                <span>선택</span>
              </div>
            </div>
            <div className="hidden sm:block">
              Ctrl/Cmd + K로 열기
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
