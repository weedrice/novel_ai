/**
 * 공통 타입 정의
 * 프로젝트 전체에서 사용되는 타입들을 중앙화
 */

// ============================================
// 도메인 엔티티
// ============================================

export interface Character {
  id: number;
  characterId: string;
  name: string;
  description?: string;
  personality?: string;
  speakingStyle?: string;
  vocabulary?: string;
  toneKeywords?: string;
  examples?: string;
  prohibitedWords?: string;
  sentencePatterns?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Episode {
  id: number;
  title: string;
  description?: string;
  episodeOrder: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Scene {
  id: number;
  sceneNumber: number;
  location: string;
  mood?: string;
  description?: string;
  participants?: string;
  episode: Episode;
  dialogues?: Dialogue[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Dialogue {
  id: number;
  dialogueOrder: number;
  text: string;
  character: Character;
  scene: Scene;
  createdAt?: string;
  updatedAt?: string;
}

export interface Relationship {
  id: number;
  relationType: RelationType;
  closeness: number;
  description?: string;
  fromCharacter: Character;
  toCharacter: Character;
  createdAt?: string;
  updatedAt?: string;
}

// ============================================
// 그래프 관련 타입
// ============================================

export interface Person {
  id: string;
  name: string;
  title?: string;
  age?: number;
  family?: string;
  description?: string;
}

export interface Relation {
  id: string;
  from: string;
  to: string;
  type: RelationType;
  closeness: number;
  description?: string;
}

export type RelationType =
  | 'family'
  | 'friend'
  | 'romance'
  | 'rival'
  | 'colleague'
  | 'mentor'
  | 'enemy';

export type LayoutDirection = 'TB' | 'LR' | 'BT' | 'RL';

export interface GraphData {
  persons: Person[];
  relations: Relation[];
}

// ============================================
// API 요청/응답 타입
// ============================================

export namespace API {
  // Dialogue Suggestion
  export interface SuggestRequest {
    speakerId: string;
    targetIds: string[];
    intent: string;
    honorific: string;
    maxLen: number;
    nCandidates: number;
    provider?: LLMProvider;
  }

  export interface SuggestResponse {
    candidates: DialogueCandidate[];
  }

  export interface DialogueCandidate {
    text: string;
    score: number;
  }

  // Scenario Generation
  export interface GenerateScenarioRequest {
    sceneId: number;
    nDialogues: number;
    provider?: LLMProvider;
  }

  export interface GenerateScenarioResponse {
    dialogues: GeneratedDialogue[];
  }

  export interface GeneratedDialogue {
    speaker: string;
    characterId: string;
    text: string;
    order: number;
  }

  // Scenario Version
  export interface ScenarioVersion {
    id: number;
    sceneId: number;
    versionName: string;
    dialogues: Dialogue[];
    createdAt: string;
  }

  // Health Check
  export interface HealthCheckResponse {
    status: 'ok' | 'error';
    timestamp: string;
    details?: Record<string, any>;
  }

  // Speaking Profile Update
  export interface UpdateSpeakingProfileRequest {
    speakingStyle?: string;
    vocabulary?: string;
    toneKeywords?: string;
    examples?: string;
    prohibitedWords?: string;
    sentencePatterns?: string;
  }

  // Dialogue CRUD
  export interface CreateDialogueRequest {
    sceneId: number;
    characterId: string;
    text: string;
    dialogueOrder: number;
  }

  export interface UpdateDialogueRequest {
    text?: string;
    dialogueOrder?: number;
  }
}

// ============================================
// UI 상태 타입
// ============================================

export interface LoadingState {
  isLoading: boolean;
  message?: string;
}

export interface ErrorState {
  hasError: boolean;
  message?: string;
  code?: string;
}

export interface SuccessState {
  isSuccess: boolean;
  message?: string;
}

// ============================================
// 공통 Enum/Constants
// ============================================

export type LLMProvider = 'openai' | 'claude' | 'gemini';

export type ButtonVariant = 'primary' | 'secondary' | 'success' | 'warning' | 'danger';

export type ButtonSize = 'sm' | 'md' | 'lg';

export type SpinnerSize = 'sm' | 'md' | 'lg';

// ============================================
// 유틸리티 타입
// ============================================

export type Nullable<T> = T | null;

export type Optional<T> = T | undefined;

export type ID = string | number;

export type Timestamp = string; // ISO 8601 format

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

// Pagination
export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
  hasPrev: boolean;
}

// Sort order
export type SortOrder = 'asc' | 'desc';

export interface SortConfig {
  field: string;
  order: SortOrder;
}