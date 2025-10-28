'use client';

import { useEscapeKey } from '@/hooks/useKeyboardShortcut';

interface ShortcutGroup {
  title: string;
  shortcuts: {
    keys: string[];
    description: string;
  }[];
}

interface KeyboardShortcutsHelpProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function KeyboardShortcutsHelp({ isOpen, onClose }: KeyboardShortcutsHelpProps) {
  useEscapeKey(onClose, isOpen);

  const isMac = typeof navigator !== 'undefined' && navigator.platform.toUpperCase().indexOf('MAC') >= 0;
  const modifierKey = isMac ? '⌘' : 'Ctrl';

  const shortcutGroups: ShortcutGroup[] = [
    {
      title: '일반',
      shortcuts: [
        { keys: [modifierKey, 'K'], description: '커맨드 팔레트 열기' },
        { keys: [modifierKey, 'D'], description: '다크 모드 토글' },
        { keys: [modifierKey, '/'], description: '키보드 단축키 도움말' },
        { keys: ['ESC'], description: '모달/팔레트 닫기' },
      ],
    },
    {
      title: '네비게이션',
      shortcuts: [
        { keys: ['↑', '↓'], description: '커맨드 팔레트에서 항목 이동' },
        { keys: ['Enter'], description: '선택된 항목 실행' },
      ],
    },
    {
      title: '팁',
      shortcuts: [
        { keys: [modifierKey, 'K'], description: '페이지 빠른 이동을 위해 커맨드 팔레트를 사용하세요' },
        { keys: [], description: '입력 필드에서는 단축키가 비활성화됩니다' },
      ],
    },
  ];

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/50 dark:bg-black/70 flex items-center justify-center z-50 px-4"
      onClick={onClose}
    >
      <div
        className="bg-white dark:bg-gray-800 rounded-lg shadow-2xl w-full max-w-2xl max-h-[80vh] overflow-hidden animate-slide-in"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="border-b border-gray-200 dark:border-gray-700 p-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">⌨️ 키보드 단축키</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                효율적인 작업을 위한 단축키 모음
              </p>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
              aria-label="닫기"
            >
              <svg
                className="w-6 h-6 text-gray-500 dark:text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* 내용 */}
        <div className="overflow-y-auto max-h-[60vh] p-6 scrollbar-thin">
          <div className="space-y-6">
            {shortcutGroups.map((group) => (
              <div key={group.title}>
                <h3 className="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wide mb-3">
                  {group.title}
                </h3>
                <div className="space-y-2">
                  {group.shortcuts.map((shortcut, index) => (
                    <div
                      key={index}
                      className="flex items-center justify-between py-3 px-4 rounded-lg bg-gray-50 dark:bg-gray-700/50 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                    >
                      <span className="text-gray-700 dark:text-gray-300">{shortcut.description}</span>
                      {shortcut.keys.length > 0 && (
                        <div className="flex items-center space-x-1">
                          {shortcut.keys.map((key, keyIndex) => (
                            <kbd
                              key={keyIndex}
                              className="px-3 py-1.5 text-sm font-semibold text-gray-600 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded shadow-sm"
                            >
                              {key}
                            </kbd>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 하단 */}
        <div className="border-t border-gray-200 dark:border-gray-700 p-4 bg-gray-50 dark:bg-gray-900/50">
          <p className="text-xs text-gray-500 dark:text-gray-400 text-center">
            <kbd className="px-2 py-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded">ESC</kbd>
            {' '}키를 눌러 닫기
          </p>
        </div>
      </div>
    </div>
  );
}
