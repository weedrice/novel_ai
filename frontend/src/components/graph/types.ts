/**
 * 인물 관계도 타입 정의
 */

export interface Person {
  id: string
  name: string
  title?: string
  age?: number
  family?: string // 가족 그룹 (예: '이가', '현가')
  description?: string
  imageUrl?: string
}

export interface Relation {
  id: string
  from: string // person id
  to: string   // person id
  type: RelationType
  label: string
  closeness?: number // 0-10
  bidirectional?: boolean
}

export type RelationType =
  | 'family'      // 가족
  | 'friend'      // 친구
  | 'romance'     // 연인
  | 'rival'       // 라이벌
  | 'colleague'   // 동료
  | 'mentor'      // 멘토/제자
  | 'enemy'       // 적

export type EdgeKind = RelationType

export interface GraphData {
  people: Person[]
  relations: Relation[]
}

export const RELATION_COLORS: Record<RelationType, string> = {
  family: '#ef4444',      // 빨강 - 가족
  friend: '#3b82f6',      // 파랑 - 친구
  romance: '#ec4899',     // 핑크 - 연인
  rival: '#f59e0b',       // 주황 - 라이벌
  colleague: '#8b5cf6',   // 보라 - 동료
  mentor: '#10b981',      // 초록 - 멘토
  enemy: '#1f2937',       // 검정 - 적
}

export const RELATION_LABELS: Record<RelationType, string> = {
  family: '가족',
  friend: '친구',
  romance: '연인',
  rival: '라이벌',
  colleague: '동료',
  mentor: '멘토/제자',
  enemy: '적',
}

export const FAMILY_COLORS: Record<string, string> = {
  '이가': '#dbeafe',      // 파랑 계열
  '현가': '#fce7f3',      // 핑크 계열
  '박가': '#dcfce7',      // 초록 계열
  '기타': '#f3f4f6',      // 회색
}
