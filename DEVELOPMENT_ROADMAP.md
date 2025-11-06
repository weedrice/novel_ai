# Novel AI - 개발 로드맵

> 이 문서는 Novel AI 프로젝트의 개발 진행 상황과 향후 계획을 추적하기 위한 로드맵입니다.
> 마지막 업데이트: 2025-10-20

## 📊 현재 프로젝트 상태 분석

### ✅ 완료된 작업

#### 1. 프로젝트 초기 설정 (Phase 0)
- [x] Gradle 기반 Spring Boot 3.4 프로젝트 구조 생성
- [x] Next.js 15 프론트엔드 프로젝트 초기화
- [x] FastAPI 기반 LLM 서버 초기화
- [x] Java 21 설정 완료
- [x] 기본 CORS 설정 구성
- [x] 프로젝트 문서화 (README.md)
- [x] Java 버전 관리 파일 생성 (.tool-versions, .sdkmanrc, .java-version)

#### 2. 기본 API 엔드포인트 구현 (Phase 0)
- [x] Health Check API (`/health`)
- [x] 에피소드 목록 조회 API (`GET /episodes`) - 더미 데이터
- [x] 대사 제안 API (`POST /dialogue/suggest`) - LLM 서버 연동
- [x] LLM 서버 대사 생성 API (`POST /gen/suggest`) - 템플릿 기반 더미 구현

#### 3. 프론트엔드 기본 UI (Phase 0)
- [x] 에피소드 목록 불러오기 UI
- [x] 대사 제안 받기 UI
- [x] API 연동 및 에러 핸들링

### 🔧 현재 기술 스택 요약

| 계층 | 기술 | 버전 | 상태 |
|-----|------|------|------|
| 백엔드 | Spring Boot | 3.4.1 | ✅ 설정 완료 |
| 백엔드 | Java | 25 (Adoptium) | ✅ 설정 완료 |
| 백엔드 | Gradle | 8.11.1 | ✅ 설정 완료 |
| 프론트엔드 | Next.js | 15.0.0 | ✅ 설정 완료 |
| 프론트엔드 | React | 18.3.1 | ✅ 설정 완료 |
| 프론트엔드 | TypeScript | 5.x | ✅ 설정 완료 |
| AI/ML | FastAPI | 0.115.0+ | ✅ 설정 완료 |
| AI/ML | Python | 3.11 | ⏳ 요구사항만 명시 |
| 데이터베이스 | PostgreSQL/H2 | - | ⚠️ 미구현 |
| 인프라 | Docker Compose | - | ⚠️ 미구현 |

### 🔴 현재 시스템의 한계점

1. **데이터베이스 미구현**: 모든 데이터가 하드코딩된 더미 데이터
2. **도메인 모델 미정의**: Character, Episode, Scene 등의 엔티티가 없음
3. **인증/인가 미구현**: 사용자 관리 및 보안 기능 없음
4. **실제 LLM 미연동**: 템플릿 기반 더미 응답만 제공
5. **관계 그래프 미구현**: 캐릭터 간 관계 시각화 기능 없음
6. **스크립트 분석 미구현**: 작성된 대본 파싱 및 분석 기능 없음

---

## 🎯 개발 로드맵

### Phase 1: 핵심 도메인 모델 및 데이터베이스 구축 (우선순위: 최상)

**목표**: 소설/웹툰 스토리텔링의 핵심 도메인을 데이터베이스로 관리

#### 1.1 도메인 모델 설계 및 구현
- [ ] **Character (캐릭터)** 엔티티
  - 기본 정보: id, name, description, personality
  - 말투 프로필: speakingStyle, vocabulary, toneKeywords
  - 관계 정보: relationships (Many-to-Many)

- [ ] **Episode (에피소드)** 엔티티
  - 기본 정보: id, title, description, order
  - 관계: scenes (One-to-Many)

- [ ] **Scene (장면)** 엔티티
  - 기본 정보: id, episodeId, sceneNumber, description
  - 설정: location, mood, participants (캐릭터 목록)
  - 관계: dialogues (One-to-Many)

- [ ] **Dialogue (대사)** 엔티티
  - 기본 정보: id, sceneId, speakerId, text
  - 메타정보: intent, honorific, emotion
  - 타임스탬프: order, timestamp

- [ ] **Relationship (캐릭터 관계)** 엔티티
  - 관계 정보: fromCharacterId, toCharacterId, relationType
  - 상세 정보: closeness, description

#### 1.2 데이터베이스 설정
- [ ] H2 인메모리 데이터베이스 로컬 개발 환경 구성
- [ ] Spring Data JPA 레포지토리 구현
- [ ] 초기 데이터 시드 (data.sql 또는 @PostConstruct)
- [ ] PostgreSQL 연동 준비 (프로필 분리: dev, prod)

#### 1.3 CRUD API 구현
- [ ] Character CRUD API
  - `GET /characters` - 전체 캐릭터 목록
  - `GET /characters/{id}` - 특정 캐릭터 상세
  - `POST /characters` - 캐릭터 생성
  - `PUT /characters/{id}` - 캐릭터 수정
  - `DELETE /characters/{id}` - 캐릭터 삭제

- [ ] Episode CRUD API
  - `GET /episodes` - 에피소드 목록 (현재 더미에서 DB 연동으로 전환)
  - `GET /episodes/{id}` - 에피소드 상세
  - `POST /episodes` - 에피소드 생성
  - `PUT /episodes/{id}` - 에피소드 수정
  - `DELETE /episodes/{id}` - 에피소드 삭제

- [ ] Scene CRUD API (에피소드 내 장면 관리)
- [ ] Dialogue CRUD API (장면 내 대사 관리)
- [ ] Relationship API (캐릭터 관계 관리)

**예상 작업 기간**: 1-2주

---

### Phase 2: 캐릭터 관계 그래프 시각화 (우선순위: 높음)

**목표**: 캐릭터 간의 관계를 직관적으로 편집하고 시각화

#### 2.1 백엔드 API
- [ ] 관계 그래프 조회 API
  - `GET /characters/graph` - 전체 캐릭터 관계 그래프 (노드/엣지 형식)
  - `GET /characters/{id}/relationships` - 특정 캐릭터의 관계 목록

#### 2.2 프론트엔드 UI
- [ ] 그래프 시각화 라이브러리 선택 및 설치
  - 추천: react-flow, cytoscape.js, vis.js 중 선택
- [ ] 캐릭터 노드 렌더링
- [ ] 관계 엣지 렌더링 (관계 유형에 따라 색상/스타일 다르게)
- [ ] 드래그 앤 드롭으로 노드 위치 조정
- [ ] 관계 추가/삭제 UI
- [ ] 캐릭터 선택 시 상세 정보 패널 표시

**예상 작업 기간**: 1주

---

### Phase 3: 말투 프로필링 및 LLM 연동 (우선순위: 최상)

**목표**: 실제 LLM 모델을 사용하여 캐릭터별 말투를 반영한 대사 생성

#### 3.1 말투 프로필 관리
- [ ] Character 엔티티에 말투 프로필 필드 추가
  - vocabulary: 자주 사용하는 단어 목록
  - sentencePatterns: 문장 패턴 예시
  - prohibitedWords: 사용하지 않는 단어
  - examples: 실제 대사 예시 목록

- [ ] 말투 프로필 CRUD API
- [ ] 프론트엔드: 말투 프로필 편집 UI

#### 3.2 LLM 모델 선택 및 통합
- [ ] LLM 모델 선택
  - 옵션 1: OpenAI API (GPT-3.5/4)
  - 옵션 2: Anthropic Claude API
  - 옵션 3: 오픈소스 모델 (LLaMA, Mistral 등) + Hugging Face

- [ ] 프롬프트 엔지니어링
  - 캐릭터 페르소나 프롬프트 템플릿 작성
  - 말투 스타일 주입 방법 설계
  - Few-shot 예시 구성

- [ ] LLM 서버 구현
  - `llm-server/app/services/llm_service.py` - LLM 호출 로직
  - `llm-server/app/services/prompt_builder.py` - 프롬프트 생성
  - API 키 관리 (.env 파일)

- [ ] API 서버에서 LLM 서버 호출 개선
  - 에러 핸들링 강화
  - 타임아웃 설정
  - 재시도 로직

#### 3.3 대사 생성 품질 개선
- [ ] 생성된 대사의 평가 메트릭 정의
- [ ] 사용자 피드백 수집 (대사 평가 기능)
- [ ] 프롬프트 반복 개선

**예상 작업 기간**: 2-3주

---

### Phase 4: 시나리오 제안 및 편집 기능 (우선순위: 중간)

**목표**: 장면별로 시나리오를 생성하고 편집할 수 있는 인터페이스

#### 4.1 시나리오 제안 API
- [ ] 장면 기반 시나리오 생성 API
  - `POST /scenes/{sceneId}/generate-scenario`
  - 입력: 장면 설정, 참여 캐릭터, 분위기
  - 출력: 전체 대화 흐름 초안

- [ ] 시나리오 저장 및 버전 관리
  - ScenarioVersion 엔티티 (시나리오 이력 관리)

#### 4.2 프론트엔드 시나리오 편집기
- [ ] 장면별 시나리오 편집 UI
- [ ] 대사 순서 드래그 앤 드롭
- [ ] 대사 편집/삭제 기능
- [ ] 캐릭터별 대사 하이라이팅
- [ ] 시나리오 내보내기 (텍스트, JSON, PDF)

**예상 작업 기간**: 2주

---

### Phase 5: 스크립트 검수 및 분석 도구 (우선순위: 중간)

**목표**: 기존 스크립트를 분석하여 캐릭터, 관계, 장면 정보 추출

#### 5.1 스크립트 파싱
- [ ] 스크립트 형식 정의 (예: 파운틴, 대본 형식)
- [ ] 파싱 로직 구현
  - 화자 인식
  - 대사 추출
  - 지문/설명 분리

- [ ] 스크립트 업로드 API
  - `POST /scripts/upload`
  - `POST /scripts/parse`

#### 5.2 자동 분석 기능
- [ ] 캐릭터 자동 인식 및 등록 제안
- [ ] 관계 추론 (대화 빈도, 호칭 분석)
- [ ] 장면 자동 분할
- [ ] 말투 패턴 학습 (기존 대사에서 학습)

#### 5.3 교정 제안
- [ ] 캐릭터 일관성 검사 (말투 불일치 감지)
- [ ] 호칭 일관성 검사
- [ ] 오타 및 문법 검사 (선택적)

**예상 작업 기간**: 2-3주

---

### Phase 6: 사용자 인증 및 권한 관리 (우선순위: 중간)

**목표**: 다중 사용자 지원 및 프로젝트 관리

#### 6.1 인증 시스템
- [ ] User 엔티티 설계
- [ ] Spring Security 설정
- [ ] JWT 기반 인증 구현
- [ ] 회원가입/로그인 API
- [ ] 소셜 로그인 (선택적: Google, GitHub)

#### 6.2 프로젝트 관리
- [ ] Project 엔티티 (여러 프로젝트 관리)
- [ ] 프로젝트별 캐릭터/에피소드 분리
- [ ] 프로젝트 공유 및 협업 기능 (선택적)

#### 6.3 프론트엔드
- [ ] 로그인/회원가입 페이지
- [ ] 프로젝트 선택 및 관리 UI
- [ ] 인증 토큰 관리 (JWT 저장/갱신)

**예상 작업 기간**: 1-2주

---

### Phase 7: Vector DB 및 의미 검색 (우선순위: 낮음)

**목표**: 대사 예시 및 캐릭터 설정을 벡터 DB에 저장하여 유사도 기반 검색

#### 7.1 Vector DB 연동
- [ ] Vector DB 선택 (Pinecone, Qdrant, Weaviate 등)
- [ ] 임베딩 모델 선택 (OpenAI Embeddings, Sentence Transformers)
- [ ] 캐릭터 대사 임베딩 저장
- [ ] 유사 대사 검색 API

#### 7.2 활용 시나리오
- [ ] 과거 대사 중 유사한 상황 찾기
- [ ] 캐릭터 성격에 맞는 예시 자동 추천
- [ ] RAG(Retrieval-Augmented Generation) 기반 대사 생성

**예상 작업 기간**: 1-2주

---

### Phase 8: Docker 및 배포 자동화 (우선순위: 중간)

**목표**: 로컬 및 프로덕션 환경에서 손쉽게 배포

#### 8.1 Docker 컨테이너화
- [ ] api-server Dockerfile 작성
- [ ] frontend Dockerfile 작성
- [ ] llm-server Dockerfile 작성
- [ ] docker-compose.yml 작성
  - 서비스: api-server, frontend, llm-server, postgres
  - 네트워크 구성
  - 볼륨 마운트 (DB 데이터 영속화)

#### 8.2 CI/CD 파이프라인
- [ ] GitHub Actions 워크플로우 작성
  - 빌드 자동화
  - 테스트 실행
  - Docker 이미지 빌드 및 푸시
  - 배포 자동화 (선택적)

#### 8.3 배포 환경 구성
- [ ] 클라우드 플랫폼 선택 (AWS, GCP, Azure, Vercel 등)
- [ ] 프로덕션 환경 설정
- [ ] 환경 변수 관리 (시크릿 관리)
- [ ] 모니터링 및 로깅 설정

**예상 작업 기간**: 1-2주

---

### Phase 9: 고급 기능 및 최적화 (우선순위: 낮음)

**목표**: 사용자 경험 개선 및 시스템 최적화

#### 9.1 UI/UX 개선
- [ ] 디자인 시스템 구축 (Tailwind CSS 활용)
- [ ] 반응형 디자인
- [ ] 다크 모드 지원
- [ ] 키보드 단축키
- [ ] 튜토리얼 및 온보딩

#### 9.2 성능 최적화
- [ ] API 응답 캐싱 (Redis)
- [ ] 데이터베이스 쿼리 최적화
- [ ] LLM 응답 스트리밍 (Server-Sent Events)
- [ ] 프론트엔드 코드 스플리팅

#### 9.3 테스트 커버리지 향상
- [ ] 단위 테스트 (JUnit, Jest)
- [ ] 통합 테스트
- [ ] E2E 테스트 (Playwright, Cypress)

#### 9.4 추가 기능
- [ ] 대사 음성 합성 (TTS) 연동
- [ ] 이미지 생성 (캐릭터 비주얼 AI 생성)
- [ ] 플롯 구조 시각화 (기승전결 분석)
- [ ] 엑셀/스프레드시트 가져오기/내보내기

**예상 작업 기간**: 지속적 개선

---

## 🚀 권장 개발 순서

우선순위와 의존성을 고려한 권장 순서:

1. **Phase 1** (필수): 도메인 모델 및 DB 구축
2. **Phase 3** (필수): LLM 연동 (실제 기능 구현)
3. **Phase 2** (필수): 관계 그래프 시각화
4. **Phase 4** (중요): 시나리오 제안 및 편집
5. **Phase 8** (배포 준비): Docker 및 CI/CD
6. **Phase 6** (프로덕션 준비): 인증 및 권한
7. **Phase 5** (부가 기능): 스크립트 검수
8. **Phase 7** (최적화): Vector DB
9. **Phase 9** (지속 개선): 고급 기능

---

## 📝 다음 작업 (Next Actions)

### 즉시 시작 가능한 작업

#### 1. 도메인 모델 설계 및 구현 (Phase 1.1)
**파일 생성 필요**:
```
api-server/src/main/java/com/jwyoo/api/entity/
  ├── Character.java
  ├── Episode.java
  ├── Scene.java
  ├── Dialogue.java
  └── Relationship.java

api-server/src/main/java/com/jwyoo/api/repository/
  ├── CharacterRepository.java
  ├── EpisodeRepository.java
  ├── SceneRepository.java
  ├── DialogueRepository.java
  └── RelationshipRepository.java
```

**추가 의존성 필요**:
```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
runtimeOnly("com.h2database:h2")
runtimeOnly("org.postgresql:postgresql")
```

#### 2. 데이터베이스 설정 (Phase 1.2)
**파일 수정 필요**:
- `api-server/src/main/resources/application.properties` - JPA 및 H2 설정 추가
- `api-server/src/main/resources/data.sql` - 초기 데이터 시드 작성

#### 3. CRUD API 구현 (Phase 1.3)
**파일 생성 필요**:
```
api-server/src/main/java/com/jwyoo/api/service/
  ├── CharacterService.java
  ├── EpisodeService.java
  └── ... (기타 서비스)

api-server/src/main/java/com/jwyoo/api/controller/
  └── CharacterController.java (신규 생성)

api-server/src/main/java/com/jwyoo/api/dto/
  ├── CharacterDto.java
  ├── EpisodeDto.java
  └── ... (기타 DTO)
```

**파일 수정 필요**:
- `EpisodeController.java` - DB에서 데이터 조회하도록 수정

---

## 📚 참고 자료 및 기술 문서

### Spring Boot / JPA
- [Spring Data JPA 공식 문서](https://spring.io/projects/spring-data-jpa)
- [JPA Entity Relationships](https://www.baeldung.com/jpa-entities)

### LLM 연동
- [OpenAI API 문서](https://platform.openai.com/docs)
- [LangChain 문서](https://python.langchain.com/)
- [Prompt Engineering Guide](https://www.promptingguide.ai/)

### 프론트엔드
- [Next.js 15 문서](https://nextjs.org/docs)
- [React Flow (그래프 시각화)](https://reactflow.dev/)

### 배포
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)

---

## 🔄 문서 업데이트 가이드

이 로드맵은 프로젝트 진행에 따라 지속적으로 업데이트되어야 합니다.

**업데이트 시점**:
- Phase 완료 시
- 새로운 기능 추가 결정 시
- 기술 스택 변경 시
- 우선순위 재조정 시

**업데이트 방법**:
1. 완료된 항목은 `- [ ]`를 `- [x]`로 변경
2. "현재 프로젝트 상태 분석" 섹션 업데이트
3. "다음 작업" 섹션 갱신
4. 상단의 "마지막 업데이트" 날짜 수정

---

## 💡 기술적 고려사항

### 확장성
- 마이크로서비스 아키텍처 유지
- API 버저닝 (v1, v2 등)
- 수평 확장 가능한 설계

### 보안
- API 키 환경변수 관리
- HTTPS 적용
- SQL Injection 방지 (JPA 사용)
- XSS, CSRF 방어

### 성능
- 데이터베이스 인덱싱 전략
- LLM 호출 비용 최적화 (캐싱, 배치 처리)
- 프론트엔드 번들 최적화

### 유지보수성
- 코드 컨벤션 통일
- 주석 및 문서화
- 단위 테스트 작성
- 로깅 전략

---

**이 로드맵은 살아있는 문서입니다. 프로젝트가 진행되면서 계속 발전시켜 나갑시다!**