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
import { demoCharacters, demoRelationships, isDemoMode } from '@/data/demoData'
import Link from 'next/link'

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

interface Episode {
  id: number
  title: string
  order: number
}

function GraphPageContent() {
  const [nodes, setNodes, onNodesChange] = useNodesState<Person>([])
  const [edges, setEdges, onEdgesChange] = useEdgesState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [layoutDirection, setLayoutDirection] = useState<LayoutDirection>('TB')
  const [selectedNode, setSelectedNode] = useState<Node<Person> | null>(null)
  const [selectedEdge, setSelectedEdge] = useState<Edge | null>(null)
  const [isDemo, setIsDemo] = useState(false)
  const [episodes, setEpisodes] = useState<Episode[]>([])
  const [selectedEpisodeId, setSelectedEpisodeId] = useState<number | null>(null)
  const reactFlowInstance = useReactFlow()

  // 컴포넌트 마운트 시 데모 모드 확인
  useEffect(() => {
    setIsDemo(isDemoMode())
  }, [])

  // 에피소드 목록 로드
  useEffect(() => {
    if (!isDemo) {
      loadEpisodes()
    }
  }, [isDemo])

  const loadEpisodes = async () => {
    try {
      const response = await apiClient.get('/episodes')
      setEpisodes(response.data)

      // 첫 번째 에피소드 자동 선택
      if (response.data.length > 0 && !selectedEpisodeId) {
        setSelectedEpisodeId(response.data[0].id)
      }
    } catch (err) {
      console.error('Failed to load episodes:', err)
    }
  }

  const fetchGraph = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      let rawNodes: GraphNode[] = []
      let rawEdges: GraphEdge[] = []

      if (isDemo) {
        // 데모 모드: 데모 데이터 변환
        await new Promise(resolve => setTimeout(resolve, 300)) // 실제 API 호출처럼 딜레이 추가

        // 캐릭터를 노드로 변환
        rawNodes = demoCharacters.map((c: any) => ({
          id: String(c.id),
          label: c.name,
        }))

        // 관계를 엣지로 변환
        rawEdges = demoRelationships.map((r: any) => ({
          id: String(r.id),
          source: String(r.fromCharacterId),
          target: String(r.toCharacterId),
          label: r.relationType,
          closeness: r.closeness,
        }))
      } else {
        // 일반 모드: API에서 데이터 가져오기
        if (!selectedEpisodeId) {
          // 에피소드가 선택되지 않은 경우
          setError('에피소드를 선택해주세요')
          return
        }

        const endpoint = `/episode-relationships/episode/${selectedEpisodeId}/graph`
        const res = await apiClient.get(endpoint)
        const data = res.data

        rawNodes = Array.isArray(data?.nodes) ? data.nodes : []
        rawEdges = Array.isArray(data?.edges) ? data.edges : []
      }

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

    } catch (e: any) {
      setError(`그래프 로드 실패: ${e?.message || e}`)
    } finally {
      setLoading(false)
    }
  }, [layoutDirection, setNodes, setEdges, isDemo, selectedEpisodeId])

  useEffect(() => {
    if (!isDemo) {
      fetchGraph()
    }
  }, [layoutDirection, fetchGraph, isDemo])

  // nodes 변경 시 fitView
  useEffect(() => {
    if (nodes.length > 0) {
      setTimeout(() => {
        reactFlowInstance.fitView({ padding: 0.2, duration: 400 })
      }, 50)
    }
  }, [nodes, reactFlowInstance])

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
    <main className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 p-4 md:p-6 transition-colors duration-200">
      <div className="max-w-[1800px] mx-auto">
        {/* 데모 모드 배너 */}
        {isDemo && (
          <div className="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 border-l-4 border-blue-500 dark:border-blue-400 rounded-r-lg">
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-blue-500 dark:text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3 flex-1">
                <p className="text-sm text-blue-700 dark:text-blue-300">
                  <strong className="font-semibold">데모 모드</strong> - 현재 예시 관계 그래프를 보고 계십니다.
                  <Link href="/login" className="underline ml-1 hover:text-blue-800 dark:hover:text-blue-200">
                    로그인
                  </Link>하여 나만의 캐릭터 관계를 만들고 저장하세요.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* 상단 헤더 */}
        <div className="mb-4 space-y-3">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 flex-wrap">
            <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/')}>
              ← 홈으로
            </Button>
            <h1 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">인물 관계도</h1>

            <div className="sm:ml-auto flex items-center gap-2 flex-wrap w-full sm:w-auto">
              <button
                className="flex-1 sm:flex-none px-3 py-2 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg border border-gray-300 dark:border-gray-600 shadow-sm transition-colors text-xs sm:text-sm font-medium"
                onClick={toggleLayout}
                title="레이아웃 전환"
              >
                {layoutDirection === 'TB' ? '수평 ↔' : '수직 ↕'}
              </button>

              <button
                className="flex-1 sm:flex-none px-3 py-2 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg border border-gray-300 dark:border-gray-600 shadow-sm transition-colors text-xs sm:text-sm font-medium"
                onClick={handleFitView}
                title="전체 보기"
              >
                전체 보기
              </button>

              <button
                className="flex-1 sm:flex-none px-3 py-2 bg-indigo-600 hover:bg-indigo-700 dark:bg-indigo-500 dark:hover:bg-indigo-600 text-white rounded-lg shadow-sm transition-colors text-xs sm:text-sm font-medium"
                onClick={fetchGraph}
              >
                새로고침
              </button>
            </div>
          </div>

          {/* 에피소드 선택 */}
          {!isDemo && episodes.length > 0 && (
            <div className="flex items-center gap-3 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-3">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300 whitespace-nowrap">
                에피소드:
              </label>
              <select
                value={selectedEpisodeId || ''}
                onChange={(e) => setSelectedEpisodeId(e.target.value ? Number(e.target.value) : null)}
                className="flex-1 px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              >
                {episodes.length === 0 && <option value="">에피소드를 불러오는 중...</option>}
                {episodes.map((ep) => (
                  <option key={ep.id} value={ep.id}>
                    {ep.order}화: {ep.title}
                  </option>
                ))}
              </select>
            </div>
          )}
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
            <div className="lg:col-span-3 h-[500px] sm:h-[600px] lg:h-[85vh]">
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden h-full border border-gray-200 dark:border-gray-700 transition-colors">
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
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-4 sm:p-5 border border-gray-200 dark:border-gray-700 lg:sticky lg:top-4 transition-colors">
                <h2 className="text-base sm:text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                  <span className="w-1 h-4 sm:h-5 bg-indigo-600 dark:bg-indigo-500 rounded-full"></span>
                  상세 정보
                </h2>

                {!selectedNode && !selectedEdge && (
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    노드 또는 관계선을 클릭하면 상세 정보를 표시합니다.
                  </p>
                )}

                {selectedNode && (
                  <div className="space-y-3">
                    <div className="pb-3 border-b border-gray-200 dark:border-gray-700">
                      <div className="text-2xl font-bold text-gray-900 dark:text-white mb-1">
                        {selectedNode.data.name}
                      </div>
                      {selectedNode.data.title && (
                        <div className="text-sm text-gray-600 dark:text-gray-400">{selectedNode.data.title}</div>
                      )}
                      {selectedNode.data.age && (
                        <div className="text-sm text-gray-500 dark:text-gray-400">{selectedNode.data.age}세</div>
                      )}
                    </div>

                    {selectedNode.data.description && (
                      <div>
                        <div className="text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">설명</div>
                        <div className="text-sm text-gray-600 dark:text-gray-400">{selectedNode.data.description}</div>
                      </div>
                    )}

                    {selectedNode.data.family && (
                      <div>
                        <div className="text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">가족</div>
                        <span className="inline-block px-3 py-1 text-sm font-medium rounded-full bg-indigo-50 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300">
                          {selectedNode.data.family}
                        </span>
                      </div>
                    )}

                    <div className="pt-3 border-t border-gray-200 dark:border-gray-700">
                      <div className="text-xs text-gray-400 dark:text-gray-500">ID: {selectedNode.id}</div>
                    </div>
                  </div>
                )}

                {selectedEdge && (
                  <div className="space-y-3">
                    <div className="pb-3 border-b border-gray-200 dark:border-gray-700">
                      <div className="text-lg font-bold text-gray-900 dark:text-white mb-2">관계 정보</div>
                      <div className="text-sm text-gray-600 dark:text-gray-400">{selectedEdge.label}</div>
                    </div>

                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-gray-500 dark:text-gray-400">출발:</span>
                        <span className="font-medium text-gray-900 dark:text-white">{selectedEdge.source}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500 dark:text-gray-400">도착:</span>
                        <span className="font-medium text-gray-900 dark:text-white">{selectedEdge.target}</span>
                      </div>
                      {(selectedEdge.data as any)?.closeness && (
                        <div className="flex justify-between">
                          <span className="text-gray-500 dark:text-gray-400">친밀도:</span>
                          <span className="font-medium text-gray-900 dark:text-white">
                            {(selectedEdge.data as any).closeness}/10
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="pt-3 border-t border-gray-200 dark:border-gray-700">
                      <div className="text-xs text-gray-400 dark:text-gray-500">ID: {selectedEdge.id}</div>
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
