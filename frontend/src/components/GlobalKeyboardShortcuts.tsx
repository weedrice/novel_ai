'use client';

import { useState } from 'react';
import { useKeyboardShortcut } from '@/hooks/useKeyboardShortcut';
import { useTheme } from '@/contexts/ThemeContext';
import CommandPalette from './CommandPalette';
import KeyboardShortcutsHelp from './KeyboardShortcutsHelp';

/**
 * 전역 키보드 단축키 핸들러
 * layout.tsx에 추가하여 앱 전체에서 사용 가능
 */
export default function GlobalKeyboardShortcuts() {
  const { toggleTheme } = useTheme();
  const [showCommandPalette, setShowCommandPalette] = useState(false);
  const [showHelp, setShowHelp] = useState(false);

  // Ctrl/Cmd + K: 커맨드 팔레트
  useKeyboardShortcut('k', () => setShowCommandPalette(true), { ctrl: true });

  // Ctrl/Cmd + D: 다크 모드 토글
  useKeyboardShortcut('d', toggleTheme, { ctrl: true });

  // Ctrl/Cmd + /: 키보드 단축키 도움말
  useKeyboardShortcut('/', () => setShowHelp(true), { ctrl: true });

  return (
    <>
      <CommandPalette isOpen={showCommandPalette} onClose={() => setShowCommandPalette(false)} />
      <KeyboardShortcutsHelp isOpen={showHelp} onClose={() => setShowHelp(false)} />
    </>
  );
}
