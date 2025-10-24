'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';

type Theme = 'light' | 'dark';

interface ThemeContextType {
  theme: Theme;
  toggleTheme: () => void;
  setTheme: (theme: Theme) => void;
  mounted: boolean;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>('light');
  const [mounted, setMounted] = useState(false);

  // 클라이언트 사이드에서만 실행
  useEffect(() => {
    setMounted(true);
    // 로컬스토리지에서 저장된 테마 불러오기
    const savedTheme = localStorage.getItem('theme') as Theme | null;
    // 시스템 다크 모드 감지
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');
    setThemeState(initialTheme);
  }, []);

  // theme 상태가 변경될 때마다 DOM 업데이트
  useEffect(() => {
    console.log('[ThemeContext] useEffect triggered. Mounted:', mounted, 'Theme:', theme);
    if (!mounted) {
      console.log('[ThemeContext] Not mounted yet, skipping DOM update');
      return;
    }

    // HTML 클래스에 다크 모드 적용
    console.log('[ThemeContext] Updating DOM. Theme:', theme);
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
      console.log('[ThemeContext] Added dark class to documentElement');
    } else {
      document.documentElement.classList.remove('dark');
      console.log('[ThemeContext] Removed dark class from documentElement');
    }

    // localStorage에 저장
    localStorage.setItem('theme', theme);
    console.log('[ThemeContext] Saved to localStorage:', theme);
  }, [theme, mounted]);

  const setTheme = (newTheme: Theme) => {
    console.log('[ThemeContext] setTheme called:', newTheme);
    setThemeState(newTheme);
    // DOM 업데이트와 localStorage 저장은 useEffect에서 처리됨
  };

  const toggleTheme = () => {
    console.log('[ThemeContext] toggleTheme called. Current theme:', theme);
    const newTheme = theme === 'light' ? 'dark' : 'light';
    console.log('[ThemeContext] Setting new theme:', newTheme);
    setTheme(newTheme);
  };

  // 항상 Provider를 제공하되, 마운트 전에는 기본값 사용
  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, setTheme, mounted }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
