/**
 * 인물 관계도 페이지 (개선 버전)
 * - React Flow + dagre 레이아웃
 * - 커스텀 노드 (PersonNode)
 * - 범례 (Legend)
 * - 레이아웃 전환 (수평/수직)
 * - 향상된 UX/UI
 */
'use client'

import React, { useEffect, useMemo, useState, useCallback, useRef } from 'react'
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  Edge,
  MarkerType,
  Node,
  addEdge,
  useEdgesState,
  useNodesState,
  Connection,
  ReactFlowProvider,
  Panel,
  useReactFlow,
} from 'reactflow'
import 'reactflow/dist/style.css'
import ErrorMessage from '@/components/ErrorMessage'
import LoadingSpinner from '@/components/LoadingSpinner'
import Button from '@/components/ui/Button'
import PersonNode from '@/components/features/graph/PersonNode'
import Legend from '@/components/features/graph/Legend'
import { applyDagreLayout, LayoutDirection } from '@/components/features/graph/utils/layout'
import { Person, Relation, RelationType, RELATION_COLORS } from '@/components/features/graph/types'
import apiClient from '@/lib/api'

// 노드 타입 등록
const nodeTypes = {
  person: PersonNode,
}

type GraphNode = {
  id: string
  label: string
}

type GraphEdge = {
  id: string
  source: string
  target: string
  label?: string
  closeness?: number
}

function GraphPageContent() {
  const [nodes, setNodes, onNodesChange] = useNodesState<Person>([])
  const [edges, setEdges, onEdgesChange] = useEdgesState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [layoutDirection, setLayoutDirection] = useState<LayoutDirection>('TB')
  const [selectedNode, setSelectedNode] = useState<Node<Person> | null>(null)
  const [selectedEdge, setSelectedEdge] = useState<Edge | null>(null)
  const reactFlowInstance = useReactFlow()

  const fetchGraph = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await apiClient.get('/relationships/graph')
      const data = res.data

      const rawNodes: GraphNode[] = Array.isArray(data?.nodes) ? data.nodes : []
      const rawEdges: GraphEdge[] = Array.isArray(data?.edges) ? data.edges : []

      // Person 타입으로 변환
      const personNodes: Node<Person>[] = rawNodes.map((n) => ({
        id: n.id,
        type: 'person',
        data: {
          id: n.id,
          name: n.label,
          label: n.label,
        },
        position: { x: 0, y: 0 }, // dagre가 재계산
      }))

      // 엣지 생성
      const flowEdges: Edge[] = rawEdges.map((e) => {
        const closeness = e.closeness ?? 5
        const relationType: RelationType = 'friend' // 기본값, API에서 타입 정보가 있으면 사용
        const color = RELATION_COLORS[relationType] || '#6b7280'
        const strokeWidth = Math.max(1.5, Math.min(closeness / 2, 5))

        return {
          id: e.id,
          source: e.source,
          target: e.target,
          label: e.label,
          type: 'default',
          markerEnd: {
            type: MarkerType.ArrowClosed,
            color,
            width: 20,
            height: 20
          },
          style: {
            stroke: color,
            strokeWidth,
          },
          labelStyle: {
            fontSize: 11,
            fontWeight: 600,
            fill: color
          },
          labelBgPadding: [6, 4] as [number, number],
          labelBgBorderRadius: 6,
          labelBgStyle: {
            fill: '#ffffff',
            fillOpacity: 0.9,
            stroke: color,
            strokeWidth: 1,
            strokeOpacity: 0.4
          },
          data: { ...e, type: relationType },
          animated: closeness >= 8, // 친밀도 높으면 애니메이션
        }
      })

      // dagre 레이아웃 적용
      const layouted = applyDagreLayout(personNodes, flowEdges, {
        direction: layoutDirection,
        nodeWidth: 180,
        nodeHeight: 100,
        rankSep: 120,
        nodeSep: 100,
      })

      setNodes(layouted)
      setEdges(flowEdges)

      // 레이아웃 후 fitView
      setTimeout(() => {
        reactFlowInstance.fitView({ padding: 0.2, duration: 400 })
      }, 50)

    } catch (e: any) {
      setError(`그래프 로드 실패: ${e?.message || e}`)
    } finally {
      setLoading(false)
    }
  }, [layoutDirection, setNodes, setEdges, reactFlowInstance])

  useEffect(() => {
    fetchGraph()
  }, [fetchGraph])

  const onConnect = useCallback(
    (c: Connection) => setEdges((eds) => addEdge(c, eds)),
    [setEdges]
  )

  const onNodeClick = useCallback(
    (_: any, n: Node<Person>) => {
      setSelectedNode(n)
      setSelectedEdge(null)
    },
    []
  )

  const onEdgeClick = useCallback(
    (_: any, e: Edge) => {
      setSelectedEdge(e)
      setSelectedNode(null)
    },
    []
  )

  const onPaneClick = useCallback(() => {
    setSelectedNode(null)
    setSelectedEdge(null)
  }, [])

  // 레이아웃 토글
  const toggleLayout = useCallback(() => {
    const newDirection = layoutDirection === 'TB' ? 'LR' : 'TB'
    setLayoutDirection(newDirection)

    // 현재 노드에 새 레이아웃 적용
    const layouted = applyDagreLayout(nodes, edges, {
      direction: newDirection,
      nodeWidth: 180,
      nodeHeight: 100,
    })

    setNodes(layouted)
    setTimeout(() => {
      reactFlowInstance.fitView({ padding: 0.2, duration: 400 })
    }, 50)
  }, [layoutDirection, nodes, edges, setNodes, reactFlowInstance])

  // FitView 버튼
  const handleFitView = useCallback(() => {
    reactFlowInstance.fitView({ padding: 0.2, duration: 400 })
  }, [reactFlowInstance])

  return (
    <main className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-4 md:p-6">
      <div className="max-w-[1800px] mx-auto">
        {/* 상단 헤더 */}
        <div className="mb-4 flex items-center gap-3 flex-wrap">
          <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/')}>
            ← 홈으로
          </Button>
          <h1 className="text-2xl font-bold text-gray-900">인물 관계도</h1>

          <div className="ml-auto flex items-center gap-2 flex-wrap">
            <button
              className="px-3 py-2 bg-white hover:bg-gray-50 text-gray-700 rounded-lg border shadow-sm transition-colors text-sm font-medium"
              onClick={toggleLayout}
              title="레이아웃 전환"
            >
              {layoutDirection === 'TB' ? '수평 ↔' : '수직 ↕'}
            </button>

            <button
              className="px-3 py-2 bg-white hover:bg-gray-50 text-gray-700 rounded-lg border shadow-sm transition-colors text-sm font-medium"
              onClick={handleFitView}
              title="전체 보기"
            >
              전체 보기
            </button>

            <button
              className="px-3 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg shadow-sm transition-colors text-sm font-medium"
              onClick={fetchGraph}
            >
              새로고침
            </button>
          </div>
        </div>

        {error && (
          <ErrorMessage message={error} onRetry={fetchGraph} onDismiss={() => setError(null)} />
        )}

        {loading ? (
          <div className="py-20 flex justify-center">
            <LoadingSpinner size="lg" message="관계도를 불러오는 중..." />
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
            {/* 메인 그래프 영역 */}
            <div className="lg:col-span-3" style={{ height: '85vh' }}>
              <div className="bg-white rounded-xl shadow-lg overflow-hidden h-full border border-gray-200">
                <ReactFlow
                  nodes={nodes}
                  edges={edges}
                  onNodesChange={onNodesChange}
                  onEdgesChange={onEdgesChange}
                  onConnect={onConnect}
                  onNodeClick={onNodeClick}
                  onEdgeClick={onEdgeClick}
                  onPaneClick={onPaneClick}
                  nodeTypes={nodeTypes}
                  fitView
                  minZoom={0.1}
                  maxZoom={2}
                  defaultEdgeOptions={{
                    type: 'smoothstep',
                  }}
                >
                  <Background color="#e5e7eb" gap={16} size={1} />
                  <Controls className="!shadow-lg !border !border-gray-200" />
                  <MiniMap
                    nodeColor={(node) => {
                      return '#c7d2fe' // indigo-200
                    }}
                    maskColor="rgba(0, 0, 0, 0.1)"
                    className="!border !border-gray-200 !shadow-lg"
                  />

                  {/* 범례 패널 */}
                  <Panel position="top-right" className="bg-transparent">
                    <Legend />
                  </Panel>
                </ReactFlow>
              </div>
            </div>

            {/* 사이드 패널 */}
            <aside className="lg:col-span-1">
              <div className="bg-white rounded-xl shadow-lg p-5 border border-gray-200 sticky top-4">
                <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                  <span className="w-1 h-5 bg-indigo-600 rounded-full"></span>
                  상세 정보
                </h2>

                {!selectedNode && !selectedEdge && (
                  <p className="text-sm text-gray-500">
                    노드 또는 관계선을 클릭하면 상세 정보를 표시합니다.
                  </p>
                )}

                {selectedNode && (
                  <div className="space-y-3">
                    <div className="pb-3 border-b border-gray-200">
                      <div className="text-2xl font-bold text-gray-900 mb-1">
                        {selectedNode.data.name}
                      </div>
                      {selectedNode.data.title && (
                        <div className="text-sm text-gray-600">{selectedNode.data.title}</div>
                      )}
                      {selectedNode.data.age && (
                        <div className="text-sm text-gray-500">{selectedNode.data.age}세</div>
                      )}
                    </div>

                    {selectedNode.data.description && (
                      <div>
                        <div className="text-xs font-semibold text-gray-700 mb-1">설명</div>
                        <div className="text-sm text-gray-600">{selectedNode.data.description}</div>
                      </div>
                    )}

                    {selectedNode.data.family && (
                      <div>
                        <div className="text-xs font-semibold text-gray-700 mb-1">가족</div>
                        <span className="inline-block px-3 py-1 text-sm font-medium rounded-full bg-indigo-50 text-indigo-700">
                          {selectedNode.data.family}
                        </span>
                      </div>
                    )}

                    <div className="pt-3 border-t border-gray-200">
                      <div className="text-xs text-gray-400">ID: {selectedNode.id}</div>
                    </div>
                  </div>
                )}

                {selectedEdge && (
                  <div className="space-y-3">
                    <div className="pb-3 border-b border-gray-200">
                      <div className="text-lg font-bold text-gray-900 mb-2">관계 정보</div>
                      <div className="text-sm text-gray-600">{selectedEdge.label}</div>
                    </div>

                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-gray-500">출발:</span>
                        <span className="font-medium text-gray-900">{selectedEdge.source}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">도착:</span>
                        <span className="font-medium text-gray-900">{selectedEdge.target}</span>
                      </div>
                      {(selectedEdge.data as any)?.closeness && (
                        <div className="flex justify-between">
                          <span className="text-gray-500">친밀도:</span>
                          <span className="font-medium text-gray-900">
                            {(selectedEdge.data as any).closeness}/10
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="pt-3 border-t border-gray-200">
                      <div className="text-xs text-gray-400">ID: {selectedEdge.id}</div>
                    </div>
                  </div>
                )}
              </div>
            </aside>
          </div>
        )}
      </div>
    </main>
  )
}

export default function GraphPage() {
  return (
    <ReactFlowProvider>
      <GraphPageContent />
    </ReactFlowProvider>
  )
}
