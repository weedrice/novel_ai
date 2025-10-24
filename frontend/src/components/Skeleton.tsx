/**
 * 스켈레톤 로딩 UI 컴포넌트
 * 로딩 중임을 사용자에게 알리는 플레이스홀더 컴포넌트
 */

interface SkeletonProps {
  className?: string;
  variant?: 'text' | 'circular' | 'rectangular';
  animation?: 'pulse' | 'wave' | 'none';
}

export default function Skeleton({
  className = '',
  variant = 'rectangular',
  animation = 'pulse'
}: SkeletonProps) {
  const baseClasses = 'bg-gray-200 dark:bg-gray-700';

  const animationClasses = {
    pulse: 'animate-pulse',
    wave: 'animate-pulse-slow',
    none: '',
  };

  const variantClasses = {
    text: 'h-4 rounded',
    circular: 'rounded-full',
    rectangular: 'rounded-md',
  };

  return (
    <div
      className={`${baseClasses} ${variantClasses[variant]} ${animationClasses[animation]} ${className}`}
      role="status"
      aria-label="로딩 중"
    />
  );
}

/**
 * 카드 형태의 스켈레톤 UI
 */
export function SkeletonCard({ className = '' }: { className?: string }) {
  return (
    <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700 ${className}`}>
      <Skeleton className="h-6 w-3/4 mb-4" />
      <Skeleton className="h-4 w-full mb-2" variant="text" />
      <Skeleton className="h-4 w-5/6 mb-2" variant="text" />
      <Skeleton className="h-4 w-4/6" variant="text" />
    </div>
  );
}

/**
 * 리스트 아이템 형태의 스켈레톤 UI
 */
export function SkeletonListItem({ className = '' }: { className?: string }) {
  return (
    <div className={`flex items-center gap-4 p-4 ${className}`}>
      <Skeleton className="w-12 h-12" variant="circular" />
      <div className="flex-1">
        <Skeleton className="h-4 w-1/3 mb-2" variant="text" />
        <Skeleton className="h-3 w-2/3" variant="text" />
      </div>
    </div>
  );
}

/**
 * 테이블 행 형태의 스켈레톤 UI
 */
export function SkeletonTable({ rows = 5, className = '' }: { rows?: number; className?: string }) {
  return (
    <div className={`space-y-3 ${className}`}>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4">
          <Skeleton className="h-10 flex-1" />
          <Skeleton className="h-10 w-24" />
          <Skeleton className="h-10 w-24" />
        </div>
      ))}
    </div>
  );
}

/**
 * 이미지 플레이스홀더 스켈레톤 UI
 */
export function SkeletonImage({ className = '', aspectRatio = '16/9' }: { className?: string; aspectRatio?: string }) {
  return (
    <div className={`relative ${className}`} style={{ aspectRatio }}>
      <Skeleton className="absolute inset-0" />
    </div>
  );
}
