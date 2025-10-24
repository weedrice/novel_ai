/**
 * 공통 카드 컴포넌트
 * 기본 div 속성(onClick 등)을 모두 지원합니다.
 */
import React from 'react';

type CardProps = React.HTMLAttributes<HTMLDivElement> & {
  title?: string;
  className?: string;
  children: React.ReactNode;
};

export default function Card({ children, title, className = '', ...rest }: CardProps) {
  return (
    <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700 transition-colors duration-200 ${className}`} {...rest}>
      {title && <h2 className="text-xl font-bold mb-4 text-gray-800 dark:text-gray-100">{title}</h2>}
      {children}
    </div>
  );
}
