/**
 * 그래프 레이아웃 유틸리티
 * dagre를 사용한 자동 레이아웃
 */

import { Node, Edge } from 'reactflow'
import dagre from 'dagre'

export type LayoutDirection = 'TB' | 'LR' | 'BT' | 'RL'

export interface LayoutOptions {
  direction?: LayoutDirection
  nodeWidth?: number
  nodeHeight?: number
  rankSep?: number // 계층 간 간격
  nodeSep?: number // 노드 간 간격
}

const defaultOptions: Required<LayoutOptions> = {
  direction: 'TB',
  nodeWidth: 180,
  nodeHeight: 80,
  rankSep: 100,
  nodeSep: 80,
}

/**
 * dagre를 사용하여 노드 위치 자동 계산
 */
export function applyDagreLayout<T = any>(
  nodes: Node<T>[],
  edges: Edge[],
  options: LayoutOptions = {}
): Node<T>[] {
  const opts = { ...defaultOptions, ...options }

  const dagreGraph = new dagre.graphlib.Graph()
  dagreGraph.setDefaultEdgeLabel(() => ({}))
  dagreGraph.setGraph({
    rankdir: opts.direction,
    ranksep: opts.rankSep,
    nodesep: opts.nodeSep,
  })

  // 노드 추가
  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, {
      width: opts.nodeWidth,
      height: opts.nodeHeight
    })
  })

  // 엣지 추가
  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target)
  })

  // 레이아웃 계산
  dagre.layout(dagreGraph)

  // 계산된 위치 적용
  return nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id)

    return {
      ...node,
      position: {
        x: nodeWithPosition.x - opts.nodeWidth / 2,
        y: nodeWithPosition.y - opts.nodeHeight / 2,
      },
    }
  })
}

/**
 * 원형 레이아웃
 */
export function applyCircularLayout<T = any>(
  nodes: Node<T>[],
  centerX: number = 500,
  centerY: number = 400,
  radius: number = 300
): Node<T>[] {
  return nodes.map((node, i, arr) => ({
    ...node,
    position: {
      x: Math.cos((i / Math.max(arr.length, 1)) * 2 * Math.PI) * radius + centerX,
      y: Math.sin((i / Math.max(arr.length, 1)) * 2 * Math.PI) * radius + centerY,
    },
  }))
}

/**
 * 가족별 그룹 레이아웃 (수평 배치)
 */
export function applyFamilyGroupLayout<T extends { family?: string } = any>(
  nodes: Node<T>[],
  groupPadding: number = 200
): Node<T>[] {
  // 가족별로 그룹화
  const families = new Map<string, Node<T>[]>()

  nodes.forEach((node) => {
    const family = node.data?.family || '기타'
    if (!families.has(family)) {
      families.set(family, [])
    }
    families.get(family)!.push(node)
  })

  let currentX = 100
  const positioned: Node<T>[] = []

  // 각 가족 그룹을 수평으로 배치
  families.forEach((familyNodes, familyName) => {
    familyNodes.forEach((node, i) => {
      positioned.push({
        ...node,
        position: {
          x: currentX,
          y: 100 + i * 120, // 세로로 정렬
        },
      })
    })

    currentX += groupPadding + 180 // 다음 그룹 위치
  })

  return positioned
}
