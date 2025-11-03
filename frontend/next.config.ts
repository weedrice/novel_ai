import type { NextConfig } from 'next';

// Bundle Analyzer 설정 (Task 93)
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});

const nextConfig: NextConfig = {
  reactStrictMode: true,

  // 성능 최적화
  compress: true, // gzip 압축 활성화

  // 빌드 최적화
  poweredByHeader: false, // X-Powered-By 헤더 제거 (보안)

  // Task 93: 프론트엔드 최적화
  // 이미지 최적화
  images: {
    formats: ['image/avif', 'image/webp'], // 최신 이미지 포맷 사용
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    minimumCacheTTL: 60, // 이미지 캐시 TTL (초)
  },

  // 번들 크기 최적화
  experimental: {
    optimizePackageImports: ['@heroicons/react', 'lucide-react'], // 자동 tree-shaking
  },

  // Webpack 최적화
  webpack: (config, { isServer }) => {
    // 프로덕션 빌드 최적화
    if (!isServer) {
      config.optimization = {
        ...config.optimization,
        splitChunks: {
          chunks: 'all',
          cacheGroups: {
            default: false,
            vendors: false,
            // React 및 관련 라이브러리
            react: {
              name: 'react-vendors',
              test: /[\\/]node_modules[\\/](react|react-dom|scheduler)[\\/]/,
              priority: 40,
              enforce: true,
            },
            // React Flow (그래프 라이브러리) - 별도 청크로 분리
            reactflow: {
              name: 'reactflow',
              test: /[\\/]node_modules[\\/](reactflow|@reactflow)[\\/]/,
              priority: 35,
              enforce: true,
            },
            // 나머지 라이브러리
            lib: {
              test: /[\\/]node_modules[\\/]/,
              name(module: any) {
                const packageName = module.context.match(
                  /[\\/]node_modules[\\/](.*?)([\\/]|$)/
                )?.[1];
                return `lib.${packageName?.replace('@', '')}`;
              },
              priority: 30,
            },
          },
        },
      };
    }
    return config;
  },

  // 개발 환경 설정
  onDemandEntries: {
    // 메모리 사용 최적화
    maxInactiveAge: 60 * 1000, // 1분
    pagesBufferLength: 5,
  },
};

export default withBundleAnalyzer(nextConfig);
