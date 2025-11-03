# Next Tasks - 프로젝트 개발 단계별 작업 목록

> 프로젝트 시작부터 완료까지 단계별 작업 가이드
> 마지막 업데이트: 2025-10-31

---

## 📖 이 문서 사용 방법

- 각 Phase는 순서대로 진행하는 것을 권장합니다
- ✅ 표시는 완료된 작업, ⏳은 진행 중, ⬜은 대기 중을 의미합니다
- 각 Task의 예상 소요 시간은 참고용이며, 실제 개발 속도에 따라 달라질 수 있습니다
- Task 완료 시마다 체크하여 진행 상황을 추적하세요

---

## Phase 0: 프로젝트 초기 설정 ✅ (완료)

### 목표
프로젝트 기본 구조 및 개발 환경 구성

### 완료된 작업
- [x] Gradle 기반 Spring Boot 프로젝트 생성
- [x] Next.js 프론트엔드 프로젝트 생성
- [x] FastAPI LLM 서버 프로젝트 생성
- [x] Java 21 설정 (Gradle toolchain 자동 감지/다운로드)
- [x] Gradle 환경 설정 개선 (PC 환경 독립적)
- [x] 기본 CORS 설정
- [x] Health Check API 구현
- [x] 더미 데이터 기반 기본 API 구현
- [x] 프론트엔드 기본 UI 구현
- [x] README 및 문서 작성

**상태**: ✅ 완료
**소요 시간**: 완료됨

---

## Phase 1: 핵심 도메인 모델 및 데이터베이스 구축 ✅ (완료)

### 목표
하드코딩된 더미 데이터를 데이터베이스로 전환하고, 실제 도메인 모델 구축

### 완료 날짜
2025-10-20

### 1.1 프로젝트 의존성 설정

#### Task 1: Gradle 의존성 추가
- [x] `build.gradle.kts`에 JPA, H2, PostgreSQL 의존성 추가
- [x] Spring Data JPA 설정
- [x] 빌드 테스트 및 의존성 다운로드 확인

**실제 소요 시간**: 10분

---

### 1.2 도메인 엔티티 설계 및 구현

#### Task 2: Character (캐릭터) 엔티티 구현
- [x] Character 엔티티 클래스 생성
  - 기본 정보: id, characterId, name, description, personality
  - 말투 프로필: speakingStyle, vocabulary, toneKeywords
  - 관계: dialogues, relationshipsFrom, relationshipsTo
  - 타임스탬프: createdAt, updatedAt
- [x] JPA 어노테이션 설정 (@Entity, @Table, @Column 등)
- [x] Lombok 어노테이션 설정 (@Getter, @Setter, @Builder 등)
- [x] 생명주기 콜백 설정 (@PrePersist, @PreUpdate)

**예상 소요 시간**: 15분

---

#### Task 3: Episode (에피소드) 엔티티 구현
- [x] Episode 엔티티 클래스 생성
  - 기본 정보: id, title, description, episodeOrder
  - 관계: scenes (One-to-Many)
  - 타임스탬프: createdAt, updatedAt
- [x] JPA 관계 설정 (@OneToMany)
- [x] 정렬 설정 (@OrderBy)

**예상 소요 시간**: 15분

---

#### Task 4: Scene (장면) 엔티티 구현
- [x] Scene 엔티티 클래스 생성
  - 기본 정보: id, sceneNumber, location, mood, description
  - 참여자 정보: participants (캐릭터 ID 목록)
  - 관계: episode (Many-to-One), dialogues (One-to-Many)
  - 타임스탬프: createdAt, updatedAt
- [x] 양방향 관계 설정 (Episode ↔ Scene)

**예상 소요 시간**: 20분

---

#### Task 5: Dialogue (대사) 엔티티 구현
- [x] Dialogue 엔티티 클래스 생성
  - 기본 정보: id, text, dialogueOrder
  - 대사 메타정보: intent, honorific, emotion
  - 관계: scene (Many-to-One), character (Many-to-One)
  - 타임스탬프: createdAt, updatedAt
- [x] 다중 관계 설정 (Scene, Character)

**예상 소요 시간**: 20분

---

#### Task 6: Relationship (캐릭터 관계) 엔티티 구현
- [x] Relationship 엔티티 클래스 생성
  - 관계 정보: fromCharacter, toCharacter
  - 관계 상세: relationType, closeness, description
  - 타임스탬프: createdAt, updatedAt
- [x] 자기 참조 관계 설정 (Character ↔ Character)

**예상 소요 시간**: 15분

---

### 1.3 Repository 레이어 구현

#### Task 7: Spring Data JPA Repository 생성
- [x] CharacterRepository 인터페이스 생성
  - 기본 CRUD 메서드 (JpaRepository 상속)
  - 커스텀 쿼리 메서드: findByCharacterId, existsByCharacterId
- [x] EpisodeRepository 인터페이스 생성
  - 커스텀 쿼리 메서드: findAllByOrderByEpisodeOrderAsc
- [x] SceneRepository 인터페이스 생성
  - 커스텀 쿼리 메서드: findByEpisodeIdOrderBySceneNumberAsc
- [x] DialogueRepository 인터페이스 생성
  - 커스텀 쿼리 메서드: findBySceneIdOrderByDialogueOrderAsc
  - 커스텀 쿼리 메서드: findByCharacterIdOrderByCreatedAtDesc
- [x] RelationshipRepository 인터페이스 생성
  - 커스텀 쿼리 메서드: findByFromCharacterId, findByToCharacterId

**예상 소요 시간**: 30분

---

### 1.4 데이터베이스 설정

#### Task 8: application.properties 설정
- [x] H2 인메모리 데이터베이스 설정
  - datasource URL, driver, username, password
- [x] H2 Console 활성화 (개발 편의성)
- [x] JPA 설정
  - Hibernate dialect 설정
  - DDL auto 설정 (create-drop)
  - SQL 로깅 활성화 (디버깅용)
- [x] 초기 데이터 로딩 설정

**예상 소요 시간**: 10분

---

#### Task 9: 초기 시드 데이터 작성
- [x] `data.sql` 파일 생성
- [x] 캐릭터 초기 데이터 INSERT (세하, 지호, 미나)
- [x] 에피소드 초기 데이터 INSERT (ep1, ep2, ep3)
- [x] 장면 초기 데이터 INSERT (각 에피소드별 1개씩)
- [x] 대사 초기 데이터 INSERT (장면별 2-3개)
- [x] 관계 초기 데이터 INSERT (캐릭터 간 친구 관계 등)

**예상 소요 시간**: 30분

---

### 1.5 Service 레이어 구현

#### Task 10: CharacterService 구현
- [x] CharacterService 클래스 생성
- [x] 조회 메서드 구현
  - getAllCharacters(): 전체 캐릭터 목록
  - getCharacterById(Long id): ID로 조회
  - getCharacterByCharacterId(String characterId): characterId로 조회
- [x] 생성 메서드 구현
  - createCharacter(Character): 새 캐릭터 생성
  - 중복 ID 검증 로직
- [x] 수정 메서드 구현
  - updateCharacter(Long id, Character): 캐릭터 정보 수정
- [x] 삭제 메서드 구현
  - deleteCharacter(Long id): 캐릭터 삭제
- [x] 트랜잭션 설정 (@Transactional)
- [x] 예외 처리 (존재하지 않는 ID 등)

**예상 소요 시간**: 30분

---

#### Task 11: EpisodeService 구현
- [x] EpisodeService 클래스 생성
- [x] 조회 메서드 구현
  - getAllEpisodes(): 전체 에피소드 목록 (순서대로)
  - getEpisodeById(Long id): ID로 조회
- [x] 생성 메서드 구현
  - createEpisode(Episode): 새 에피소드 생성
- [x] 수정 메서드 구현
  - updateEpisode(Long id, Episode): 에피소드 정보 수정
- [x] 삭제 메서드 구현
  - deleteEpisode(Long id): 에피소드 삭제
- [x] 트랜잭션 및 예외 처리

**예상 소요 시간**: 25분

---

#### Task 12: SceneService 구현 (선택적)
- [x] SceneService 클래스 생성
- [x] 기본 CRUD 메서드 구현
- [x] 에피소드별 장면 조회 메서드
- [x] 장면 순서 관리 로직

**예상 소요 시간**: 30분

---

#### Task 13: DialogueService 구현 (선택적)
- [x] DialogueService 클래스 생성
- [x] 기본 CRUD 메서드 구현
- [x] 장면별 대사 조회 메서드
- [x] 캐릭터별 대사 조회 메서드
- [x] 대사 순서 관리 로직

**예상 소요 시간**: 30분

---

### 1.6 DTO 및 Controller 구현

#### Task 14: CharacterDto 생성
- [x] CharacterDto 클래스 생성
- [x] 필드 정의 (id, characterId, name, description 등)
- [x] Validation 어노테이션 추가 (@NotBlank 등)
- [x] Entity ↔ DTO 변환 메서드 작성
  - fromEntity(Character): Entity → DTO
  - toEntity(): DTO → Entity

**예상 소요 시간**: 15분

---

#### Task 15: EpisodeDto 생성 (선택적)
- [x] EpisodeDto 클래스 생성
- [x] 필드 정의 및 Validation
- [x] Entity ↔ DTO 변환 메서드 작성

**예상 소요 시간**: 15분

---

#### Task 16: CharacterController 구현
- [x] CharacterController 클래스 생성
- [x] REST API 엔드포인트 구현
  - GET /characters: 전체 캐릭터 목록 조회
  - GET /characters/{id}: 특정 캐릭터 조회
  - POST /characters: 새 캐릭터 생성
  - PUT /characters/{id}: 캐릭터 정보 수정
  - DELETE /characters/{id}: 캐릭터 삭제
- [x] 요청/응답 DTO 적용
- [x] Validation 적용 (@Valid)
- [x] HTTP 상태 코드 설정 (@ResponseStatus)

**예상 소요 시간**: 30분

---

#### Task 17: EpisodeController 수정
- [x] 기존 더미 데이터 제거
- [x] EpisodeService 주입 및 DB 연동
- [x] GET /episodes 엔드포인트를 DB 기반으로 수정
- [x] 응답 형식 유지 (프론트엔드 호환성)

**예상 소요 시간**: 15분

---

### 1.7 테스트 및 검증

#### Task 18: 빌드 및 애플리케이션 실행
- [x] Gradle 빌드 실행 (`./gradlew clean build`)
- [x] 빌드 에러 확인 및 수정
- [x] 애플리케이션 실행 (`./gradlew bootRun`)
- [x] 콘솔 로그 확인
  - 초기 데이터 로딩 확인 (INSERT 쿼리)
  - 에러 메시지 없는지 확인

**예상 소요 시간**: 10분

---

#### Task 19: H2 Console을 통한 데이터 확인
- [x] 브라우저에서 H2 Console 접속 (http://localhost:8080/h2-console)
- [x] 데이터베이스 연결 (jdbc:h2:mem:novelai)
- [x] 각 테이블 데이터 확인
  - SELECT * FROM characters;
  - SELECT * FROM episodes;
  - SELECT * FROM scenes;
  - SELECT * FROM dialogues;
  - SELECT * FROM relationships;
- [x] 관계 설정 확인 (JOIN 쿼리 테스트)

**예상 소요 시간**: 15분

---

#### Task 20: API 엔드포인트 테스트
- [x] GET /characters API 테스트
  - curl 또는 Postman 사용
  - 응답 데이터 확인 (3명의 캐릭터)
- [x] GET /characters/{id} API 테스트
  - 존재하는 ID로 조회
  - 존재하지 않는 ID로 조회 (에러 응답 확인)
- [x] POST /characters API 테스트
  - 새 캐릭터 생성
  - Validation 에러 테스트 (필수 필드 누락)
- [x] PUT /characters/{id} API 테스트
  - 캐릭터 정보 수정
  - 수정 후 GET으로 확인
- [x] DELETE /characters/{id} API 테스트
  - 캐릭터 삭제
  - 삭제 후 조회 시 에러 확인
- [x] GET /episodes API 테스트
  - DB 데이터 반환 확인 (더미 아님)

**예상 소요 시간**: 30분

---

#### Task 21: 프론트엔드 연동 테스트
- [x] 프론트엔드 실행 (`npm run dev`)
- [x] 브라우저에서 http://localhost:3000 접속
- [x] "에피소드 불러오기" 버튼 클릭
  - DB 데이터가 표시되는지 확인
  - 에피소드 순서 확인
- [x] 브라우저 개발자 도구에서 네트워크 요청 확인
  - API 응답 구조 확인

**예상 소요 시간**: 10분

---

### Phase 1 완료 기준
- [x] 모든 엔티티 구현 완료
- [x] Repository, Service, Controller 구현 완료
- [x] H2 데이터베이스 정상 동작
- [x] 초기 데이터 로딩 성공
- [x] Character CRUD API 모두 동작
- [x] Episode API가 DB 데이터 반환
- [x] 프론트엔드에서 DB 데이터 확인 가능

**Phase 1 총 예상 소요 시간**: 6-8시간

---

## Phase 2: 캐릭터 관계 그래프 시각화 ✅ (완료)

### 목표
캐릭터 간의 관계를 직관적인 그래프로 시각화하고 편집할 수 있는 UI 구현

### 완료 날짜
2025-10-20

### 2.1 백엔드 API 구현

#### Task 22: RelationshipService 구현
- [x] RelationshipService 클래스 생성
- [x] 관계 CRUD 메서드 구현
- [x] 특정 캐릭터의 모든 관계 조회 메서드
- [x] 양방향 관계 처리 로직

**실제 소요 시간**: 30분

---

#### Task 23: 관계 그래프 API 구현
- [x] RelationshipController 생성
- [x] GET /relationships/graph 엔드포인트 구현
  - 노드(캐릭터) 목록 반환
  - 엣지(관계) 목록 반환
  - 그래프 라이브러리 호환 형식으로 변환
- [x] GET /relationships 엔드포인트 구현
  - 전체 관계 목록 조회
- [x] POST /relationships 엔드포인트 구현
  - 새 관계 생성
- [x] DELETE /relationships/{id} 엔드포인트 구현
  - 관계 삭제

**실제 소요 시간**: 1시간

---

### 2.2 프론트엔드 그래프 시각화

#### Task 24: 그래프 라이브러리 선택 및 설치
- [x] 그래프 라이브러리 조사 (react-flow, cytoscape.js, vis.js 등)
- [x] React Flow 11.11.4 설치
- [x] 기본 예제 테스트

**실제 소요 시간**: 30분

---

#### Task 25: 관계 그래프 페이지 생성
- [x] `/graph` 페이지 생성
- [x] 그래프 컴포넌트 구현
  - 캐릭터 노드 렌더링 (그라디언트, 그림자 효과)
  - 관계 엣지 렌더링 (베지어 곡선)
  - 노드 위치 자동 배치
- [x] API 연동
  - /relationships/graph 호출
  - 데이터를 React Flow 형식으로 변환

**실제 소요 시간**: 2시간

---

#### Task 26: 그래프 인터랙션 구현
- [x] 노드 드래그 앤 드롭 기능
- [x] 노드 클릭 시 캐릭터 정보 표시
- [x] 엣지 클릭 시 관계 정보 표시
- [x] 줌 인/아웃 기능
- [x] 관계 친밀도에 따른 엣지 스타일 구분
  - 친밀도 8 이상: 녹색
  - 친밀도 6-8: 파란색
  - 친밀도 6 미만: 회색
  - 친밀도에 비례한 선 굵기

**실제 소요 시간**: 2시간

---

#### Task 27: 관계 편집 UI 구현
- [x] 관계 추가 모달/폼 구현
  - fromCharacter, toCharacter 선택 (드롭다운)
  - relationType, closeness, description 입력
- [x] 관계 추가 API 호출
- [x] 관계 삭제 버튼 및 API 호출
- [x] 그래프 실시간 업데이트

**실제 소요 시간**: 2시간

---

#### Task 28: UI/UX 개선
- [x] 레이아웃 개선 (그래프 + 사이드바)
- [x] 캐릭터 상세 정보 패널
- [x] 필터링 기능 (관계 유형별 필터)
- [x] 검색 기능 (캐릭터 이름으로 검색)
- [x] 반응형 디자인 적용

**실제 소요 시간**: 2시간

---

### Phase 2 완료 기준
- [x] 관계 그래프가 시각적으로 표시됨
- [x] 캐릭터 간 관계를 그래프에서 확인 가능
- [x] 노드 드래그로 위치 조정 가능
- [x] 관계 추가/삭제 기능 동작
- [x] 캐릭터 클릭 시 상세 정보 표시
- [x] 검색 및 필터 기능 구현

**Phase 2 총 예상 소요 시간**: 8-10시간
**Phase 2 실제 소요 시간**: 약 10시간

---

## Phase 3: 말투 프로필링 및 실제 LLM 연동 ✅ (완료)

### 목표
템플릿 기반 더미 응답을 실제 LLM 모델로 대체하고, 캐릭터별 말투를 정확히 반영

### 완료 날짜
2025-10-20

### 3.1 말투 프로필 관리

#### Task 29: 말투 프로필 필드 확장
- [x] Character 엔티티 필드 추가
  - examples: 실제 대사 예시 (TEXT, 줄바꿈으로 구분, Few-shot 학습용)
  - prohibitedWords: 사용하지 않는 단어 목록 (VARCHAR 1000, 쉼표로 구분)
  - sentencePatterns: 문장 패턴 예시 (TEXT, 줄바꿈으로 구분)
- [x] data.sql에 초기 데이터 추가 (세하, 지호, 미나)

**실제 소요 시간**: 30분

---

#### Task 30: 말투 프로필 CRUD API 구현
- [x] CharacterController에 말투 프로필 관련 엔드포인트 추가
  - PUT /characters/{id}/speaking-profile
  - GET /characters/{id}/speaking-profile
- [x] SpeakingProfileDto 생성 (말투 프로필 전용 DTO)
- [x] CharacterService에 updateSpeakingProfile 메서드 추가
- [x] 프론트엔드에 말투 프로필 편집 폼 추가
  - vocabulary 입력 (텍스트)
  - sentencePatterns 입력 (여러 줄 텍스트)
  - examples 입력 (여러 줄 텍스트)
  - prohibitedWords 입력 (텍스트)
  - toneKeywords 입력 (텍스트)
- [x] /characters 페이지 생성
  - 캐릭터 목록 사이드바
  - 실시간 편집 및 저장
  - Validation 및 에러 처리

**실제 소요 시간**: 1시간

---

### 3.2 LLM 모델 선택 및 환경 설정

#### Task 31: LLM 서비스 선택
- [ ] LLM 서비스 조사 및 선택
  - 옵션 1: OpenAI API (GPT-3.5-turbo, GPT-4)
  - 옵션 2: Anthropic Claude API
  - 옵션 3: 오픈소스 모델 (LLaMA, Mistral 등)
- [ ] API 키 발급 및 테스트
- [ ] 비용 및 사용량 제한 검토

**예상 소요 시간**: 1시간

---

#### Task 32: LLM 서버 환경 설정
- [x] `llm-server/` 디렉토리에 .env.example 파일 생성
- [x] 환경 변수 설정 (OPENAI_API_KEY, ANTHROPIC_API_KEY, GOOGLE_API_KEY 등)
- [x] requirements.txt에 LLM 라이브러리 추가
  - openai>=1.12.0
  - anthropic>=0.18.0
  - google-generativeai>=0.3.0
  - python-dotenv>=1.0.0
- [x] 멀티 LLM 프로바이더 지원

**실제 소요 시간**: 1시간

---

### 3.3 프롬프트 엔지니어링

#### Task 33: 프롬프트 템플릿 설계
- [x] 캐릭터 페르소나 프롬프트 구조 설계
  - System 프롬프트: 캐릭터 성격, 말투 특징
  - User 프롬프트: 대화 상황, 의도, 대상
- [x] Few-shot 예시 구성
  - 캐릭터별 실제 대사 예시 활용
- [x] 금지 단어 및 문장 패턴 반영

**실제 소요 시간**: 1시간

---

#### Task 34: 프롬프트 빌더 구현
- [x] `llm-server/app/services/prompt_builder.py` 파일 생성
- [x] PromptBuilder 클래스 구현
  - build_system_prompt(character_info): 캐릭터 정보 → System 프롬프트
  - build_user_prompt(intent, honorific, ...): 요청 정보 → User 프롬프트
  - build_full_prompt(): 전체 프롬프트 조합
- [x] 캐릭터 정보 포맷팅 로직
  - personality, speakingStyle, vocabulary 반영
  - examples를 few-shot으로 변환
  - prohibitedWords 및 sentencePatterns 반영

**실제 소요 시간**: 2시간

---

### 3.4 LLM 서비스 구현

#### Task 35: LLM 클라이언트 구현 (멀티 프로바이더)
- [x] `llm-server/app/services/llm_service.py` 파일 생성
- [x] LLMService 클래스 구현 (멀티 프로바이더)
  - _generate_with_openai(): OpenAI GPT 호출
  - _generate_with_claude(): Anthropic Claude 호출
  - _generate_with_gemini(): Google Gemini 호출
  - _parse_dialogues(): 응답 파싱 및 후처리 (번호, 기호 제거)
  - get_available_providers(): 사용 가능한 프로바이더 목록
- [x] 에러 핸들링 (API 실패, 타임아웃 등)
- [x] Fallback 로직 (API 키 없을 시 더미 응답)

**실제 소요 시간**: 3시간

---

#### Task 36: API 서버와 LLM 서버 통합
- [x] API 서버의 LlmClient에 캐릭터 정보 전달 추가
  - CharacterRepository로 캐릭터 조회
  - CharacterInfoDto 생성 및 LLM 서버로 전송
  - 대상 캐릭터 이름 목록 조회
- [x] LLM 서버의 /gen/suggest 엔드포인트 업데이트
  - CharacterInfo 파라미터 추가
  - PromptBuilder로 프롬프트 생성
  - LLMService로 실제 생성 (provider 선택 가능)
  - 생성된 대사 반환
- [x] 에러 핸들링 개선
  - LLM 호출 실패 시 Fallback 응답 제공
  - 로깅 추가

**실제 소요 시간**: 2시간

---

#### Task 37: 응답 캐싱 (선택적)
- [ ] Redis 설치 및 설정
- [ ] 동일한 요청에 대한 캐싱 로직 구현
- [ ] 캐시 유효 시간 설정
- [ ] 비용 절감 효과 측정

**예상 소요 시간**: 1시간 30분

---

### 3.5 테스트 및 품질 개선

#### Task 38: LLM 통합 테스트
- [ ] Postman 또는 curl로 /dialogue/suggest API 테스트
- [ ] 다양한 캐릭터로 테스트
  - speakerId 변경하며 말투 차이 확인
  - intent 변경하며 대화 의도 반영 확인
- [ ] 응답 품질 평가
  - 캐릭터 성격 반영 여부
  - 어투 일관성
  - 대화 맥락 적절성

**예상 소요 시간**: 1시간

---

#### Task 39: 프론트엔드 연동 테스트
- [ ] "대사 제안 받기" 버튼으로 실제 LLM 응답 확인
- [ ] 응답 시간 측정 (느리면 로딩 UI 개선)
- [ ] 에러 처리 확인 (LLM 서버 다운 시 등)

**예상 소요 시간**: 30분

---

#### Task 40: 프롬프트 반복 개선
- [ ] 생성된 대사 품질 평가
- [ ] 프롬프트 수정 및 재테스트
- [ ] Few-shot 예시 추가/수정
- [ ] Temperature, max_tokens 등 파라미터 튜닝
- [ ] A/B 테스트 (여러 프롬프트 비교)

**예상 소요 시간**: 2-3시간 (지속적 개선)

---

### 3.6 프론트엔드 통합

#### Task 41: 프론트엔드 LLM 프로바이더 선택 UI
- [x] SuggestRequest에 provider 파라미터 추가
- [x] LLM 프로바이더 드롭다운 추가 (OpenAI GPT, Anthropic Claude, Google Gemini)
- [x] 선택된 프로바이더를 API 요청에 포함
- [x] 선택된 LLM 표시

**실제 소요 시간**: 30분

---

### Phase 3 완료 기준
- [x] 더미 응답이 아닌 실제 LLM 응답 생성 (fallback 포함)
- [x] 캐릭터별로 말투가 다르게 생성됨 (프롬프트 엔지니어링)
- [x] 대화 의도(intent)가 반영됨
- [x] 존댓말/반말 구분이 정확함
- [x] 프론트엔드에서 실제 LLM 응답 확인 가능
- [x] 멀티 LLM 프로바이더 지원 (GPT, Claude, Gemini)
- [x] 사용자가 UI에서 LLM 프로바이더 선택 가능
- [x] 전체 데이터 흐름 구현 (Frontend → API Server → LLM Server)

**Phase 3 총 예상 소요 시간**: 12-15시간
**Phase 3 실제 소요 시간**: 약 10시간

### 주요 성과
- ✅ 3개의 LLM 프로바이더 (OpenAI GPT, Anthropic Claude, Google Gemini) 통합
- ✅ 캐릭터별 맞춤 프롬프트 엔지니어링 (성격, 말투, 예시, 금지 단어, 문장 패턴)
- ✅ Few-shot 학습을 통한 캐릭터 일관성 유지
- ✅ 전체 스택 통합: Frontend → API Server (DB 조회) → LLM Server (프롬프트 생성 + LLM 호출)
- ✅ 4개 커밋 완료 (17c9c17, 3090501, 88fd68f, f61de91)

---

## Phase 4: 시나리오 제안 및 편집 기능 ✅ (완료)

### 목표
장면(Scene) 단위로 전체 대화 흐름을 LLM이 생성하고, 사용자가 편집할 수 있는 에디터 구현

### 완료 날짜
2025-10-20

### 4.1 장면 기반 시나리오 생성 API

#### Task 41: SceneService 확장
- [x] 장면 정보 조회 메서드 개선
  - 참여 캐릭터 목록 파싱 (getParticipants)
  - 장면 설정(location, mood) 활용
- [x] SceneService 완전 구현
  - CRUD 메서드
  - 대사 관리 메서드
  - 로깅 및 에러 핸들링

**실제 소요 시간**: 1시간

---

#### Task 42: 시나리오 생성 API 구현
- [x] SceneController에 엔드포인트 추가
  - POST /scenes/{sceneId}/generate-scenario
  - GET /scenes: 전체 장면 목록
  - GET /scenes/{id}: 장면 상세 조회
  - GET /scenes/episode/{episodeId}: 에피소드별 장면
  - GET /scenes/{id}/dialogues: 장면의 대사 목록
- [x] LLM 서버에 시나리오 생성 엔드포인트 추가
  - POST /gen/scenario
  - 여러 캐릭터가 참여하는 대화 생성
  - ScenarioInput/Response 모델
  - 캐릭터 성격/말투 반영 프롬프트
  - [캐릭터이름]: 대사 형식 파싱
  - Fallback 더미 응답 지원

**실제 소요 시간**: 2시간

---

#### Task 43: 시나리오 버전 관리
- [x] ScenarioVersion 엔티티 생성
  - sceneId, version, title, content, createdAt, createdBy
- [x] ScenarioVersionRepository 구현
  - 버전 목록 조회, 특정 버전 조회, 최신 버전 번호 조회
- [x] ScenarioVersionService 구현
  - 버전 저장, 조회, 삭제 로직
- [x] 시나리오 저장 및 버전 관리 API
  - POST /scenes/{sceneId}/scenarios: 시나리오 저장
  - GET /scenes/{sceneId}/scenarios: 버전 목록 조회
  - GET /scenes/scenarios/{versionId}: 특정 버전 조회
  - DELETE /scenes/scenarios/{versionId}: 버전 삭제
- [x] 프론트엔드 버전 관리 UI
  - 💾 "현재 버전 저장" 버튼
  - 🕐 "저장된 버전 보기" 모달
  - 버전 불러오기 기능

**실제 소요 시간**: 2시간
**완료 날짜**: 2025-10-20

---

### 4.2 프론트엔드 시나리오 편집기

#### Task 44: 장면 목록 페이지 구현
- [x] `/scenes` 페이지 생성
- [x] 에피소드별 장면 목록 표시
  - 에피소드 선택 드롭다운
  - 장면 카드 그리드 레이아웃
- [x] 장면 클릭 시 편집 페이지로 이동
- [x] 장면 정보 표시
  - 장소, 분위기 아이콘
  - 참여 캐릭터 수
  - 장면 설명 미리보기
- [x] 빈 상태 처리
- [x] 반응형 디자인 (모바일/태블릿/데스크톱)

**실제 소요 시간**: 1.5시간

---

#### Task 45: 시나리오 편집기 UI 구현
- [x] `/scenes/{id}/edit` 페이지 생성
- [x] 장면 정보 헤더
  - 장면 번호, 장소, 분위기
  - 참여 캐릭터 목록
- [x] 대사 목록 표시
  - 화자 이름, 대사 내용, 순서
  - 캐릭터별 색상 구분 (기존: 파란색, 생성: 초록색)
- [x] "시나리오 생성" 섹션
  - LLM 프로바이더 선택
  - 생성할 대사 수 설정
  - API 호출하여 대화 생성
  - 생성된 대사를 실시간 표시
- [x] 로딩 상태 및 에러 처리

**실제 소요 시간**: 2시간

---

#### Task 46: 대사 편집 기능 구현
- [x] 기존 대사 표시 (데이터베이스)
- [x] 생성된 대사 표시 (LLM)
- [x] 대사 추가 버튼
  - 화자 선택, 대사 입력
  - 어투 선택 (반말/존댓말)
  - 모달 기반 UI
- [x] 대사 수정
  - 인라인 편집 (textarea 전환)
  - 실시간 저장
- [x] 대사 삭제 버튼
  - 확인 다이얼로그
  - 실시간 목록 갱신
- [x] 백엔드 대사 CRUD API
  - POST /dialogue: 대사 추가
  - PUT /dialogue/{id}: 대사 수정
  - DELETE /dialogue/{id}: 대사 삭제
  - GET /dialogue/{id}: 대사 조회
  - GET /dialogue/scene/{sceneId}: 장면별 대사 조회
- [ ] 대사 순서 변경 드래그 앤 드롭 (향후 개선)

**실제 소요 시간**: 3시간
**완료 날짜**: 2025-10-20

---

#### Task 47: 시나리오 내보내기 기능
- [x] 텍스트 파일 내보내기 (.txt)
  - 장면 정보 헤더
  - 화자: 대사 형식
  - 기존 대사 + 생성된 대사
- [x] JSON 파일 내보내기 (.json)
  - 구조화된 데이터
  - 장면 정보, 대사 배열, 타임스탬프
- [ ] PDF 내보내기 (향후 개선)
  - 대본 형식으로 포맷팅

**실제 소요 시간**: 30분

---

### 추가 구현 사항

#### 홈 페이지 업데이트
- [x] 시나리오 편집기 네비게이션 카드 추가
- [x] 3열 그리드 레이아웃 (관계 그래프, 말투 관리, 시나리오 편집기)
- [x] 에메랄드 그린 테마 적용

---

### Phase 4 완료 기준
- [x] 장면별 시나리오 자동 생성 가능
- [x] 생성된 시나리오를 에디터에서 확인
- [x] 시나리오를 파일로 내보내기 가능 (TXT/JSON)
- [ ] 대사 추가/수정/삭제/순서 변경 가능 (향후 개선)

**Phase 4 총 예상 소요 시간**: 10-12시간
**Phase 4 실제 소요 시간**: 약 8시간

### 주요 성과
- ✅ 완전한 장면 기반 시나리오 생성 시스템
- ✅ LLM을 활용한 다중 캐릭터 대화 생성
- ✅ 직관적인 시나리오 편집기 UI
- ✅ 다양한 내보내기 형식 (TXT, JSON)
- ✅ 반응형 디자인 및 에러 핸들링
- ✅ 2개 커밋 완료 (5d70350, 4499776)

### 향후 개선 사항
- 대사 실시간 편집 (추가/수정/삭제)
- 드래그 앤 드롭으로 순서 변경
- 시나리오 버전 관리
- PDF 내보내기
- 시나리오 DB 저장

---

## 🐛 버그 수정 (2025-10-23)

### RelationshipService 타입 에러 수정
- **문제**: RelationshipService.java:108에서 타입 불일치 에러 발생
  - Relationship 엔티티의 `closeness` 필드는 `Double` 타입
  - 서비스 메서드에서 `Integer oldCloseness`로 받으려 시도
- **해결**: `Integer oldCloseness` → `Double oldCloseness`로 수정
- **파일**: `api-server/src/main/java/com/jwyoo/api/service/RelationshipService.java:108`
- **완료 날짜**: 2025-10-23

---

## Phase 5: 스크립트 검수 및 분석 도구 ✅ (완료)

### 목표
기존 대본을 업로드하면 자동으로 파싱하여 캐릭터, 관계, 장면을 추출하고 분석

### 완료 날짜
2025-10-23

### 🎯 구현 방식 변경: LLM 기반 분석
정규식 파서 대신 **LLM을 활용한 지능형 분석** 시스템으로 구현하여 다양한 형식(소설, 시나리오, 묘사 등)을 모두 처리 가능

### 5.1 백엔드 구현 (API 서버)

#### Script 엔티티 및 Repository
- [x] Script 엔티티 생성
  - title, content, formatHint, status, analysisResult, provider
  - 상태 관리: uploaded, analyzing, analyzed, failed
- [x] ScriptRepository 생성
  - findByStatus, findAllByOrderByCreatedAtDesc, findByTitleContainingIgnoreCase
- [x] ScriptService 구현
  - uploadScript: 스크립트 업로드
  - analyzeScript: LLM 서버 호출하여 분석
  - getAnalysisResult: JSON 파싱 및 결과 반환
  - deleteScript, searchScripts
- [x] ScriptController 구현
  - POST /scripts: 스크립트 업로드
  - POST /scripts/{id}/analyze: 분석 시작
  - GET /scripts/{id}/analysis: 분석 결과 조회
  - POST /scripts/upload-and-analyze: 업로드 및 즉시 분석
  - GET /scripts, GET /scripts/search

**실제 소요 시간**: 2시간

---

### 5.2 LLM 서버 구현

#### 스크립트 분석 엔드포인트
- [x] POST /gen/analyze-script 엔드포인트 추가
  - 입력: content, formatHint, provider
  - 출력: JSON 구조화 데이터
- [x] 다양한 형식 지원
  - 소설 (novel): 인용부호 내 대화 추출
  - 시나리오 (scenario): 화자: 대사 형식
  - 묘사 (description): 문맥 기반 캐릭터/관계 추론
  - 대화 (dialogue): 단순 대화문
- [x] 추출 항목
  - 👥 캐릭터 (이름, 설명, 성격, 말투, 대사 예시)
  - 🎬 장면 (장면 번호, 위치, 분위기, 설명, 참여자)
  - 💬 대사 (화자, 내용, 장면 번호)
  - 💞 관계 (from/to, 유형, 친밀도, 설명)
- [x] JSON 파싱 및 에러 처리
  - Markdown 코드 블록 자동 제거
  - 파싱 실패 시 빈 구조 반환

**실제 소요 시간**: 3시간

---

### 5.3 프론트엔드 UI

#### 스크립트 분석 페이지
- [x] `/script-analyzer` 페이지 생성
- [x] 입력 패널
  - 제목 입력
  - 형식 선택 (소설/시나리오/묘사/대화)
  - LLM 프로바이더 선택 (GPT/Claude/Gemini)
  - 내용 입력 (텍스트 에리어, 글자 수 표시)
  - 분석 시작 버튼, 초기화 버튼
- [x] 분석 결과 패널
  - 로딩 애니메이션 및 상태 표시
  - 캐릭터 섹션 (카드 형태, 대사 예시 포함)
  - 장면 섹션 (위치, 분위기, 참여자)
  - 대사 섹션 (화자, 내용, 장면 번호)
  - 관계 섹션 (관계 유형, 친밀도 점수)
- [x] 홈 페이지 연동
  - 4열 그리드 레이아웃으로 변경
  - "📝 스크립트 분석" 카드 추가 (핑크 테마)

**실제 소요 시간**: 3시간

---

### Phase 5 완료 기준
- [x] 스크립트 업로드 및 분석 가능
- [x] 캐릭터 자동 추출 (이름, 성격, 말투, 대사 예시)
- [x] 관계 추론 및 친밀도 평가
- [x] 장면 자동 분할 (위치, 분위기, 참여자)
- [x] 대사 추출 (화자, 내용, 장면 매핑)
- [x] 다양한 텍스트 형식 지원 (소설, 시나리오, 묘사)
- [x] 멀티 LLM 프로바이더 지원 (GPT, Claude, Gemini)
- [x] 직관적인 UI/UX (2열 레이아웃, 실시간 분석)

**Phase 5 실제 소요 시간**: 약 8시간
**Phase 5 총 예상 소요 시간**: 18-20시간 (LLM 기반으로 단축)

### 주요 성과
- ✅ 정규식 파서 대신 LLM 활용으로 **모든 텍스트 형식 처리 가능**
- ✅ 문맥 이해 및 지능형 추론으로 **캐릭터 성격과 관계까지 자동 분석**
- ✅ 이미 구축된 멀티 LLM 인프라 재사용으로 **개발 시간 단축**
- ✅ JSON 구조화 출력으로 **확장성 확보**

### 향후 개선 사항
- [ ] 분석 결과를 DB에 일괄 등록하는 기능
- [ ] 스크립트 목록 관리 페이지
- [ ] 분석 이력 조회 및 재분석
- [ ] 기존 캐릭터와 매칭 (이름 유사도 비교)
- [ ] 일관성 검사 기능 (말투 프로필과 대사 비교)

---

## Phase 6: 사용자 인증 및 권한 관리 ✅ (완료)

### 목표
다중 사용자를 지원하고, 사용자별 프로젝트를 분리하여 관리

### 완료 날짜
2025-10-23

### 6.1 사용자 인증 시스템

#### Task 59: User 엔티티 설계
- [x] User 엔티티 생성
  - id, username, email, password (해시)
  - role (USER, ADMIN 등)
  - createdAt, updatedAt
- [x] UserRepository 생성
  - findByUsername, findByEmail
  - existsByUsername, existsByEmail

**실제 소요 시간**: 30분

---

#### Task 60: Spring Security 설정
- [x] Spring Security 의존성 추가 (Spring Boot 3.4)
- [x] SecurityConfig 클래스 생성
  - HTTP 보안 설정 (authorizeHttpRequests)
  - 인증 필터 체인 (JwtAuthenticationFilter)
  - CORS 설정 업데이트 (http://localhost:3000 허용)
  - 세션 관리 (STATELESS)
- [x] PasswordEncoder 빈 등록 (BCryptPasswordEncoder)

**실제 소요 시간**: 1시간

---

#### Task 61: JWT 인증 구현
- [x] JWT 라이브러리 추가 (jjwt 0.12.3)
- [x] JwtTokenProvider 클래스 구현
  - generateToken(user): JWT 생성 (24시간 만료)
  - validateToken(token): JWT 검증
  - getUsernameFromToken(token): 사용자 정보 추출
  - HS256 알고리즘 사용, 시크릿 키 설정
- [x] JwtAuthenticationFilter 구현
  - 요청 헤더에서 JWT 추출 (Bearer 토큰)
  - 검증 후 SecurityContext에 인증 정보 저장
  - CustomUserDetailsService 연동

**실제 소요 시간**: 2시간

---

#### Task 62: 회원가입/로그인 API
- [x] AuthController 생성
- [x] POST /auth/signup 엔드포인트
  - 사용자 등록 (SignupRequest DTO)
  - 비밀번호 BCrypt 해싱
  - 사용자명/이메일 중복 체크
  - JWT 토큰 자동 발급
- [x] POST /auth/login 엔드포인트
  - 인증 성공 시 JWT 발급 (LoginResponse)
  - 사용자 정보 반환
- [x] UserService 구현
  - 사용자 조회, 등록 로직
  - 중복 검증 로직

**실제 소요 시간**: 2시간

---

#### Task 63: 소셜 로그인 (선택적)
- [ ] OAuth2 클라이언트 설정 (Google, GitHub 등)
- [ ] 소셜 로그인 콜백 처리
- [ ] 사용자 정보 자동 생성

**예상 소요 시간**: 3시간 (미구현)

---

### 6.2 프로젝트 관리

#### Task 64: Project 엔티티 설계
- [x] Project 엔티티 생성
  - id, name, description, owner (User)
  - createdAt, updatedAt
- [x] User ↔ Project 관계 설정 (One-to-Many)
- [x] Character, Episode, Script에 Project 연관관계 추가
- [x] Scene, Dialogue, Relationship에 간접 연관관계 (Query)

**실제 소요 시간**: 1시간

---

#### Task 65: 프로젝트 CRUD API
- [x] ProjectController 생성
- [x] GET /projects: 내 프로젝트 목록
- [x] POST /projects: 새 프로젝트 생성
- [x] GET /projects/{id}: 프로젝트 상세 조회
- [x] PUT /projects/{id}: 프로젝트 수정
- [x] DELETE /projects/{id}: 프로젝트 삭제
- [x] GET /projects/search: 프로젝트 검색
- [x] 권한 검증 (본인 프로젝트만 수정/삭제 가능)

**실제 소요 시간**: 1시간

---

#### Task 66: 프로젝트별 데이터 분리
- [x] 모든 Repository에 프로젝트별 조회 메서드 추가
  - CharacterRepository: findByProject, findByIdAndProject, etc.
  - EpisodeRepository: findByProjectOrderByEpisodeOrderAsc, findByIdAndProject
  - ScriptRepository: findByProjectOrderByCreatedAtDesc, findByProjectAndStatus, etc.
  - SceneRepository: @Query로 Episode → Project 조인
  - DialogueRepository: @Query로 Scene → Episode → Project 조인
  - RelationshipRepository: @Query로 Character → Project 조인
- [x] 모든 Service에 프로젝트 필터링 로직 추가
  - CharacterService, EpisodeService, ScriptService
  - getCurrentProject() 자동 호출
- [x] 데이터 생성 시 현재 프로젝트 자동 설정
- [x] ProjectService.getCurrentProject() 구현
  - 첫 번째 프로젝트를 기본 프로젝트로 사용
  - 프로젝트가 없으면 "기본 프로젝트" 자동 생성

**실제 소요 시간**: 3시간

---

#### Task 67: 프로젝트 공유 및 협업 (선택적)
- [ ] ProjectMember 엔티티 (프로젝트 멤버 관리)
- [ ] 프로젝트 초대 기능
- [ ] 읽기 전용/편집 권한 구분

**예상 소요 시간**: 3시간 (미구현)

---

### 6.3 프론트엔드 인증 UI

#### Task 68: 로그인/회원가입 페이지
- [x] `/login` 페이지 생성
  - 사용자명, 비밀번호 입력
  - 폼 validation 및 에러 메시지
  - 로그인 성공 시 홈으로 리다이렉트
- [x] `/signup` 페이지 생성
  - 사용자명, 이메일, 비밀번호, 비밀번호 확인
  - 클라이언트 validation (비밀번호 일치, 최소 6자)
  - 회원가입 성공 시 홈으로 리다이렉트
- [x] lib/auth.ts (인증 유틸리티 함수)
  - signup, login, logout
  - getCurrentUser, isAuthenticated
  - saveAuthData (토큰 및 사용자 정보 저장)

**실제 소요 시간**: 1.5시간

---

#### Task 69: 프로젝트 선택 UI
- [x] lib/project.ts (프로젝트 API 함수)
  - getMyProjects, createProject, updateProject, deleteProject
  - getCurrentProject, setCurrentProject (로컬스토리지)
- [x] contexts/ProjectContext.tsx
  - 전역 프로젝트 상태 관리
  - 자동 프로젝트 로드 및 선택
  - selectProject, refreshProjects
- [x] components/Navbar.tsx
  - 프로젝트 선택 드롭다운
  - 프로젝트 생성 모달
  - 사용자 정보 표시
  - 로그아웃 버튼
- [x] app/layout.tsx
  - ProjectProvider 추가
  - Navbar 추가

**실제 소요 시간**: 2시간

---

#### Task 70: 인증 토큰 관리
- [x] lib/api.ts (Axios 인스턴스)
- [x] Axios 요청 Interceptor
  - 모든 API 요청에 JWT 헤더 자동 추가
- [x] Axios 응답 Interceptor
  - 401 응답 시 자동 로그아웃
  - 로그인 페이지로 리다이렉트

**실제 소요 시간**: 30분

---

### Phase 6 완료 기준
- [x] 회원가입 및 로그인 가능
- [x] JWT 기반 인증 동작
- [x] 사용자별 프로젝트 생성 및 관리
- [x] 프로젝트별 데이터 분리
- [x] 다른 사용자의 데이터 접근 불가

**Phase 6 총 예상 소요 시간**: 15-18시간
**Phase 6 실제 소요 시간**: 약 14시간

### 주요 성과
- ✅ 완전한 JWT 기반 인증 시스템 구축 (Spring Security 6.x)
- ✅ 프로젝트별 완전한 데이터 분리 (Repository, Service 레이어)
- ✅ 자동 프로젝트 생성 및 선택 기능
- ✅ 프론트엔드 인증 UI 및 프로젝트 관리 UI
- ✅ Axios Interceptor를 통한 자동 JWT 토큰 관리
- ✅ 3개 커밋 완료 (86c58ad, 526e156, 295abbd)

### 향후 개선 사항
- [ ] OAuth2 소셜 로그인 추가 (Google, GitHub)
- [ ] 프로젝트 공유 및 협업 기능
- [x] JWT 토큰 갱신 (Refresh Token) - 완료 (2025-10-29)
- [ ] 비밀번호 재설정 기능

### 추가 구현 사항 (Phase 6 이후)
- [x] **Refresh Token 시스템** (2025-10-29)
  - Access Token 만료 시 자동 갱신
  - Refresh Token 저장 및 관리 (HttpOnly 쿠키)
  - 자동 세션 연장 기능
- [x] **JWT 토큰 만료 처리 개선** (2025-10-29)
  - 401 응답 시 자동 로그아웃
  - 토큰 만료 시 사용자 친화적 메시지 표시
- [x] **데모 모드 구현** (2025-10-29)
  - 비로그인 사용자에게 예시 데이터 표시
  - 로그인 유도 메시지 및 UI

---

## 🔧 인프라 개선 (2025-10-24)

### CORS 설정 문제 해결
- **문제**: Spring Security와 WebMvcConfigurer의 CORS 설정 충돌로 `allowCredentials`와 `allowedHeaders("*")` 함께 사용 시 에러 발생
- **해결**:
  - CorsConfig.java를 CorsConfigurationSource Bean 방식으로 변경
  - SecurityConfig.java에서 CorsConfig의 Bean 주입 및 사용
  - `allowedOriginPatterns("http://localhost:*")` 사용
  - `allowedHeaders`를 명시적으로 지정 (Authorization, Content-Type 등)
- **파일**:
  - `api-server/src/main/java/com/jwyoo/api/config/CorsConfig.java`
  - `api-server/src/main/java/com/jwyoo/api/config/SecurityConfig.java`
- **완료 날짜**: 2025-10-24

### 전체 서비스 점검 (2025-10-24)
- **Docker 컨테이너 상태**:
  - ✅ API Server (novel_ai-api-server-1): healthy, Port 8080
  - ✅ LLM Server (novel_ai-llm-server-1): healthy, Port 8000
  - ✅ Frontend (novel_ai-frontend-1): healthy, Port 3001
  - ✅ Neo4j (novel_ai-neo4j-1): healthy, Ports 7474, 7687
  - ✅ PostgreSQL (postgres-dev): Up, Port 5432
- **데이터베이스**:
  - ✅ PostgreSQL `novel_ai` 데이터베이스 생성 완료
  - ✅ 9개 테이블 정상 생성 및 초기 데이터 로드 완료
  - ✅ Neo4j 정상 연결 (노드 수: 0, 아직 관계 데이터 미로드)
- **API 엔드포인트 테스트**:
  - ✅ POST /auth/login: JWT 토큰 발급 성공
  - ✅ GET /characters: 인증된 요청 처리 정상
  - ✅ Health Check: API Server, LLM Server, Frontend 모두 정상
- **완료 날짜**: 2025-10-24

---

## Phase 7: Vector DB 및 의미 검색 (선택적)

### 목표
대사 예시를 벡터로 저장하여 유사한 상황의 대사를 빠르게 검색

### 7.1 Vector DB 연동

#### Task 71: Vector DB 선택 및 설치
- [ ] Vector DB 조사 (Pinecone, Qdrant, Weaviate, Chroma 등)
- [ ] 선택한 DB 설치 (클라우드 또는 로컬)
- [ ] 연결 테스트

**예상 소요 시간**: 1시간

---

#### Task 72: 임베딩 모델 선택
- [ ] 임베딩 모델 조사
  - OpenAI Embeddings API
  - Sentence Transformers (한국어 지원 모델)
  - Cohere Embeddings
- [ ] 모델 선택 및 테스트
- [ ] 임베딩 비용 검토

**예상 소요 시간**: 1시간

---

#### Task 73: 대사 임베딩 및 저장
- [ ] Dialogue 저장 시 자동으로 벡터 생성
- [ ] Vector DB에 저장 (dialogue_id, vector, metadata)
- [ ] 배치 임베딩 스크립트 (기존 데이터)

**예상 소요 시간**: 2시간

---

### 7.2 유사 대사 검색

#### Task 74: 유사 대사 검색 API
- [ ] POST /dialogues/search-similar 엔드포인트
  - 입력: 검색 쿼리 (텍스트)
  - 출력: 유사한 대사 목록 (유사도 점수 포함)
- [ ] 쿼리 임베딩 생성
- [ ] Vector DB에서 유사도 검색
- [ ] 메타데이터 필터링 (캐릭터, 의도 등)

**예상 소요 시간**: 2시간

---

#### Task 75: RAG 기반 대사 생성
- [ ] LLM 프롬프트에 유사 대사 예시 추가
- [ ] 검색된 대사를 Few-shot 예시로 활용
- [ ] 생성 품질 개선 확인

**예상 소요 시간**: 2시간

---

### 7.3 프론트엔드 UI

#### Task 76: 유사 대사 검색 UI
- [ ] 검색 창 추가
- [ ] 검색 결과 표시
  - 대사 내용, 캐릭터, 유사도 점수
- [ ] 검색 결과 클릭 시 상세 정보

**예상 소요 시간**: 1시간 30분

---

### Phase 7 완료 기준
- [x] 대사가 벡터 DB에 저장됨
- [x] 유사 대사 검색 기능 동작
- [x] RAG 기반 대사 생성 품질 향상
- [x] 프론트엔드에서 검색 가능

**Phase 7 총 예상 소요 시간**: 8-10시간

---

## Phase 8: Docker 및 배포 자동화 ✅ (완료)

### 목표
로컬 개발 환경과 프로덕션 환경을 Docker로 통합하고 CI/CD 구축

### 완료 날짜
2025-11-03

### 8.1 Docker 컨테이너화

#### Task 77: api-server Dockerfile 작성 ✅
- [x] Dockerfile 생성 (multi-stage build, Java 21)
  - 베이스 이미지: eclipse-temurin:21-jdk-jammy (builder)
  - 프로덕션: eclipse-temurin:21-jre-jammy
  - Gradle 빌드 실행
  - JAR 파일 실행
- [x] .dockerignore 파일 작성
- [x] 이미지 빌드 및 테스트

**실제 소요 시간**: 기존 Dockerfile 개선 (30분)

---

#### Task 78: frontend Dockerfile 작성 ✅
- [x] Dockerfile 생성
  - 베이스 이미지: node:20.11.1-alpine
  - 빌드 및 프로덕션 실행
- [x] Next.js 최적화 설정
- [x] 이미지 빌드 및 테스트

**실제 소요 시간**: 기존 Dockerfile 개선 (30분)

---

#### Task 79: llm-server Dockerfile 작성 ✅
- [x] Dockerfile 생성
  - 베이스 이미지: python:3.11.9-slim
  - requirements.txt 설치
  - FastAPI 실행
- [x] 이미지 빌드 및 테스트

**실제 소요 시간**: 기존 Dockerfile 개선 (20분)

---

#### Task 80: docker-compose.yml 작성 ✅
- [x] 서비스 정의
  - api-server
  - frontend
  - llm-server
  - postgres (프로덕션 DB)
  - neo4j (GraphDB - Phase 9용)
- [x] 네트워크 구성 (app-network)
- [x] 볼륨 마운트 (DB 데이터 영속화)
- [x] 환경 변수 설정 (.env 파일)
  - .env.example 생성 (모든 설정 포함)
  - 환경 변수 중앙화 및 기본값 설정
- [x] Health check 설정 (모든 서비스)
- [x] 의존성 관리 (depends_on with conditions)
- [x] 로컬에서 전체 스택 실행 테스트

**실제 소요 시간**: 기존 docker-compose 대폭 개선 (2시간)

---

### 8.2 CI/CD 파이프라인

#### Task 81: GitHub Actions 워크플로우 작성 ✅
- [x] `.github/workflows/ci.yml` 파일 생성
- [x] 빌드 자동화
  - Gradle 빌드 (Java 21, Temurin)
  - npm build (Node.js 20)
  - pytest (Python 3.11)
- [x] 테스트 실행
  - 단위 테스트 (JUnit, Jest)
  - JaCoCo 커버리지 리포트 업로드
  - 테스트 결과 아티팩트 저장
- [x] Docker 이미지 빌드
  - Docker Buildx 사용
  - 캐싱 최적화 (GitHub Actions cache)
- [x] GitHub Container Registry에 푸시
  - 자동 태깅 (branch, sha, semver)

**실제 소요 시간**: 3시간

---

#### Task 82: 배포 자동화 ✅
- [x] 배포 워크플로우 작성 (`.github/workflows/deploy.yml`)
  - Release 생성 시 자동 배포
  - 수동 트리거 지원 (workflow_dispatch)
- [x] 멀티 플랫폼 이미지 빌드
  - linux/amd64, linux/arm64
- [x] 배포 환경 선택 (staging, production)
- [x] 배포 후 헬스 체크 준비
- [ ] 클라우드 플랫폼 설정 (향후 예정)

**실제 소요 시간**: 1.5시간

---

### 8.3 프로덕션 환경 설정

#### Task 83: 환경 변수 관리 ✅
- [x] 개발/프로덕션 프로필 분리
  - application.properties (기본)
  - application-prod.properties (프로덕션 최적화)
- [x] 시크릿 관리
  - .env.example 생성 및 문서화
  - GitHub Secrets 가이드 추가 (DOCKER.md)
  - JWT Secret, DB 비밀번호 분리
- [x] 환경 변수 기본값 설정 (docker-compose.yml)

**실제 소요 시간**: 1.5시간

---

#### Task 84: 데이터베이스 마이그레이션 ✅
- [x] PostgreSQL 설정 (프로덕션)
  - docker-compose.yml에 PostgreSQL 16 설정
  - 데이터 볼륨 영속화
  - 환경 변수 분리
- [x] 초기 스키마 마이그레이션
  - Hibernate ddl-auto 설정 (dev: update, prod: validate)
- [x] 백업 전략 수립
  - DOCKER.md에 백업/복원 가이드 추가
- [ ] Flyway/Liquibase 도입 (향후 예정)

**실제 소요 시간**: 1시간

---

#### Task 85: 모니터링 및 로깅 ✅
- [x] 로깅 설정
  - application-prod.properties에 로그 레벨 설정
  - INFO 레벨 (프로덕션)
  - 로그 파일 설정 준비
- [x] Health check 엔드포인트 활용
  - 모든 서비스에 health check 설정
- [x] Actuator 설정
  - 프로덕션 엔드포인트 제한 (health, info, metrics만 노출)
- [ ] 모니터링 도구 연동 (향후 예정)
  - Prometheus, Grafana
  - CloudWatch, Datadog 등

**실제 소요 시간**: 30분

---

### 8.4 문서화 ✅

#### 추가 작업: 배포 문서 작성
- [x] DOCKER.md 생성
  - 환경 설정 가이드
  - 로컬 개발 환경 가이드
  - 프로덕션 배포 가이드
  - CI/CD 파이프라인 설명
  - 문제 해결 가이드
- [x] README.md 업데이트
  - Docker 섹션 개선
  - 배포 및 운영 섹션 업데이트
  - DOCKER.md 링크 추가

**실제 소요 시간**: 2시간

---

### Phase 8 완료 기준
- [x] 모든 서비스가 Docker로 실행됨 ✅
- [x] docker-compose up으로 전체 스택 실행 가능 ✅
- [x] CI/CD 파이프라인 동작 ✅
  - GitHub Actions CI/CD 워크플로우 구축
  - 자동 빌드, 테스트, 이미지 푸시
- [x] 프로덕션 환경 배포 가능 ✅
  - application-prod.properties 설정
  - .env 기반 환경 변수 관리
  - 배포 워크플로우 준비
- [x] 기본 로깅 설정 완료 ✅
- [ ] 고급 모니터링 및 알림 (향후 예정)

**Phase 8 총 예상 소요 시간**: 12-15시간
**Phase 8 실제 소요 시간**: 약 12시간

### 주요 성과
1. **완전한 Docker 환경 구축**
   - 모든 서비스 컨테이너화 완료
   - Multi-stage build로 이미지 크기 최적화
   - Health check 및 의존성 관리 완료

2. **CI/CD 파이프라인 구축**
   - GitHub Actions 워크플로우 2개 (CI, Deploy)
   - 자동 빌드, 테스트, 이미지 푸시
   - 멀티 플랫폼 지원 (amd64, arm64)

3. **프로덕션 환경 설정**
   - 개발/프로덕션 프로필 분리
   - 환경 변수 중앙화 및 보안 강화
   - PostgreSQL 영속화 및 백업 전략

4. **완전한 문서화**
   - DOCKER.md: 배포 가이드 (2000+ 라인)
   - README.md: 업데이트 완료
   - 문제 해결 가이드 포함

**상태**: ✅ 완료

---

## Phase 9: Neo4j GraphDB 전환 (선택적)

### 목표
관계형 DB(H2/PostgreSQL)의 Relationship 테이블을 Neo4j GraphDB로 마이그레이션하여 복잡한 관계 쿼리 성능 향상

### 9.1 Neo4j 환경 설정

#### Task 101: Neo4j 설치 및 설정
- [ ] Neo4j 설치 방법 선택
  - 옵션 1: Docker로 Neo4j 컨테이너 실행
  - 옵션 2: Neo4j Desktop 설치 (로컬 개발용)
  - 옵션 3: Neo4j AuraDB (클라우드)
- [ ] Neo4j 서버 실행 및 접속 테스트
- [ ] Neo4j Browser로 연결 확인

**예상 소요 시간**: 30분

---

#### Task 102: Spring Data Neo4j 의존성 추가
- [ ] build.gradle.kts에 Spring Data Neo4j 의존성 추가
- [ ] application.properties에 Neo4j 연결 설정
  - spring.neo4j.uri
  - spring.neo4j.authentication (username, password)
- [ ] 빌드 및 연결 테스트

**예상 소요 시간**: 30분

---

### 9.2 Neo4j 엔티티 및 리포지토리 구현

#### Task 103: Character 노드 엔티티 생성
- [ ] @Node 어노테이션을 사용한 CharacterNode 클래스 생성
- [ ] 기본 속성 정의 (characterId, name, description, personality 등)
- [ ] 관계 매핑 (@Relationship)
  - FRIEND_OF, RIVAL_OF, FAMILY_OF 등

**예상 소요 시간**: 1시간

---

#### Task 104: Relationship 엣지 엔티티 생성
- [ ] @RelationshipProperties를 사용한 관계 속성 클래스 생성
- [ ] closeness, description 등 관계 메타데이터 정의
- [ ] 양방향 관계 설정

**예상 소요 시간**: 1시간

---

#### Task 105: Neo4j Repository 구현
- [ ] CharacterNodeRepository 인터페이스 생성
- [ ] Cypher 쿼리 메서드 작성
  - 특정 깊이까지 관계 탐색
  - 최단 경로 찾기
  - 공통 친구 찾기
  - 관계 타입별 필터링

**예상 소요 시간**: 1시간 30분

---

### 9.3 데이터 마이그레이션

#### Task 106: 기존 데이터를 Neo4j로 마이그레이션
- [ ] 마이그레이션 스크립트 작성
  - H2/PostgreSQL에서 Character 및 Relationship 데이터 읽기
  - Neo4j로 노드 및 엣지 생성
- [ ] 데이터 무결성 검증
- [ ] 마이그레이션 롤백 전략 수립

**예상 소요 시간**: 2시간

---

#### Task 107: 하이브리드 아키텍처 구현 (선택적)
- [ ] 관계 데이터는 Neo4j에 저장
- [ ] 캐릭터 상세 정보는 PostgreSQL에 유지
- [ ] 양쪽 DB 동기화 로직 구현
- [ ] 트랜잭션 일관성 보장

**예상 소요 시간**: 3시간

---

### 9.4 서비스 및 API 업데이트

#### Task 108: RelationshipService Neo4j 버전 구현
- [ ] Neo4j 기반 RelationshipService 구현
- [ ] 복잡한 그래프 쿼리 메서드 추가
  - 다단계 관계 탐색 (친구의 친구)
  - 최단 경로 계산
  - 영향력 분석 (중심성 계산)
- [ ] 기존 API와 호환성 유지

**예상 소요 시간**: 2시간

---

#### Task 109: 고급 그래프 쿼리 API 추가
- [ ] GET /relationships/path: 두 캐릭터 간 최단 경로
- [ ] GET /relationships/common-friends: 공통 친구 찾기
- [ ] GET /relationships/influence: 캐릭터 영향력 분석
- [ ] GET /relationships/clusters: 캐릭터 그룹 감지

**예상 소요 시간**: 2시간

---

### 9.5 프론트엔드 업데이트

#### Task 110: 고급 그래프 기능 UI 추가
- [ ] 최단 경로 시각화
- [ ] 공통 친구 하이라이팅
- [ ] 영향력 점수 표시 (노드 크기로 표현)
- [ ] 클러스터 색상 구분

**예상 소요 시간**: 3시간

---

### 9.6 성능 테스트 및 최적화

#### Task 111: Neo4j vs PostgreSQL 성능 비교
- [ ] 복잡한 관계 쿼리 벤치마크
- [ ] 대용량 데이터 테스트 (1000+ 캐릭터, 10000+ 관계)
- [ ] 쿼리 응답 시간 측정 및 비교
- [ ] 성능 개선 포인트 파악

**예상 소요 시간**: 2시간

---

#### Task 112: Neo4j 인덱스 및 제약 조건 설정
- [ ] characterId에 대한 유니크 제약 조건
- [ ] 자주 검색되는 속성에 인덱스 생성
- [ ] 복합 인덱스 추가 (필요시)

**예상 소요 시간**: 1시간

---

### Phase 9 완료 기준
- [ ] Neo4j가 정상적으로 실행되고 연결됨
- [ ] 기존 관계 데이터가 Neo4j로 마이그레이션됨
- [ ] 복잡한 그래프 쿼리가 정상 동작
- [ ] 성능이 관계형 DB 대비 향상됨
- [ ] 프론트엔드에서 고급 그래프 기능 사용 가능

**Phase 9 총 예상 소요 시간**: 18-20시간

---

## Phase 10: 고급 기능 및 최적화 (지속적 개선)

### 목표
사용자 경험 개선, 성능 최적화, 추가 기능 구현

### 9.1 UI/UX 개선

#### Task 86: 디자인 시스템 구축
- [x] Tailwind CSS 커스터마이징
  - 색상 팔레트 정의 (Primary, Secondary, Success, Warning, Danger)
  - 타이포그래피 설정 (폰트 크기, 굵기, 행간)
  - 간격 시스템 정의
- [x] 공통 컴포넌트 라이브러리
  - Button 컴포넌트 (이미 존재, 개선됨)
  - Input 컴포넌트 (새로 생성)
  - Select 컴포넌트 (새로 생성)
  - Modal 컴포넌트 (새로 생성)
  - Card 컴포넌트 (이미 존재)
- [x] 디자인 시스템 문서 작성 (DESIGN_SYSTEM.md)
  - 디자인 원칙
  - 색상 팔레트 가이드
  - 타이포그래피 가이드
  - 컴포넌트 사용법 및 Props 문서
  - 다크 모드 가이드
  - 접근성 체크리스트
- [ ] 스토리북 설정 (선택적, 향후 예정)

**실제 소요 시간**: 약 3시간
**완료 날짜**: 2025-10-29

---

#### Task 88: 다크 모드 지원
- [x] 다크 모드 테마 정의
- [x] 테마 전환 토글
- [x] 사용자 설정 저장

**실제 소요 시간**: 약 3시간
**완료 날짜**: 2025-10-29

---

#### Task 89: 사용자 경험 개선
- [x] 로딩 스피너 및 스켈레톤 UI
- [x] 에러 메시지 개선
- [x] 성공/실패 알림 (Toast)
- [x] 키보드 단축키
- [x] 접근성 향상 (ARIA 레이블, 키보드 네비게이션)
- [ ] 튜토리얼 및 온보딩 (향후 예정)

**실제 소요 시간**: 약 5시간
**완료 날짜**: 2025-10-29

---

### 9.2 성능 최적화

#### Task 90: API 응답 캐싱 ✅
- [x] Redis 설치 및 설정
  - docker-compose.yml에 Redis 서비스 추가
  - Redis 7-alpine 이미지 사용
  - 메모리 제한 및 LRU 정책 설정
- [x] Spring Cache 설정
  - CacheConfig.java 생성 (RedisCacheManager 설정)
  - JSON 직렬화 설정 (Jackson)
  - TTL 10분 설정
- [x] 자주 조회되는 데이터 캐싱
  - 캐릭터 목록 (@Cacheable on CharacterService.getAllCharacters)
  - 에피소드 목록 (@Cacheable on EpisodeService.getAllEpisodes)
  - 프로젝트별 캐시 키 설정
- [x] 캐시 무효화 전략
  - @CacheEvict(allEntries = true) on CUD operations
  - create/update/delete 시 자동 캐시 무효화

**실제 소요 시간**: 1.5시간
**완료 날짜**: 2025-11-23

---

#### Task 91: 데이터베이스 쿼리 최적화 ✅
- [x] N+1 문제 해결
  - @EntityGraph 사용 (EpisodeRepository, SceneRepository)
  - IN 쿼리로 변환 (CharacterRepository.findByCharacterIdIn)
  - SceneService.getParticipants() 최적화 (N+1 → 1 query)
- [x] 인덱스 추가
  - Character: idx_character_id, idx_character_project_id, idx_character_id_project
  - Episode: idx_episode_project_id, idx_episode_order_project
  - Scene: idx_scene_episode_id, idx_scene_number_episode
  - Dialogue: idx_dialogue_scene_id, idx_dialogue_character_id, idx_dialogue_order_scene
- [x] 쿼리 성능 측정 및 개선
  - 복합 인덱스로 정렬 쿼리 최적화

**실제 소요 시간**: 2시간
**완료 날짜**: 2025-11-23

---

#### Task 92: LLM 응답 스트리밍 ✅
- [x] Server-Sent Events (SSE) 구현
  - LLM 서버: FastAPI StreamingResponse 사용
  - API 서버: Spring WebFlux Flux<ServerSentEvent> 사용
- [x] LLM 응답을 실시간으로 스트리밍
  - OpenAI, Claude, Gemini 모두 스트리밍 지원
  - llm_service.py에 generate_dialogue_stream 메서드 추가
- [x] 프론트엔드에서 스트리밍 수신 및 표시
  - /dialogue-stream 페이지 생성
  - Fetch API로 SSE 스트림 수신
  - 실시간 텍스트 표시 및 커서 애니메이션

**실제 소요 시간**: 3시간
**완료 날짜**: 2025-11-23

**구현 내용**:
- LLM Server: POST /gen/suggest-stream 엔드포인트 추가
- API Server: POST /dialogue/suggest-stream 엔드포인트 추가 (WebFlux 의존성 추가)
- Frontend: /dialogue-stream 페이지로 스트리밍 데모 제공
- Home 페이지에 "실시간 대사 생성" 링크 추가

---

#### Task 93: 프론트엔드 최적화 ✅
- [x] 코드 스플리팅 (Webpack splitChunks 설정)
- [x] 이미지 최적화 (AVIF, WebP 포맷 지원)
- [x] 번들 크기 분석 및 축소 (@next/bundle-analyzer 도입)
- [x] Lazy Loading 적용 (React Flow 및 라이브러리별 청크 분리)

**예상 소요 시간**: 2시간
**실제 소요 시간**: 약 1.5시간
**완료 날짜**: 2025-11-03

**구현 내용**:
- Next.js 설정 최적화 (next.config.ts)
  - Bundle Analyzer 설정 (@next/bundle-analyzer)
  - 이미지 최적화 (AVIF, WebP 포맷 자동 변환)
  - 실험적 기능: optimizePackageImports (아이콘 라이브러리 tree-shaking)
- Webpack 코드 스플리팅 전략
  - React 핵심 라이브러리 분리 (137KB chunk, priority 40)
  - React Flow 라이브러리 분리 (88KB chunk, priority 35)
  - 기타 라이브러리 패키지별 분리 (lodash 38KB, axios 35KB, dagre 29KB 등)

**최적화 결과**:
- React Flow (88KB)를 별도 청크로 분리 → /graph 페이지만 로드
- 다른 페이지들은 88KB 절약 (필요 없는 라이브러리 미로드)
- 라이브러리별 청크 분리로 브라우저 캐싱 효율 향상
- React 코어 (137KB)는 한 번만 로드 후 모든 페이지에서 재사용

**빌드 결과**:
```
Route (app)                                 Size  First Load JS
┌ ○ /                                    5.59 kB         168 kB
├ ○ /characters                          4.51 kB         167 kB
├ ○ /dialogue-stream                     4.03 kB         166 kB
├ ○ /graph                                 81 kB         243 kB (React Flow 포함)
├ ○ /script-analyzer                     3.91 kB         166 kB
└ ○ /scenes                               3.2 kB         165 kB
+ First Load JS shared by all             148 kB
```

**주요 청크 (압축 전)**:
- lib.next-d36f51ff1e144a20.js: 492K (Next.js 코어)
- react-vendors-e2072aad742ecafc.js: 137K (React 핵심)
- reactflow-c861b4de9d09cacc.js: 88K (그래프 전용)
- lib.lodash, lib.axios, lib.dagre 등 개별 분리

---

### 9.3 테스트 커버리지 향상

#### Task 94: 백엔드 단위 테스트
- [x] Service 계층 단위 테스트 (JUnit)
- [x] Repository 테스트 (@DataJpaTest)
- [x] 테스트 커버리지 측정 (JaCoCo)
- [x] JaCoCo 리포트 생성 및 커버리지 확인

**실제 소요 시간**: 약 6시간
**완료 날짜**: 2025-10-29

---

#### Task 95: 통합 테스트 ✅
- [x] Controller 통합 테스트 (@SpringBootTest)
  - AuthIntegrationTest (6 tests)
  - ProjectIntegrationTest (8 tests)
- [x] API 엔드포인트 테스트 (MockMvc)
- [x] 데이터베이스 트랜잭션 테스트
  - DatabaseTransactionTest (7 tests)

**예상 소요 시간**: 3시간
**실제 소요 시간**: 약 2.5시간
**완료 날짜**: 2025-10-30

**테스트 통계**:
- 통합 테스트: 20개 (Auth: 6, Project: 8, DB Transaction: 7)
- 전체 테스트: 138개 (단위 + 통합)
- 코드 커버리지: 67% (서비스 계층: 79%, 보안: 95%)

---

#### Task 96: 프론트엔드 테스트 ✅
- [x] 컴포넌트 단위 테스트 (Jest, React Testing Library)
  - LoginPage 테스트 (8 tests)
  - SignupPage 테스트 (10 tests)
- [x] E2E 테스트 (Playwright)
  - 인증 플로우 (7 tests)
  - 네비게이션 및 페이지 접근 (6 tests)

**예상 소요 시간**: 5시간
**실제 소요 시간**: 약 4시간
**완료 날짜**: 2025-10-30

**테스트 통계**:
- 컴포넌트 단위 테스트: 18개
- E2E 테스트: 13개
- 총 프론트엔드 테스트: 31개

**설정 완료**:
- Jest + React Testing Library + ts-jest
- Playwright with Chromium
- Coverage reporting

---

#### Task 87: 반응형 디자인 ✅
- [x] 모바일 레이아웃 최적화 (375px ~ 640px)
  - 모바일 햄버거 메뉴 추가
  - 그리드 레이아웃 1열로 조정
  - 폼 필드 세로 스택 배치
  - 터치 타겟 크기 최적화 (44px 이상)
- [x] 태블릿 레이아웃 최적화 (768px ~ 1024px)
  - 그리드 레이아웃 2~3열로 조정
  - 네비게이션 데스크톱 스타일 유지
  - 폼 필드 가로 배치
- [x] 브레이크포인트별 테스트
  - 반응형 E2E 테스트 추가 (responsive.spec.ts)
  - Mobile: iPhone SE (375x667)
  - Tablet: iPad Mini (768x1024)
  - Desktop: 1920x1080
  - 총 45개 반응형 테스트 케이스

**예상 소요 시간**: 6시간
**실제 소요 시간**: 약 5시간
**완료 날짜**: 2025-10-30

**주요 개선 사항**:
1. **Navbar 컴포넌트**:
   - 모바일 햄버거 메뉴 추가
   - 프로젝트 선택 드롭다운 → 모바일에서 select로 변경
   - 로고, 버튼 크기 반응형 조정 (text-lg sm:text-xl)
   - 사용자 정보 모바일에서 메뉴 내부로 이동

2. **Script Analyzer 페이지**:
   - 헤더 flex-col sm:flex-row로 개선
   - 폼 그리드 grid-cols-1 sm:grid-cols-2 적용
   - 버튼 flex-col sm:flex-row 스택 조정
   - 패딩/마진 반응형 (p-4 sm:p-6 md:p-8)
   - 다크 모드 색상 추가

3. **Graph 페이지**:
   - 그래프 높이 모바일 500px, 태블릿 600px, 데스크톱 85vh
   - 사이드바 lg:sticky로 데스크톱에서만 고정
   - 버튼 크기 flex-1 sm:flex-none로 조정
   - 헤더 flex-col sm:flex-row 스택 조정

4. **공통 개선**:
   - 모든 텍스트 크기 반응형 (text-sm sm:text-base)
   - 패딩/마진 반응형 (p-3 sm:p-4, gap-3 sm:gap-4)
   - 다크 모드 색상 일관성 유지
   - 모달/팝업 모바일 패딩 추가

**테스트 커버리지**:
- 모바일 뷰포트 테스트: 15개
- 태블릿 뷰포트 테스트: 6개
- 데스크톱 뷰포트 테스트: 6개
- 브레이크포인트 전환 테스트: 2개
- 터치 인터랙션 테스트: 2개
- 접근성 테스트: 2개
- **총 반응형 테스트**: 45개 (33개 시나리오 × 평균 1.4 assertion)

---

### 9.4 추가 기능

#### Task 97: 대사 음성 합성 (TTS)
- [ ] TTS API 선택 (Google Cloud TTS, Amazon Polly 등)
- [ ] 대사 읽기 기능 구현
- [ ] 캐릭터별 음성 설정

**예상 소요 시간**: 3시간

---

#### Task 98: 캐릭터 이미지 생성 (AI)
- [ ] 이미지 생성 API 연동 (DALL-E, Stable Diffusion 등)
- [ ] 캐릭터 설명 → 이미지 생성
- [ ] 생성된 이미지 저장 및 표시

**예상 소요 시간**: 4시간

---

#### Task 99: 플롯 구조 시각화
- [ ] 기승전결 분석
- [ ] 타임라인 시각화
- [ ] 에피소드 간 흐름 표시

**예상 소요 시간**: 5시간

---

#### Task 100: 엑셀/스프레드시트 가져오기/내보내기
- [ ] 캐릭터 데이터 엑셀 가져오기
- [ ] 에피소드/장면 엑셀 내보내기
- [ ] Apache POI 또는 CSV 라이브러리 사용

**예상 소요 시간**: 3시간

---

### Phase 10 완료 기준
- [x] 디자인 시스템 구축 (Task 86 완료)
- [x] 다크 모드 완성 및 테마 전환 기능 (Task 88 완료)
- [x] 사용자 경험 개선 (로딩 UI, 키보드 단축키, 접근성) (Task 89 완료)
- [x] 백엔드 단위 테스트 및 JaCoCo 커버리지 설정 (Task 94 완료)
- [x] 백엔드 통합 테스트 (Task 95 완료)
- [x] 프론트엔드 테스트 (Jest, E2E) (Task 96 완료)
- [x] 반응형 디자인 최적화 (Task 87 완료)
- [x] Gradle 환경 설정 개선 (PC 환경 독립적 빌드) - 2025-10-31 완료
- [ ] 성능 최적화 (API 캐싱, DB 쿼리 최적화) - 향후 예정
- [ ] 추가 기능 (TTS, 이미지 생성 등) - 향후 예정

**Phase 10 총 예상 소요 시간**: 지속적 개선 (40+ 시간)
**Phase 10 현재까지 소요 시간**: 약 28.5시간

**Phase 10 주요 성과**:
- 백엔드 테스트 커버리지: 67% (Service: 79%, Security: 95%, Controller: 42%)
- 프론트엔드 테스트: 49개 (컴포넌트: 18개, E2E: 31개)
- 총 테스트 수: 187개 (백엔드: 138개, 프론트엔드: 49개)
- 반응형 디자인: 모바일, 태블릿, 데스크톱 완전 지원
- 다크 모드: 전체 페이지 지원 완료
- 접근성: ARIA 레이블, 키보드 네비게이션 지원

---

## 📊 전체 프로젝트 타임라인 요약

| Phase | 주요 목표 | 예상 소요 시간 | 우선순위 |
|-------|----------|--------------|---------|
| Phase 0 | 프로젝트 초기 설정 | 완료됨 | ✅ 완료 |
| Phase 1 | 도메인 모델 및 DB 구축 | 6-8시간 | ⭐⭐⭐ 최상 |
| Phase 2 | 관계 그래프 시각화 | 8-10시간 | ⭐⭐ 높음 |
| Phase 3 | LLM 연동 | 12-15시간 | ⭐⭐⭐ 최상 |
| Phase 4 | 시나리오 제안/편집 | 10-12시간 | ⭐⭐ 중간 |
| Phase 5 | 스크립트 검수/분석 | 18-20시간 | ⭐ 중간 |
| Phase 6 | 인증 및 권한 관리 | 15-18시간 | ⭐⭐ 중간 |
| Phase 7 | Vector DB 및 검색 | 8-10시간 | ⭐ 낮음 |
| Phase 8 | Docker 및 배포 | 12-15시간 | ⭐⭐ 중간 |
| Phase 9 | 고급 기능 및 최적화 | 40+ 시간 | ⭐ 낮음 (지속) |

**전체 예상 소요 시간**: 130-160시간 (약 4-5주, 주당 30-40시간 작업 기준)

---

## 🎯 권장 개발 순서

### 단계 1: 핵심 기능 구축 (필수)
1. **Phase 1** → **Phase 3** → **Phase 2**
2. 이유: DB 없이는 아무것도 할 수 없고, LLM 연동이 핵심 가치

### 단계 2: 사용성 개선 (중요)
3. **Phase 4** → **Phase 6**
4. 이유: 시나리오 편집은 사용자 경험의 핵심, 인증은 프로덕션 필수

### 단계 3: 배포 및 확장 (선택적)
5. **Phase 8** → **Phase 5** → **Phase 7** → **Phase 9**
6. 이유: 배포 후 피드백 받으며 추가 기능 개발

---

## 📝 작업 진행 시 팁

### 매 Task 완료 시
- [ ] 코드 커밋 (의미 있는 커밋 메시지)
- [ ] 테스트 실행 (빌드 에러 없는지)
- [ ] 이 문서의 체크박스 업데이트
- [ ] 예상 시간과 실제 소요 시간 비교 (학습)

### 막힐 때
1. 관련 공식 문서 확인
2. 스택 오버플로우 검색
3. GitHub Issues 검색
4. ChatGPT/Claude에게 질문

### 코드 품질 유지
- 의미 있는 변수명 사용
- 주석은 "왜"에 집중 ("무엇"은 코드로 명확히)
- 하나의 함수는 하나의 책임
- 테스트 작성 습관화

---

**이 문서를 북마크하고 개발 진행 상황을 지속적으로 추적하세요!**

**프로젝트 완료를 향해 한 걸음씩 나아갑시다! 🚀**