/**
 * 인물 노드 커스텀 컴포넌트
 */
'use client'

import React, { memo } from 'react'
import { Handle, Position, NodeProps } from 'reactflow'
import { Person, FAMILY_COLORS } from './types'

interface PersonNodeData extends Person {
  label: string
}

function PersonNode({ data, selected }: NodeProps<PersonNodeData>) {
  const family = data.family || '기타'
  const bgColor = FAMILY_COLORS[family] || FAMILY_COLORS['기타']

  return (
    <div
      className={`
        relative px-4 py-3 rounded-xl border-2 transition-all duration-200
        ${selected ? 'border-indigo-600 shadow-xl scale-105' : 'border-gray-300 shadow-md hover:shadow-lg hover:scale-102'}
      `}
      style={{
        background: bgColor,
        minWidth: '160px',
        maxWidth: '200px',
      }}
    >
      <Handle type="target" position={Position.Top} className="w-3 h-3 !bg-indigo-500" />

      <div className="text-center">
        {/* 이름 */}
        <div className="text-lg font-bold text-gray-900 mb-1">
          {data.name || data.label}
        </div>

        {/* 직함/나이 */}
        <div className="text-xs text-gray-600 space-y-0.5">
          {data.title && (
            <div className="font-medium">{data.title}</div>
          )}
          {data.age && (
            <div className="text-gray-500">{data.age}세</div>
          )}
        </div>

        {/* 가족 배지 */}
        {data.family && (
          <div className="mt-2">
            <span className="inline-block px-2 py-0.5 text-[10px] font-semibold rounded-full bg-white/60 text-gray-700">
              {data.family}
            </span>
          </div>
        )}
      </div>

      <Handle type="source" position={Position.Bottom} className="w-3 h-3 !bg-indigo-500" />
    </div>
  )
}

export default memo(PersonNode)
