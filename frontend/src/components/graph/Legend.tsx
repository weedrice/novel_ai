/**
 * 관계 유형 범례 컴포넌트
 */
'use client'

import React from 'react'
import { RELATION_COLORS, RELATION_LABELS, RelationType } from './types'

interface LegendProps {
  className?: string
}

export default function Legend({ className = '' }: LegendProps) {
  return (
    <div className={`bg-white rounded-lg shadow-lg p-4 ${className}`}>
      <h3 className="text-sm font-bold text-gray-900 mb-3">관계 유형</h3>
      <div className="space-y-2">
        {(Object.entries(RELATION_LABELS) as [RelationType, string][]).map(([type, label]) => (
          <div key={type} className="flex items-center gap-2">
            <div
              className="w-8 h-1 rounded-full"
              style={{ backgroundColor: RELATION_COLORS[type] }}
            />
            <span className="text-xs text-gray-700">{label}</span>
          </div>
        ))}
      </div>

      <div className="mt-4 pt-4 border-t border-gray-200">
        <h4 className="text-xs font-semibold text-gray-700 mb-2">선 굵기</h4>
        <div className="text-[10px] text-gray-500 space-y-1">
          <div>얇음: 친밀도 낮음 (0-5)</div>
          <div>보통: 친밀도 중간 (6-7)</div>
          <div>굵음: 친밀도 높음 (8-10)</div>
        </div>
      </div>
    </div>
  )
}
