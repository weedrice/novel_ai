'use client';

import React, { useEffect, useState, useCallback } from 'react';
import ReactFlow, {
  Node,
  Edge,
  Controls,
  Background,
  MiniMap,
  useNodesState,
  useEdgesState,
  addEdge,
  Connection,
  MarkerType,
} from 'reactflow';
import dagre from 'dagre';
import 'reactflow/dist/style.css';
import { getAllRelationships, syncProjectData, getCentralCharacters } from '@/lib/graph';
import { getCurrentProject } from '@/lib/project';

// Dagre를 사용한 자동 레이아웃
const getLayoutedElements = (nodes: Node[], edges: Edge[], direction = 'TB') => {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  dagreGraph.setGraph({ rankdir: direction });

  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: 150, height: 50 });
  });

  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  const layoutedNodes = nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    return {
      ...node,
      position: {
        x: nodeWithPosition.x - 75,
        y: nodeWithPosition.y - 25,
      },
    };
  });

  return { nodes: layoutedNodes, edges };
};

// 관계 유형별 색상 매핑
const relationColors: Record<string, string> = {
  friend: '#10b981', // green
  rival: '#f59e0b', // amber
  family: '#3b82f6', // blue
  lover: '#ec4899', // pink
  enemy: '#ef4444', // red
};

export default function GraphViewPage() {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [selectedEpisode, setSelectedEpisode] = useState<number | null>(null);
  const [centralChars, setCentralChars] = useState<any[]>([]);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  // 그래프 데이터 로드
  const loadGraphData = async () => {
    try {
      setLoading(true);
      const data = await getAllRelationships();

      // 캐릭터 노드 추출 (중복 제거)
      const characterMap = new Map();

      // relationships 데이터 파싱 (실제 구조에 맞게 조정 필요)
      if (data.relationships && Array.isArray(data.relationships)) {
        data.relationships.forEach((rel: any) => {
          // Neo4j 결과 구조에 따라 파싱
          // 임시로 간단한 구조 가정
          if (rel.c1) {
            characterMap.set(rel.c1.characterId, rel.c1);
          }
          if (rel.c2) {
            characterMap.set(rel.c2.characterId, rel.c2);
          }
        });
      }

      // 노드 생성
      const graphNodes: Node[] = Array.from(characterMap.values()).map((char: any) => ({
        id: char.characterId,
        data: {
          label: char.name,
          character: char
        },
        position: { x: 0, y: 0 },
        style: {
          background: '#fff',
          border: '2px solid #3b82f6',
          borderRadius: '8px',
          padding: '10px',
          fontSize: '14px',
          fontWeight: 'bold',
        },
      }));

      // 엣지 생성
      const graphEdges: Edge[] = [];
      if (data.relationships && Array.isArray(data.relationships)) {
        data.relationships.forEach((rel: any, idx: number) => {
          if (rel.r && rel.c1 && rel.c2) {
            const edgeColor = relationColors[rel.r.relationType] || '#6b7280';
            graphEdges.push({
              id: `e${idx}`,
              source: rel.c1.characterId,
              target: rel.c2.characterId,
              label: `${rel.r.relationType} (${rel.r.closeness?.toFixed(1) || '?'})`,
              type: 'smoothstep',
              animated: true,
              style: { stroke: edgeColor, strokeWidth: 2 },
              markerEnd: {
                type: MarkerType.ArrowClosed,
                color: edgeColor,
              },
            });
          }
        });
      }

      // 레이아웃 적용
      const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
        graphNodes,
        graphEdges,
        'TB'
      );

      setNodes(layoutedNodes);
      setEdges(layoutedEdges);
    } catch (error) {
      console.error('Failed to load graph data:', error);
      alert('그래프 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 프로젝트 데이터 동기화
  const handleSync = async () => {
    try {
      setSyncing(true);
      const project = getCurrentProject();
      if (!project) {
        alert('프로젝트를 선택해주세요.');
        return;
      }

      await syncProjectData(project.id);
      alert('데이터 동기화 완료!');
      await loadGraphData();
    } catch (error) {
      console.error('Sync failed:', error);
      alert('동기화에 실패했습니다.');
    } finally {
      setSyncing(false);
    }
  };

  // 중심 인물 찾기
  const findCentralCharacters = async () => {
    try {
      const data = await getCentralCharacters(5);
      setCentralChars(data.centralCharacters || []);
    } catch (error) {
      console.error('Failed to find central characters:', error);
    }
  };

  useEffect(() => {
    loadGraphData();
    findCentralCharacters();
  }, []);

  return (
    <div className="h-screen flex flex-col">
      {/* 헤더 */}
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              캐릭터 관계 그래프
            </h1>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
              Neo4j GraphDB 기반 관계 시각화
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={handleSync}
              disabled={syncing}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
            >
              {syncing ? '동기화 중...' : '데이터 동기화'}
            </button>
            <button
              onClick={loadGraphData}
              disabled={loading}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400"
            >
              {loading ? '로딩 중...' : '새로고침'}
            </button>
          </div>
        </div>

        {/* 범례 */}
        <div className="mt-4 flex gap-4 text-sm">
          {Object.entries(relationColors).map(([type, color]) => (
            <div key={type} className="flex items-center gap-2">
              <div
                className="w-4 h-4 rounded"
                style={{ backgroundColor: color }}
              />
              <span className="text-gray-700 dark:text-gray-300 capitalize">
                {type}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* 그래프 영역 */}
      <div className="flex-1 relative">
        {loading ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-gray-600 dark:text-gray-400">로딩 중...</div>
          </div>
        ) : (
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            fitView
          >
            <Background />
            <Controls />
            <MiniMap />
          </ReactFlow>
        )}
      </div>

      {/* 사이드바: 중심 인물 */}
      {centralChars.length > 0 && (
        <div className="absolute right-4 top-32 bg-white dark:bg-gray-800 p-4 rounded-lg shadow-lg w-64 border border-gray-200 dark:border-gray-700">
          <h3 className="font-bold text-gray-900 dark:text-white mb-2">
            중심 인물 Top 5
          </h3>
          <ul className="space-y-2">
            {centralChars.map((char, idx) => (
              <li
                key={idx}
                className="text-sm text-gray-700 dark:text-gray-300 flex justify-between"
              >
                <span>{idx + 1}. {char.result?.c?.name || '알 수 없음'}</span>
                <span className="text-blue-600 dark:text-blue-400 font-mono">
                  {char.result?.relationshipCount || 0}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
