'use client';

import { useEffect, useState } from 'react';
import { usePathname } from 'next/navigation';
import { validateStoredToken, isTokenExpired, getTokenExpiryTime } from '@/lib/auth';
import { useToast } from '@/contexts/ToastContext';

/**
 * 인증 토큰 유효성 검증 컴포넌트
 * 앱 초기화 시 및 페이지 이동 시 토큰 만료 여부를 체크합니다.
 * 토큰 만료 5분 전 알림을 표시합니다.
 */
export default function AuthValidator() {
  const pathname = usePathname();
  const { showToast } = useToast();
  const [hasShownWarning, setHasShownWarning] = useState(false);

  useEffect(() => {
    // 로그인/회원가입 페이지에서는 검증 스킵
    if (pathname === '/login' || pathname === '/signup') {
      return;
    }

    // 토큰 유효성 검증 (만료 시 자동 로그아웃)
    validateStoredToken();

    // 주기적 토큰 체크 (1분마다)
    const interval = setInterval(() => {
      const token = localStorage.getItem('token');
      if (!token) return;

      const expiryTime = getTokenExpiryTime(token);
      if (!expiryTime) return;

      const now = Math.floor(Date.now() / 1000);
      const timeLeft = expiryTime - now;

      // 토큰 만료 5분 전 경고 (한 번만)
      if (timeLeft > 0 && timeLeft <= 5 * 60 && !hasShownWarning) {
        const minutes = Math.floor(timeLeft / 60);
        showToast(
          `로그인 세션이 ${minutes}분 후 만료됩니다. 작업을 저장해주세요.`,
          'warning'
        );
        setHasShownWarning(true);
      }

      // 토큰 유효성 재검증
      validateStoredToken();
    }, 60 * 1000); // 1분

    return () => clearInterval(interval);
  }, [pathname, hasShownWarning, showToast]);

  return null; // UI를 렌더링하지 않음
}
