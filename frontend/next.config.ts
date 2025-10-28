import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  reactStrictMode: true,

  // 성능 최적화
  compress: true, // gzip 압축 활성화

  // 빌드 최적화
  poweredByHeader: false, // X-Powered-By 헤더 제거 (보안)

  // 개발 환경 설정
  onDemandEntries: {
    // 메모리 사용 최적화
    maxInactiveAge: 60 * 1000, // 1분
    pagesBufferLength: 5,
  },
};

export default nextConfig;
