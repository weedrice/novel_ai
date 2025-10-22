'use client'

/**
 * 에러 바운더리 컴포넌트
 * React 렌더링 중 발생하는 에러를 catch하고 fallback UI를 표시
 */

import React from 'react';
import { isDevelopment } from '@/lib/env';

interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<{ error: Error; resetError: () => void }>;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

/**
 * 기본 에러 fallback UI
 */
function DefaultErrorFallback({
  error,
  resetError,
}: {
  error: Error;
  resetError: () => void;
}) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div className="flex items-center justify-center w-12 h-12 mx-auto bg-red-100 rounded-full mb-4">
          <svg
            className="w-6 h-6 text-red-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 text-center mb-2">
          앗! 문제가 발생했습니다
        </h1>

        <p className="text-gray-600 text-center mb-6">
          예상치 못한 오류가 발생했습니다. 다시 시도해 주세요.
        </p>

        {isDevelopment && error && (
          <div className="mb-6 p-4 bg-gray-100 rounded-lg overflow-auto">
            <p className="text-sm font-semibold text-gray-700 mb-2">에러 정보:</p>
            <pre className="text-xs text-red-600 whitespace-pre-wrap">
              {error.message}
            </pre>
            {error.stack && (
              <pre className="text-xs text-gray-600 mt-2 whitespace-pre-wrap">
                {error.stack}
              </pre>
            )}
          </div>
        )}

        <div className="flex gap-3">
          <button
            onClick={resetError}
            className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            다시 시도
          </button>
          <button
            onClick={() => (window.location.href = '/')}
            className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
          >
            홈으로
          </button>
        </div>
      </div>
    </div>
  );
}

/**
 * 에러 바운더리 클래스 컴포넌트
 */
export class ErrorBoundary extends React.Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
    };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // 에러 로깅
    console.error('Error caught by ErrorBoundary:', error, errorInfo);

    // 프로덕션에서는 에러 모니터링 서비스로 전송
    // 예: Sentry.captureException(error, { extra: errorInfo });
    if (!isDevelopment) {
      // TODO: 에러 모니터링 서비스 연동
      this.logErrorToService(error, errorInfo);
    }
  }

  /**
   * 에러를 외부 서비스로 전송
   */
  private logErrorToService(error: Error, errorInfo: React.ErrorInfo) {
    // Sentry, LogRocket, Datadog 등과 연동
    try {
      // 예시: fetch를 사용한 간단한 로깅
      fetch('/api/log-error', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          error: {
            message: error.message,
            stack: error.stack,
          },
          errorInfo: {
            componentStack: errorInfo.componentStack,
          },
          timestamp: new Date().toISOString(),
          userAgent: navigator.userAgent,
        }),
      }).catch((err) => {
        console.error('Failed to log error:', err);
      });
    } catch (loggingError) {
      console.error('Error logging failed:', loggingError);
    }
  }

  /**
   * 에러 상태 초기화
   */
  resetError = () => {
    this.setState({
      hasError: false,
      error: null,
    });
  };

  render() {
    if (this.state.hasError && this.state.error) {
      const FallbackComponent = this.props.fallback || DefaultErrorFallback;

      return (
        <FallbackComponent error={this.state.error} resetError={this.resetError} />
      );
    }

    return this.props.children;
  }
}

/**
 * 에러 바운더리 훅 (함수형 컴포넌트용)
 */
export function useErrorHandler() {
  const [error, setError] = React.useState<Error | null>(null);

  React.useEffect(() => {
    if (error) {
      throw error;
    }
  }, [error]);

  return setError;
}