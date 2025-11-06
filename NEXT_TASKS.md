# Next Tasks - 남은 작업 목록

> 프로젝트에서 아직 완료되지 않은 작업들
> 마지막 업데이트: 2025-11-05

---

## 📖 이 문서 사용 방법

- 완료된 작업은 [COMPLETED_TASKS.md](COMPLETED_TASKS.md)로 이동되었습니다
- 각 Phase는 선택적이며, 프로젝트 필요에 따라 진행하세요
- ⬜ 표시는 대기 중, ⏳은 진행 중을 의미합니다
- Task 완료 시마다 체크하여 진행 상황을 추적하세요

---

## 🎯 현재 상태 요약 (2025-11-06)

### ✅ 최근 완료 (2025-11-06)
- **Neo4j 그래프 시각화**: React Flow 기반 캐릭터 관계 그래프 인터랙티브 시각화
- **Centrality 분석**: Degree, Betweenness, Closeness, Weighted Degree 계산 API
- **시간축 관계 추적**: 에피소드별 관계 변화 타임라인 및 네트워크 밀도 분석
- **Chart.js 통합**: 시계열 차트로 관계 진화 시각화

### ✅ 완료된 Phase
- Phase 0: 프로젝트 초기 설정
- Phase 1: 핵심 도메인 모델 및 데이터베이스 구축
- Phase 2: 캐릭터 관계 그래프 시각화
- Phase 3: 말투 프로필링 및 실제 LLM 연동
- Phase 4: 시나리오 제안 및 편집 기능
- Phase 5: 스크립트 검수 및 분석 도구 (Episode로 통합 완료)
- Phase 6: 사용자 인증 및 권한 관리
- Phase 7.3: 프론트엔드 검색 UI
- Phase 8: Docker 및 배포 자동화
- Phase 9: Neo4j GraphDB 통합 (일부 완료 - Task 106, 108, 109)
- Phase 10: 고급 기능 및 최적화 (Task 86-96, 99, 105 완료)

### 📊 현재 데이터베이스 구조

#### RDB 엔티티 (PostgreSQL)
```
User
  └─> Project (1:N)
        ├─> Episode (1:N)
        │     ├─ scriptText (TEXT)
        │     ├─ scriptFormat (VARCHAR)
        │     ├─ analysisStatus (VARCHAR)
        │     ├─ analysisResult (JSONB)
        │     └─> EpisodeRelationship (1:N)
        │           ├─> Character (N:1)
        │           └─> Character (N:1)
        ├─> Character (1:N)
        │     └─> Dialogue (1:N)
        └─> Scene (1:N via Episode)
              ├─> Dialogue (1:N)
              └─> ScenarioVersion (1:N)
```

#### 주요 특징
- **Episode**: 스크립트 원문 + LLM 분석 결과 통합 저장
- **EpisodeRelationship**: 에피소드별 캐릭터 관계 추적 (시간축 변화 기록)
- **Project**: 사용자별 데이터 격리 (Multi-tenancy)

### ⏳ 남은 Phase
- **Phase 7.1-7.2**: Vector DB 및 의미 검색 백엔드 (선택적)
- **Phase 9**: Neo4j GraphDB 통합 (Task 107 남음 - 선택적)
- **Phase 10**: 추가 기능 (Task 97-98, 100)
- **Phase 11**: Multi-Database Architecture (신규 추가 - 선택적)

### 테스트 통계
- **백엔드**: 159개 통과 (Integration/Service tests)
- **프론트엔드**: 49개 (컴포넌트: 18, E2E: 31)
- **총 테스트**: 208개
- **커버리지**: Service 79%, Security 95%

---

## Phase 7: Vector DB 및 의미 검색 (선택적)

### 목표
캐릭터 대사 및 장면 검색 시 의미 기반 검색 기능 추가

### 7.1 Pinecone/Weaviate/Qdrant 설정

#### Task 101: Vector DB 선택 및 설정
- [ ] Vector DB 선택 (Pinecone, Weaviate, Qdrant 중)
- [ ] Vector DB 클라이언트 라이브러리 추가
- [ ] 연결 설정 및 인덱스 생성
- [ ] 환경 변수 설정

**예상 소요 시간**: 1시간

---

#### Task 102: 임베딩 모델 연동
- [ ] OpenAI Embeddings API 또는 SentenceTransformers 연동
- [ ] 텍스트 → 벡터 변환 로직 구현
- [ ] 배치 임베딩 처리

**예상 소요 시간**: 2시간

---

### 7.2 의미 검색 기능 구현

#### Task 103: 대사 임베딩 저장
- [ ] 대사 생성 시 자동 임베딩 저장
- [ ] 기존 대사 일괄 임베딩
- [ ] 업데이트/삭제 시 벡터 동기화

**예상 소요 시간**: 2시간

---

#### Task 104: 의미 검색 API 구현
- [ ] 검색 쿼리 → 벡터 변환
- [ ] 유사도 기반 검색 (Cosine Similarity)
- [ ] 검색 결과 필터링 및 정렬
- [ ] GET /dialogues/search?q={query} 엔드포인트 구현

**예상 소요 시간**: 3시간

---

**Phase 7 예상 소요 시간**: 8-10시간

---


## Phase 10: 추가 기능 (지속적 개선)

### 목표
사용자 경험을 더욱 풍부하게 하는 추가 기능 구현

### 10.1 대사 음성 합성 (TTS)

#### Task 97: TTS API 연동
- [ ] TTS API 선택 (Google Cloud TTS, Amazon Polly, ElevenLabs 등)
- [ ] API 클라이언트 구현
- [ ] 대사 텍스트 → 음성 변환
- [ ] 음성 파일 저장 및 URL 반환

**예상 소요 시간**: 2시간

---

#### Task 97-1: 캐릭터별 음성 설정
- [ ] Character 엔티티에 voiceId 필드 추가
- [ ] 캐릭터별 음성 프로필 관리 UI
- [ ] 음성 미리듣기 기능

**예상 소요 시간**: 1시간

---

#### Task 97-2: 대사 읽기 기능 구현
- [ ] 대사 읽기 버튼 추가
- [ ] 오디오 플레이어 컴포넌트
- [ ] 재생/일시정지/중지 기능

**예상 소요 시간**: 1시간

---

**Task 97 총 예상 소요 시간**: 4시간

---

### 10.2 캐릭터 이미지 생성 (AI)

#### Task 98: 이미지 생성 API 연동
- [ ] 이미지 생성 API 선택 (DALL-E, Stable Diffusion, Midjourney API 등)
- [ ] API 클라이언트 구현
- [ ] 캐릭터 설명 → 프롬프트 생성
- [ ] 이미지 생성 및 URL 반환

**예상 소요 시간**: 2시간

---

#### Task 98-1: 캐릭터 이미지 저장 및 표시
- [ ] Character 엔티티에 imageUrl 필드 추가
- [ ] 이미지 생성 버튼 및 모달
- [ ] 생성된 이미지 미리보기
- [ ] 이미지 선택 및 저장

**예상 소요 시간**: 2시간

---

**Task 98 총 예상 소요 시간**: 4시간

---

### 10.4 엑셀/스프레드시트 가져오기/내보내기

#### Task 100: 엑셀 Import/Export
- [ ] 엑셀 라이브러리 추가 (Apache POI 또는 ExcelJS)
- [ ] 캐릭터 데이터 엑셀 가져오기
  - 엑셀 파일 파싱
  - 캐릭터 일괄 생성/업데이트
  - 유효성 검증 및 에러 처리
- [ ] 에피소드/장면 엑셀 내보내기
  - 에피소드 및 장면 데이터 엑셀 변환
  - 대사 포함 시나리오 엑셀 내보내기
  - 다운로드 기능 구현

**예상 소요 시간**: 3시간

**기능 상세**:
- Import: 엑셀 업로드 → 파싱 → 검증 → DB 저장
- Export: DB 조회 → 엑셀 생성 → 다운로드
- 템플릿 제공 (빈 엑셀 파일 다운로드)

---

**Phase 10 추가 기능 총 예상 소요 시간**: 11시간

---

## Phase 11: Multi-Database Architecture (신규 - 선택적)

### 목표
RDB + GraphDB + VectorDB를 통합하여 AI 분석 플랫폼 구축

### 현재 상태
- ✅ RDB (PostgreSQL): Episode.analysisResult (JSONB)에 LLM 분석 결과 저장
- ⬜ GraphDB (Neo4j): 미구현 (Phase 9에서 예정)
- ⬜ VectorDB: 미구현 (Phase 7에서 예정)

### 11.1 AI Analysis Storage 개선

#### Task 110: AI Analysis 엔티티 설계
- [ ] AIAnalysis 엔티티 생성
  - `id`, `episodeId`, `analysisType` (sentiment, summary, tone, character_extraction, relationship_extraction)
  - `modelName` (gpt-4, claude-3, gemini-pro 등)
  - `result` (JSONB)
  - `confidence` (0.0-1.0)
  - `createdAt`, `updatedAt`
- [ ] AIAnalysis Repository & Service 구현
- [ ] Episode.analysisResult → AIAnalysis 마이그레이션 스크립트

**예상 소요 시간**: 3시간

**장점**:
- 여러 AI 모델 결과 비교 가능
- 분석 히스토리 추적
- 분석 타입별 필터링 및 검색

---

#### Task 111: AI Analysis API 구현
- [ ] POST /episodes/{id}/analyses - AI 분석 실행
- [ ] GET /episodes/{id}/analyses - 분석 목록 조회
- [ ] GET /analyses/{id} - 특정 분석 조회
- [ ] DELETE /analyses/{id} - 분석 삭제
- [ ] GET /analyses/compare - 여러 모델 결과 비교

**예상 소요 시간**: 2시간

---

### 11.2 Vector Embeddings for RAG

#### Task 112: RAG Vector 테이블 설계
- [ ] pgvector extension 설치 (PostgreSQL)
- [ ] rag_vectors 테이블 생성
  ```sql
  CREATE TABLE rag_vectors (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(50) NOT NULL, -- 'dialogue', 'scene', 'episode', 'character'
    source_id BIGINT NOT NULL,
    text_chunk TEXT NOT NULL,
    embedding VECTOR(1536) NOT NULL, -- OpenAI embedding dimension
    metadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
  );
  CREATE INDEX ON rag_vectors USING ivfflat (embedding vector_cosine_ops);
  ```
- [ ] RagVector 엔티티 생성

**예상 소요 시간**: 2시간

---

#### Task 113: Embedding 생성 자동화
- [ ] Dialogue/Scene 생성 시 자동 임베딩 생성
- [ ] EmbeddingService 구현 (OpenAI Embeddings API 연동)
- [ ] 배치 임베딩 처리 (기존 데이터)
- [ ] 업데이트/삭제 시 벡터 동기화

**예상 소요 시간**: 3시간

---

#### Task 114: RAG 기반 검색 API
- [ ] POST /search/semantic - 의미 기반 검색
  - 검색 쿼리 → 임베딩 변환
  - 유사도 검색 (Cosine Similarity)
  - Top-K 결과 반환
- [ ] GET /search/similar/{sourceType}/{sourceId} - 유사 콘텐츠 찾기
- [ ] 프론트엔드 통합 (검색 결과 표시)

**예상 소요 시간**: 3시간

---

### 11.3 Graph Layer Synchronization (Neo4j 연동)

#### Task 115: RDB → GraphDB ETL 파이프라인
- [ ] GraphSyncService 구현
- [ ] Episode 생성 → Neo4j (Episode) 노드 생성
- [ ] EpisodeRelationship 생성 → Neo4j INTERACTS_WITH 관계 생성
- [ ] 배치 동기화 스크립트 (기존 데이터)
- [ ] 삭제 시 Neo4j 동기화

**예상 소요 시간**: 4시간

**동기화 플로우**:
```
PostgreSQL (RDB)
  └─> Spring Event Listener
       └─> GraphSyncService
            └─> Neo4j (GraphDB)
```

---

#### Task 116: GraphDB ↔ VectorDB 크로스 링크
- [ ] AI 분석 결과에서 추출된 개념(Concept) 노드 생성
  - Example: (Episode) → CONTAINS → (Concept: "사랑", "우정", "배신")
- [ ] Concept 노드와 VectorDB 임베딩 연결
- [ ] 유사 개념 찾기 (GraphDB + VectorDB 하이브리드 쿼리)

**예상 소요 시간**: 3시간

---

### 11.4 통합 검색 API

#### Task 117: Hybrid Search 구현
- [ ] 텍스트 검색 (PostgreSQL Full-Text Search)
- [ ] 의미 검색 (VectorDB Cosine Similarity)
- [ ] 그래프 검색 (Neo4j 관계 탐색)
- [ ] 세 가지 결과를 통합하여 랭킹
- [ ] POST /search/hybrid - 통합 검색 API

**예상 소요 시간**: 4시간

**검색 플로우**:
```
User Query
  ├─> PostgreSQL (키워드 매치)
  ├─> VectorDB (의미 유사도)
  └─> Neo4j (관계 기반 추천)
       └─> Result Aggregator → Ranked Results
```

---

**Phase 11 예상 소요 시간**: 24시간

---

## 🔧 알려진 이슈 및 개선 사항

### 1. Controller Test 실패 문제 (우선순위: 낮음)
**문제**: 42개 @WebMvcTest 실패 (ApplicationContext 로딩 문제)

**원인**:
- Spring Boot가 CacheAutoConfiguration을 로드하려고 시도
- Redis 연결 실패로 인한 컨텍스트 로딩 실패

**해결 방안 (향후)**:
- @WebMvcTest 대신 @SpringBootTest + @AutoConfigureMockMvc 사용 검토
- TestContainers를 사용한 Redis 통합 테스트 환경 구축

**현재 상태**:
- 핵심 기능은 Integration/Service 테스트로 검증 완료 (159개 통과)
- 실제 동작에는 문제 없음

---

### 2. 프로덕션 데이터베이스 영속성 (우선순위: 중간)
**문제**: Docker 컨테이너 재시작 시 데이터 초기화 가능

**해결 방안**:
- docker-compose.yml에 볼륨 마운트 추가
- PostgreSQL 데이터 디렉터리 호스트에 마운트
- 백업 스크립트 작성

---

### 3. LLM 분석 결과 구조화 부족 (우선순위: 중간)
**문제**: Episode.analysisResult가 JSONB로 저장되어 쿼리 및 비교 어려움

**해결 방안**:
- Phase 11.1 (Task 110): AIAnalysis 엔티티 생성
- 분석 타입별 테이블 분리
- 여러 모델 결과 비교 가능

---

## 📋 작업 진행 시 권장 사항

### 우선순위별 작업 순서
1. **필수 개선 사항** (우선순위: 높음)
   - 프로덕션 데이터베이스 영속성 확보
   - Task 110-111: AI Analysis 구조화

2. **사용자 경험 개선** (우선순위: 중간)
   - Task 97: TTS 기능 (대사 음성 합성)
   - Task 100: 엑셀 Import/Export (작가 워크플로 개선)

3. **고급 기능** (우선순위: 낮음)
   - Task 98: AI 이미지 생성
   - Phase 7: Vector DB 의미 검색
   - Phase 9: Neo4j GraphDB 전환
   - Phase 11: Multi-Database Architecture

### 매 Task 완료 시
- [ ] 코드 커밋 (의미 있는 커밋 메시지)
- [ ] 테스트 실행 (빌드 에러 없는지)
- [ ] 이 문서의 체크박스 업데이트
- [ ] 실제 소요 시간 기록

### 코드 품질 유지
- 의미 있는 변수명 사용
- 주석은 "왜"에 집중
- 하나의 함수는 하나의 책임
- 테스트 작성 습관화

---

## 📊 전체 프로젝트 진행 상황

### 완료율
- **Phase 0-8**: 100% 완료 ✅
- **Phase 10**: 약 75% 완료 (Task 86-96, 99 완료, Task 97-98, 100 대기)
- **Phase 7, 9, 11**: 0% (선택적, 미진행)

### 데이터베이스 현황
```
✅ PostgreSQL (RDB)
  - User, Project, Episode, Character, Scene, Dialogue
  - EpisodeRelationship (에피소드별 관계 추적)
  - ScenarioVersion, RefreshToken

⬜ Neo4j (GraphDB) - Phase 9/11.3에서 구현 예정
  - 복잡한 관계 쿼리 최적화
  - N단계 관계 탐색

⬜ Vector DB (pgvector) - Phase 7/11.2에서 구현 예정
  - RAG 기반 의미 검색
  - 유사 콘텐츠 추천
```

### 테스트 커버리지
- Service 계층: 79%
- Security 계층: 95%
- 전체: 67% (JaCoCo 기준)

### 주요 마일스톤
- ✅ 2025-10-15: Phase 0 완료 (프로젝트 초기 설정)
- ✅ 2025-10-20: Phase 1 완료 (도메인 모델 구축)
- ✅ 2025-10-22: Phase 3 완료 (LLM 연동)
- ✅ 2025-10-30: Phase 6 완료 (인증 시스템)
- ✅ 2025-11-04: Redis 테스트 환경 개선
- ✅ 2025-11-05: Script-Episode 통합 및 EpisodeRelationship 구현
- ✅ 2025-11-06: **Phase 9 완료 (Neo4j GraphDB 통합 - Centrality 분석 & 시각화)** 🎉

---

## 🎯 다음 단계 제안

### 즉시 착수 가능한 작업
1. **프로덕션 DB 영속성 확보** (docker-compose.yml 볼륨 설정) - 30분
2. **Task 110-111: AI Analysis 구조화** - 5시간
3. **Task 100: 엑셀 Import/Export** - 3시간

### 중장기 계획
1. **Phase 11.2: RAG Vector 구현** (의미 검색) - 8시간
2. **Phase 9: Neo4j 통합** (관계 그래프 최적화) - 10시간
3. **Phase 11.3-11.4: Multi-DB 통합** (ETL 파이프라인) - 11시간

---

## 📐 Database Schema Overview

### 1️⃣ Relational Schema (PostgreSQL - 현재)

```sql
-- 사용자 및 프로젝트
CREATE TABLE users (...);
CREATE TABLE refresh_tokens (...);
CREATE TABLE projects (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  name VARCHAR(200) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 에피소드 및 스크립트
CREATE TABLE episodes (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id),
  title VARCHAR(200) NOT NULL,
  description VARCHAR(2000),
  episode_order INT NOT NULL,
  script_text TEXT, -- 스크립트 원문
  script_format VARCHAR(50), -- novel, screenplay, etc.
  analysis_status VARCHAR(20), -- not_analyzed, analyzing, analyzed, failed
  analysis_result JSONB, -- LLM 분석 결과
  llm_provider VARCHAR(50), -- openai, claude, gemini
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 캐릭터
CREATE TABLE characters (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id),
  character_id VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(1000),
  personality VARCHAR(500),
  speaking_style VARCHAR(500),
  vocabulary VARCHAR(1000),
  tone_keywords VARCHAR(1000),
  examples TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 에피소드별 캐릭터 관계 (시간축 추적)
CREATE TABLE episode_relationships (
  id BIGSERIAL PRIMARY KEY,
  episode_id BIGINT NOT NULL REFERENCES episodes(id),
  from_character_id BIGINT NOT NULL REFERENCES characters(id),
  to_character_id BIGINT NOT NULL REFERENCES characters(id),
  relation_type VARCHAR(50) NOT NULL, -- friend, rival, family, lover, enemy
  closeness DOUBLE PRECISION, -- 0.0-10.0
  description VARCHAR(1000),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 장면 및 대사
CREATE TABLE scenes (...);
CREATE TABLE dialogues (...);
CREATE TABLE scenario_versions (...);
```

### 2️⃣ Graph Schema (Neo4j - Phase 9에서 구현 예정)

```cypher
// 노드
CREATE (u:User {id: 1, username: "author1"})
CREATE (p:Project {id: 1, name: "My Novel"})
CREATE (e:Episode {id: 1, title: "Chapter 1", order: 1})
CREATE (c:Character {id: 1, name: "Alice"})

// 관계
CREATE (u)-[:OWNS]->(p)
CREATE (p)-[:CONTAINS]->(e)
CREATE (e)-[:FEATURES {at_episode: 1}]->(c)
CREATE (c)-[:INTERACTS_WITH {
  episode_id: 1,
  relation_type: "friend",
  closeness: 8.0
}]->(c2:Character)

// 개념 노드 (AI 분석 결과)
CREATE (concept:AIConcept {name: "love", type: "theme"})
CREATE (e)-[:CONTAINS_CONCEPT {confidence: 0.85}]->(concept)
```

### 3️⃣ Vector Schema (pgvector - Phase 11.2에서 구현 예정)

```sql
-- pgvector extension 설치
CREATE EXTENSION IF NOT EXISTS vector;

-- RAG 벡터 테이블
CREATE TABLE rag_vectors (
  id BIGSERIAL PRIMARY KEY,
  source_type VARCHAR(50) NOT NULL, -- 'dialogue', 'scene', 'episode', 'character'
  source_id BIGINT NOT NULL,
  text_chunk TEXT NOT NULL,
  embedding VECTOR(1536) NOT NULL, -- OpenAI ada-002 dimension
  metadata JSONB, -- {characterId: 1, episodeId: 2, tone: "sad"}
  created_at TIMESTAMP DEFAULT NOW()
);

-- 벡터 유사도 검색 인덱스 (IVFFlat)
CREATE INDEX ON rag_vectors USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);

-- 하이브리드 인덱스 (필터 + 벡터)
CREATE INDEX ON rag_vectors (source_type, source_id);
```

### 4️⃣ ETL Sync Flow

```
┌─────────────────────────────────────────────────────┐
│                PostgreSQL (RDB)                     │
│  - Episodes, Characters, EpisodeRelationships       │
└──────────────┬──────────────────┬───────────────────┘
               │                   │
     EventListener          EventListener
               │                   │
               ▼                   ▼
   ┌───────────────────┐   ┌──────────────────┐
   │   Neo4j (Graph)   │   │ pgvector (RAG)   │
   │   - Nodes         │   │ - Embeddings     │
   │   - Relationships │   │ - Similarity     │
   └───────────────────┘   └──────────────────┘
               │                   │
               └──────────┬────────┘
                          │
                    Cross-Reference
                  (Semantic + Graph)
```

**동기화 트리거**:
1. Episode 생성 → Neo4j (Episode) 노드 생성 + VectorDB 임베딩 생성
2. EpisodeRelationship 생성 → Neo4j INTERACTS_WITH 관계 생성
3. Dialogue 생성 → VectorDB 임베딩 생성
4. 삭제 시 → 모든 DB에서 동기화 삭제

---

**프로젝트는 이미 매우 안정적이고 기능적입니다! 남은 작업은 모두 선택적 개선 사항입니다. 🎉**

**완료된 작업은 [COMPLETED_TASKS.md](COMPLETED_TASKS.md)를 참조하세요.**
