/**
 * 환경 변수 타입 안전성
 * 런타임에 환경 변수 검증 및 타입 안전한 접근 제공
 */

interface EnvConfig {
  API_BASE_URL: string;
  NODE_ENV: 'development' | 'production' | 'test';
}

/**
 * 환경 변수 검증 및 파싱
 */
function parseEnv(): EnvConfig {
  const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080';
  const nodeEnv = process.env.NODE_ENV || 'development';

  // 유효성 검증
  if (!apiBaseUrl) {
    throw new Error('NEXT_PUBLIC_API_BASE 환경 변수가 설정되지 않았습니다.');
  }

  // URL 형식 검증
  try {
    new URL(apiBaseUrl);
  } catch {
    throw new Error(`유효하지 않은 API_BASE_URL: ${apiBaseUrl}`);
  }

  return {
    API_BASE_URL: apiBaseUrl,
    NODE_ENV: nodeEnv as EnvConfig['NODE_ENV'],
  };
}

/**
 * 타입 안전한 환경 변수 export
 */
export const env = parseEnv();

/**
 * 개발 환경 여부 확인
 */
export const isDevelopment = env.NODE_ENV === 'development';

/**
 * 프로덕션 환경 여부 확인
 */
export const isProduction = env.NODE_ENV === 'production';

/**
 * 테스트 환경 여부 확인
 */
export const isTest = env.NODE_ENV === 'test';

/**
 * 디버그 로그 (개발 환경에서만)
 */
export const debugLog = (...args: any[]) => {
  if (isDevelopment) {
    console.log('[DEBUG]', ...args);
  }
};

/**
 * 환경 변수 정보 출력 (개발 환경에서만)
 */
if (isDevelopment) {
  console.log('Environment Configuration:', {
    API_BASE_URL: env.API_BASE_URL,
    NODE_ENV: env.NODE_ENV,
  });
}