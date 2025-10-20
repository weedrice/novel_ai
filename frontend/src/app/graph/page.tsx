'use client'

import React, { useEffect, useMemo, useState, useCallback } from 'react'
import ReactFlow, {
  Background,
  Controls,
  Edge,
  MarkerType,
  Node,
  addEdge,
  useEdgesState,
  useNodesState,
  Connection,
} from 'reactflow'
import 'reactflow/dist/style.css'
import ErrorMessage from '@/components/ErrorMessage'
import LoadingSpinner from '@/components/LoadingSpinner'
import Button from '@/components/Button'

const API = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080'

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

export default function GraphPage() {
  const [nodes, setNodes, onNodesChange] = useNodesState<Node[]>([])
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState<string>('all')
  const [relationTypes, setRelationTypes] = useState<string[]>([])
  const [selectedNode, setSelectedNode] = useState<Node | null>(null)
  const [selectedEdge, setSelectedEdge] = useState<Edge | null>(null)
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [showAdd, setShowAdd] = useState(false)
  const [characters, setCharacters] = useState<{ id: number; name: string }[]>([])
  const [newRel, setNewRel] = useState({
    fromCharacterId: '',
    toCharacterId: '',
    relationType: 'friend',
    closeness: 5,
    description: '',
  })

  const fetchGraph = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`${API}/relationships/graph`, { cache: 'no-store' })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()

      const rawNodes: GraphNode[] = Array.isArray(data?.nodes) ? data.nodes : []
      const rawEdges: GraphEdge[] = Array.isArray(data?.edges) ? data.edges : []

      const flowNodes: Node[] = rawNodes.map((n, i, arr) => ({
        id: n.id,
        data: { label: n.label },
        position: {
          x: Math.cos((i / Math.max(arr.length, 1)) * 2 * Math.PI) * 300 + 500,
          y: Math.sin((i / Math.max(arr.length, 1)) * 2 * Math.PI) * 300 + 400,
        },
        style: {
          background: 'white',
          border: '2px solid #4f46e5',
          color: '#111827',
          borderRadius: 12,
          padding: '10px 14px',
          fontWeight: 600,
        },
      }))

      const flowEdges: Edge[] = rawEdges.map((e) => {
        const closeness = e.closeness ?? 5
        const color = closeness >= 8 ? '#10b981' : closeness >= 6 ? '#3b82f6' : '#6b7280'
        return {
          id: e.id,
          source: e.source,
          target: e.target,
          label: [e.label, typeof e.closeness === 'number' ? `(${e.closeness.toFixed(1)})` : '']
            .filter(Boolean)
            .join(' '),
          markerEnd: { type: MarkerType.ArrowClosed, color, width: 20, height: 20 },
          style: { stroke: color, strokeWidth: Math.max(2, closeness / 2) },
          labelStyle: { fontSize: 12, fontWeight: 600, fill: color },
          labelBgPadding: [6, 3],
          labelBgBorderRadius: 6,
          labelBgStyle: { fill: '#ffffff', fillOpacity: 0.85, stroke: color, strokeOpacity: 0.3 },
          data: e,
        }
      })

      setNodes(flowNodes)
      setEdges(flowEdges)
      // 관계 유형 목록 구성
      const types = Array.from(
        new Set<string>(rawEdges.map((e) => String(e.label || '')).filter(Boolean))
      )
      setRelationTypes(types)
    } catch (e: any) {
      setError(`그래프 로드 실패: ${e?.message || e}`)
    } finally {
      setLoading(false)
    }
  }, [setNodes, setEdges])

  useEffect(() => {
    fetchGraph()
    // load characters for add-relationship form
    ;(async () => {
      try {
        const res = await fetch(`${API}/characters`, { cache: 'no-store' })
        const data = await res.json()
        setCharacters(
          (Array.isArray(data) ? data : []).map((c: any) => ({ id: c.id, name: c.name }))
        )
      } catch {}
    })()
  }, [fetchGraph])

  const onConnect = useCallback((c: Connection) => setEdges((eds) => addEdge(c, eds)), [setEdges])

  const onNodeClick = useCallback((_: any, n: Node) => {
    setSelectedNode(n)
    setSelectedEdge(null)
  }, [])

  const onEdgeClick = useCallback((_: any, e: Edge) => {
    setSelectedEdge(e)
    setSelectedNode(null)
  }, [])

  const onPaneClick = useCallback(() => {
    setSelectedNode(null)
    setSelectedEdge(null)
  }, [])

  const filtered = useMemo(() => {
    // 이름 필터
    const q = filter.trim().toLowerCase()
    let keptNodes = nodes
    let keptEdges = edges
    if (q) {
      const keepNode = new Set(
        nodes.filter((n) => String(n.data?.label || '').toLowerCase().includes(q)).map((n) => n.id),
      )
      keptNodes = nodes.filter((n) => keepNode.has(n.id))
      keptEdges = edges.filter((e) => keepNode.has(e.source) || keepNode.has(e.target))
    }
    // 유형 필터
    if (typeFilter !== 'all') {
      keptEdges = keptEdges.filter((e) => String(e.data?.label || e.label || '') === typeFilter)
      const connected = new Set<string>()
      keptEdges.forEach((e) => {
        connected.add(e.source)
        connected.add(e.target)
      })
      keptNodes = keptNodes.filter((n) => connected.has(n.id))
    }
    return { nodes: keptNodes, edges: keptEdges }
  }, [filter, typeFilter, nodes, edges])

  return (
    <main className="min-h-screen bg-gray-50 p-6 md:p-10">
      <div className="max-w-6xl mx-auto">
        <div className="mb-4 flex items-center gap-3">
          <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/')}>홈으로</Button>
          <a href="/" className="px-3 py-2 border rounded text-sm">홈으로</a>
          <h1 className="text-2xl font-bold text-gray-900">관계 그래프</h1>
          <button
            className="ml-auto px-3 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md border"
            onClick={fetchGraph}
          >
            새로고침
          </button>
          <button
            className="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-md border"
            onClick={() => setShowAdd(true)}
          >
            관계 추가
          </button>
          {selectedEdge && (
            <button
              className="px-3 py-2 bg-red-600 hover:bg-red-700 text-white rounded-md border"
              onClick={async () => {
                try {
                  const id = String(selectedEdge.id)
                  await fetch(`${API}/relationships/${id}`, { method: 'DELETE' })
                  setSelectedEdge(null)
                  await fetchGraph()
                } catch (e) {
                  setError('삭제 실패')
                }
              }}
            >
              선택 엣지 삭제
            </button>
          )}
          <input
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md"
            placeholder="이름 검색"
          />
          <select
            className="px-3 py-2 border border-gray-300 rounded-md"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <option value="all">모든 관계</option>
            {relationTypes.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </div>

        {error && (
          <ErrorMessage message={error} onRetry={fetchGraph} onDismiss={() => setError(null)} />
        )}

        {loading ? (
          <div className="py-20 flex justify-center">
            <LoadingSpinner size="lg" message="그래프를 불러오는 중..." />
          </div>
        ) : (
          <>
          <div className="grid grid-cols-1 lg:grid-cols-5 gap-4">
            <div className="lg:col-span-4 lg:order-1" style={{ height: '80vh', background: 'white', borderRadius: 12, overflow: 'hidden' }}>
              <ReactFlow
                nodes={filtered.nodes}
                edges={filtered.edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                onNodeClick={onNodeClick}
                onEdgeClick={onEdgeClick}
                onPaneClick={onPaneClick}
                fitView
              >
                <Background />
                <Controls />
              </ReactFlow>
            </div>
            <aside className={`lg:col-span-1 lg:order-2 bg-white rounded-lg shadow p-4 h-[80vh] overflow-auto ${sidebarOpen ? '' : 'hidden lg:block lg:col-span-1'}`}> 
              <div className="flex items-center justify-between mb-3">
                <h2 className="text-lg font-semibold">정보 패널</h2>
                <button className="text-sm px-2 py-1 rounded bg-gray-700 hover:bg-gray-800 text-white" onClick={() => setSidebarOpen(!sidebarOpen)}>
                  {sidebarOpen ? '접기' : '펴기'}
                </button>
              </div>
              {!selectedNode && !selectedEdge && (
                <p className="text-sm text-gray-500">노드 또는 엣지를 클릭하면 상세 정보를 표시합니다.</p>
              )}
              {selectedNode && (
                <div>
                  <h2 className="text-lg font-semibold mb-2">노드 정보</h2>
                  <div className="text-sm text-gray-700">
                    <div><strong>ID:</strong> {selectedNode.id}</div>
                    <div><strong>Label:</strong> {String((selectedNode.data as any)?.label || '')}</div>
                  </div>
                </div>
              )}
              {selectedEdge && (
                <div>
                  <h2 className="text-lg font-semibold mb-2">엣지 정보</h2>
                  <div className="text-sm text-gray-700 space-y-1">
                    <div><strong>ID:</strong> {selectedEdge.id}</div>
                    <div><strong>Source:</strong> {selectedEdge.source}</div>
                    <div><strong>Target:</strong> {selectedEdge.target}</div>
                    <div><strong>Label:</strong> {String(selectedEdge.label || '')}</div>
                    <div><strong>Closeness:</strong> {String((selectedEdge.data as any)?.closeness ?? '')}</div>
                  </div>
                </div>
              )}
            </aside>
          </div>
          {showAdd && (
            <div
              className="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
              onClick={() => setShowAdd(false)}
            >
              <div
                className="bg-white rounded-lg p-5 w-[520px] max-w-[92vw]"
                onClick={(e) => e.stopPropagation()}
              >
                <h2 className="text-lg font-semibold mb-4">관계 추가</h2>
                <div className="space-y-3">
                  <div>
                    <label className="block text-sm mb-1">출발 캐릭터</label>
                    <select
                      className="w-full border rounded px-3 py-2"
                      value={newRel.fromCharacterId}
                      onChange={(e) => setNewRel({ ...newRel, fromCharacterId: e.target.value })}
                    >
                      <option value="">선택</option>
                      {characters.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm mb-1">도착 캐릭터</label>
                    <select
                      className="w-full border rounded px-3 py-2"
                      value={newRel.toCharacterId}
                      onChange={(e) => setNewRel({ ...newRel, toCharacterId: e.target.value })}
                    >
                      <option value="">선택</option>
                      {characters.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm mb-1">관계 유형</label>
                    <input
                      className="w-full border rounded px-3 py-2"
                      value={newRel.relationType}
                      onChange={(e) => setNewRel({ ...newRel, relationType: e.target.value })}
                      placeholder="e.g. friend, rival, family"
                    />
                  </div>
                  <div>
                    <label className="block text-sm mb-1">친밀도: {newRel.closeness}</label>
                    <input
                      type="range"
                      min={0}
                      max={10}
                      step={0.1}
                      className="w-full"
                      value={newRel.closeness}
                      onChange={(e) => setNewRel({ ...newRel, closeness: Number(e.target.value) })}
                    />
                  </div>
                  <div>
                    <label className="block text-sm mb-1">설명</label>
                    <textarea
                      className="w-full border rounded px-3 py-2"
                      rows={3}
                      value={newRel.description}
                      onChange={(e) => setNewRel({ ...newRel, description: e.target.value })}
                    />
                  </div>
                </div>
                <div className="mt-4 flex justify-end gap-2">
                  <button className="px-3 py-2 rounded border" onClick={() => setShowAdd(false)}>취소</button>
                  <button
                    className="px-3 py-2 rounded bg-indigo-600 text-white hover:bg-indigo-700"
                    onClick={async () => {
                      if (!newRel.fromCharacterId || !newRel.toCharacterId) return
                      try {
                        const body = {
                          fromCharacter: { id: Number(newRel.fromCharacterId) },
                          toCharacter: { id: Number(newRel.toCharacterId) },
                          relationType: newRel.relationType,
                          closeness: newRel.closeness,
                          description: newRel.description,
                        }
                        await fetch(`${API}/relationships`, {
                          method: 'POST',
                          headers: { 'Content-Type': 'application/json' },
                          body: JSON.stringify(body),
                        })
                        setShowAdd(false)
                        setNewRel({ fromCharacterId: '', toCharacterId: '', relationType: 'friend', closeness: 5, description: '' })
                        await fetchGraph()
                      } catch (e) {
                        setError('관계 추가 실패')
                      }
                    }}
                  >
                    생성
                  </button>
                </div>
              </div>
            </div>
          )}
          </>
        )}
      </div>
    </main>
  )
}
