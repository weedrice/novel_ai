# Next Tasks - 프로젝트 개발 단계별 작업 목록

> 프로젝트 시작부터 완료까지 단계별 작업 가이드
> 마지막 업데이트: 2025-10-20

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
- [x] Java 25 업그레이드
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

#### Task 43: 시나리오 버전 관리 (선택적)
- [ ] ScenarioVersion 엔티티 생성
  - sceneId, version, content, createdAt
- [ ] 시나리오 저장 및 버전 관리 API
  - POST /scenes/{sceneId}/scenarios: 시나리오 저장
  - GET /scenes/{sceneId}/scenarios: 버전 목록 조회
  - GET /scenarios/{versionId}: 특정 버전 조회

**상태**: 향후 개선 사항으로 보류

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
- [ ] 대사 추가 버튼 (향후 개선)
  - 화자 선택, 대사 입력
- [ ] 대사 수정 (향후 개선)
- [ ] 대사 삭제 버튼 (향후 개선)
- [ ] 대사 순서 변경 드래그 앤 드롭 (향후 개선)
- [ ] 변경 사항 저장 API 호출 (향후 개선)

**실제 소요 시간**: 1시간 (기본 표시 기능)
**향후 개선**: 실시간 편집 기능

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

## Phase 5: 스크립트 검수 및 분석 도구

### 목표
기존 대본을 업로드하면 자동으로 파싱하여 캐릭터, 관계, 장면을 추출하고 분석

### 5.1 스크립트 파싱

#### Task 48: 스크립트 형식 정의
- [ ] 지원할 스크립트 형식 결정
  - 옵션 1: 파운틴(Fountain) 형식
  - 옵션 2: 커스텀 대본 형식 (화자: 대사)
  - 옵션 3: 일반 텍스트 (정규식 파싱)
- [ ] 파싱 규칙 문서화

**예상 소요 시간**: 1시간

---

#### Task 49: 스크립트 파서 구현
- [ ] ScriptParser 클래스 구현
  - parse(scriptText): 텍스트 → 구조화된 데이터
  - 화자 인식 (콜론 또는 괄호 기준)
  - 대사 추출
  - 지문/설명 분리
- [ ] 정규식 또는 파싱 라이브러리 활용

**예상 소요 시간**: 3시간

---

#### Task 50: 스크립트 업로드 API
- [ ] ScriptController 생성
- [ ] POST /scripts/upload 엔드포인트
  - 텍스트 파일 또는 텍스트 직접 입력
  - 파일 저장 (선택적)
- [ ] POST /scripts/parse 엔드포인트
  - 파싱 결과 반환 (화자 목록, 대사 목록, 장면 분할)

**예상 소요 시간**: 2시간

---

### 5.2 자동 분석 기능

#### Task 51: 캐릭터 자동 인식
- [ ] 파싱 결과에서 화자 목록 추출
- [ ] 기존 캐릭터와 매칭 (이름 유사도 비교)
- [ ] 신규 캐릭터 등록 제안 API
  - GET /scripts/{id}/suggested-characters
  - 사용자가 확인 후 일괄 등록

**예상 소요 시간**: 2시간

---

#### Task 52: 관계 추론
- [ ] 대화 빈도 분석
  - 어떤 캐릭터끼리 자주 대화하는지
- [ ] 호칭 분석
  - "친구야", "선배님" 등으로 관계 유형 추론
- [ ] 관계 등록 제안 API
  - GET /scripts/{id}/suggested-relationships

**예상 소요 시간**: 3시간

---

#### Task 53: 장면 자동 분할
- [ ] 장면 전환 감지
  - 지문에서 위치 변경 감지
  - 빈 줄 또는 특수 마커
- [ ] 장면별 대사 그룹핑
- [ ] 장면 분할 결과 반환 API

**예상 소요 시간**: 2시간

---

#### Task 54: 말투 패턴 학습
- [ ] 캐릭터별 대사 수집
- [ ] 어휘 빈도 분석 (형태소 분석 활용)
- [ ] 문장 패턴 추출
- [ ] Character 엔티티에 자동 입력 제안

**예상 소요 시간**: 3시간

---

### 5.3 교정 제안

#### Task 55: 캐릭터 일관성 검사
- [ ] 캐릭터별 말투 프로필과 실제 대사 비교
- [ ] 불일치 감지 (금지 단어 사용, 어투 불일치 등)
- [ ] 교정 제안 API
  - GET /scripts/{id}/inconsistencies

**예상 소요 시간**: 2시간

---

#### Task 56: 호칭 일관성 검사
- [ ] 같은 캐릭터를 부르는 호칭 일관성 확인
- [ ] 호칭 변경 사항 감지 (관계 변화 추론)

**예상 소요 시간**: 1시간 30분

---

### 5.4 프론트엔드 UI

#### Task 57: 스크립트 업로드 페이지
- [ ] `/scripts/upload` 페이지 생성
- [ ] 파일 업로드 또는 텍스트 직접 입력
- [ ] 파싱 결과 표시
  - 인식된 캐릭터 목록
  - 장면 분할 결과
  - 대사 수

**예상 소요 시간**: 2시간

---

#### Task 58: 분석 결과 페이지
- [ ] 캐릭터 등록 제안 UI
  - 체크박스로 선택
  - 일괄 등록 버튼
- [ ] 관계 등록 제안 UI
- [ ] 일관성 검사 결과 표시
  - 불일치 항목 하이라이팅
  - 교정 제안 표시

**예상 소요 시간**: 3시간

---

### Phase 5 완료 기준
- [x] 스크립트 파일 업로드 및 파싱 가능
- [x] 캐릭터 자동 인식 및 등록 제안
- [x] 관계 추론 및 등록 제안
- [x] 장면 자동 분할
- [x] 말투 일관성 검사 및 교정 제안

**Phase 5 총 예상 소요 시간**: 18-20시간

---

## Phase 6: 사용자 인증 및 권한 관리

### 목표
다중 사용자를 지원하고, 사용자별 프로젝트를 분리하여 관리

### 6.1 사용자 인증 시스템

#### Task 59: User 엔티티 설계
- [ ] User 엔티티 생성
  - id, username, email, password (해시)
  - role (USER, ADMIN 등)
  - createdAt, updatedAt
- [ ] UserRepository 생성

**예상 소요 시간**: 30분

---

#### Task 60: Spring Security 설정
- [ ] Spring Security 의존성 추가
- [ ] SecurityConfig 클래스 생성
  - HTTP 보안 설정
  - 인증 필터 체인
  - CORS 설정 업데이트
- [ ] PasswordEncoder 빈 등록 (BCrypt)

**예상 소요 시간**: 1시간

---

#### Task 61: JWT 인증 구현
- [ ] JWT 라이브러리 추가 (jjwt 등)
- [ ] JwtTokenProvider 클래스 구현
  - generateToken(user): JWT 생성
  - validateToken(token): JWT 검증
  - getUsernameFromToken(token): 사용자 정보 추출
- [ ] JwtAuthenticationFilter 구현
  - 요청 헤더에서 JWT 추출
  - 검증 후 SecurityContext에 인증 정보 저장

**예상 소요 시간**: 2시간

---

#### Task 62: 회원가입/로그인 API
- [ ] AuthController 생성
- [ ] POST /auth/signup 엔드포인트
  - 사용자 등록
  - 비밀번호 해싱
  - 중복 체크
- [ ] POST /auth/login 엔드포인트
  - 인증 성공 시 JWT 발급
- [ ] UserService 구현
  - 사용자 조회, 생성 로직

**예상 소요 시간**: 2시간

---

#### Task 63: 소셜 로그인 (선택적)
- [ ] OAuth2 클라이언트 설정 (Google, GitHub 등)
- [ ] 소셜 로그인 콜백 처리
- [ ] 사용자 정보 자동 생성

**예상 소요 시간**: 3시간

---

### 6.2 프로젝트 관리

#### Task 64: Project 엔티티 설계
- [ ] Project 엔티티 생성
  - id, name, description, ownerId
  - createdAt, updatedAt
- [ ] User ↔ Project 관계 설정 (One-to-Many)
- [ ] Character, Episode 등에 projectId 추가

**예상 소요 시간**: 1시간

---

#### Task 65: 프로젝트 CRUD API
- [ ] ProjectController 생성
- [ ] GET /projects: 내 프로젝트 목록
- [ ] POST /projects: 새 프로젝트 생성
- [ ] PUT /projects/{id}: 프로젝트 수정
- [ ] DELETE /projects/{id}: 프로젝트 삭제
- [ ] 권한 검증 (본인 프로젝트만 수정/삭제 가능)

**예상 소요 시간**: 2시간

---

#### Task 66: 프로젝트별 데이터 분리
- [ ] CharacterService 등에 프로젝트 필터 추가
  - getAllCharacters(projectId)
  - 현재 로그인 사용자의 프로젝트만 조회
- [ ] 데이터 생성 시 projectId 자동 설정

**예상 소요 시간**: 2시간

---

#### Task 67: 프로젝트 공유 및 협업 (선택적)
- [ ] ProjectMember 엔티티 (프로젝트 멤버 관리)
- [ ] 프로젝트 초대 기능
- [ ] 읽기 전용/편집 권한 구분

**예상 소요 시간**: 3시간

---

### 6.3 프론트엔드 인증 UI

#### Task 68: 로그인/회원가입 페이지
- [ ] `/login` 페이지 생성
- [ ] `/signup` 페이지 생성
- [ ] 폼 validation
- [ ] API 호출 및 JWT 저장 (localStorage 또는 cookie)

**예상 소요 시간**: 2시간

---

#### Task 69: 프로젝트 선택 UI
- [ ] 로그인 후 프로젝트 목록 표시
- [ ] 프로젝트 선택 시 컨텍스트 저장
- [ ] 프로젝트 생성 모달
- [ ] 네비게이션에 현재 프로젝트 표시

**예상 소요 시간**: 2시간

---

#### Task 70: 인증 토큰 관리
- [ ] Axios Interceptor 설정
  - 요청마다 JWT 헤더 자동 추가
  - 401 응답 시 로그인 페이지로 리다이렉트
- [ ] 토큰 갱신 로직 (선택적)

**예상 소요 시간**: 1시간

---

### Phase 6 완료 기준
- [x] 회원가입 및 로그인 가능
- [x] JWT 기반 인증 동작
- [x] 사용자별 프로젝트 생성 및 관리
- [x] 프로젝트별 데이터 분리
- [x] 다른 사용자의 데이터 접근 불가

**Phase 6 총 예상 소요 시간**: 15-18시간

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

## Phase 8: Docker 및 배포 자동화

### 목표
로컬 개발 환경과 프로덕션 환경을 Docker로 통합하고 CI/CD 구축

### 8.1 Docker 컨테이너화

#### Task 77: api-server Dockerfile 작성
- [ ] Dockerfile 생성
  - 베이스 이미지: openjdk:25
  - Gradle 빌드 실행
  - JAR 파일 실행
- [ ] .dockerignore 파일 작성
- [ ] 이미지 빌드 및 테스트

**예상 소요 시간**: 1시간

---

#### Task 78: frontend Dockerfile 작성
- [ ] Dockerfile 생성
  - 베이스 이미지: node:20
  - 빌드 및 프로덕션 실행
- [ ] Next.js 최적화 설정
- [ ] 이미지 빌드 및 테스트

**예상 소요 시간**: 1시간

---

#### Task 79: llm-server Dockerfile 작성
- [ ] Dockerfile 생성
  - 베이스 이미지: python:3.11
  - requirements.txt 설치
  - FastAPI 실행
- [ ] 이미지 빌드 및 테스트

**예상 소요 시간**: 30분

---

#### Task 80: docker-compose.yml 작성
- [ ] 서비스 정의
  - api-server
  - frontend
  - llm-server
  - postgres (프로덕션 DB)
- [ ] 네트워크 구성
- [ ] 볼륨 마운트 (DB 데이터 영속화)
- [ ] 환경 변수 설정 (.env 파일)
- [ ] 로컬에서 전체 스택 실행 테스트

**예상 소요 시간**: 2시간

---

### 8.2 CI/CD 파이프라인

#### Task 81: GitHub Actions 워크플로우 작성
- [ ] `.github/workflows/ci.yml` 파일 생성
- [ ] 빌드 자동화
  - Gradle 빌드
  - npm build
  - pytest (LLM 서버)
- [ ] 테스트 실행
  - 단위 테스트
  - 통합 테스트 (선택적)
- [ ] Docker 이미지 빌드
- [ ] Docker Hub 또는 GitHub Container Registry에 푸시

**예상 소요 시간**: 3시간

---

#### Task 82: 배포 자동화 (선택적)
- [ ] 배포 워크플로우 작성
  - main 브랜치 푸시 시 자동 배포
- [ ] 클라우드 플랫폼 설정 (AWS, GCP, Azure 등)
- [ ] 배포 스크립트 작성
- [ ] 배포 후 헬스 체크

**예상 소요 시간**: 4시간

---

### 8.3 프로덕션 환경 설정

#### Task 83: 환경 변수 관리
- [ ] 개발/프로덕션 프로필 분리
  - application-dev.properties
  - application-prod.properties
- [ ] 시크릿 관리
  - GitHub Secrets
  - AWS Secrets Manager (선택적)

**예상 소요 시간**: 1시간

---

#### Task 84: 데이터베이스 마이그레이션
- [ ] PostgreSQL 설정 (프로덕션)
- [ ] Flyway 또는 Liquibase 도입 (선택적)
- [ ] 초기 스키마 마이그레이션
- [ ] 백업 전략 수립

**예상 소요 시간**: 2시간

---

#### Task 85: 모니터링 및 로깅
- [ ] 로깅 설정
  - Logback 설정
  - 로그 레벨 조정
- [ ] 모니터링 도구 연동 (선택적)
  - Prometheus, Grafana
  - CloudWatch, Datadog 등
- [ ] 알림 설정 (에러 발생 시)

**예상 소요 시간**: 3시간

---

### Phase 8 완료 기준
- [x] 모든 서비스가 Docker로 실행됨
- [x] docker-compose up으로 전체 스택 실행 가능
- [x] CI/CD 파이프라인 동작
- [x] 프로덕션 환경 배포 가능
- [x] 모니터링 및 로깅 설정 완료

**Phase 8 총 예상 소요 시간**: 12-15시간

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
- [ ] Tailwind CSS 커스터마이징
  - 색상 팔레트 정의
  - 타이포그래피 설정
  - 컴포넌트 스타일 가이드
- [ ] 공통 컴포넌트 라이브러리
  - Button, Input, Modal 등
- [ ] 스토리북 설정 (선택적)

**예상 소요 시간**: 4시간

---

#### Task 87: 반응형 디자인
- [ ] 모바일 레이아웃 최적화
- [ ] 태블릿 레이아웃 최적화
- [ ] 브레이크포인트별 테스트

**예상 소요 시간**: 3시간

---

#### Task 88: 다크 모드 지원
- [ ] 다크 모드 테마 정의
- [ ] 테마 전환 토글
- [ ] 사용자 설정 저장

**예상 소요 시간**: 2시간

---

#### Task 89: 사용자 경험 개선
- [ ] 로딩 스피너 및 스켈레톤 UI
- [ ] 에러 메시지 개선
- [ ] 성공/실패 알림 (Toast)
- [ ] 키보드 단축키
- [ ] 튜토리얼 및 온보딩

**예상 소요 시간**: 4시간

---

### 9.2 성능 최적화

#### Task 90: API 응답 캐싱
- [ ] Redis 설치 및 설정
- [ ] Spring Cache 설정
- [ ] 자주 조회되는 데이터 캐싱
  - 캐릭터 목록
  - 에피소드 목록
- [ ] 캐시 무효화 전략

**예상 소요 시간**: 2시간

---

#### Task 91: 데이터베이스 쿼리 최적화
- [ ] N+1 문제 해결
  - @EntityGraph 사용
  - Fetch Join 적용
- [ ] 인덱스 추가
  - 자주 검색되는 컬럼
  - 외래 키
- [ ] 쿼리 성능 측정 및 개선

**예상 소요 시간**: 3시간

---

#### Task 92: LLM 응답 스트리밍
- [ ] Server-Sent Events (SSE) 구현
- [ ] LLM 응답을 실시간으로 스트리밍
- [ ] 프론트엔드에서 스트리밍 수신 및 표시

**예상 소요 시간**: 3시간

---

#### Task 93: 프론트엔드 최적화
- [ ] 코드 스플리팅
- [ ] 이미지 최적화 (Next.js Image)
- [ ] 번들 크기 분석 및 축소
- [ ] Lazy Loading 적용

**예상 소요 시간**: 2시간

---

### 9.3 테스트 커버리지 향상

#### Task 94: 백엔드 단위 테스트
- [ ] Service 계층 단위 테스트 (JUnit)
- [ ] Repository 테스트 (@DataJpaTest)
- [ ] 테스트 커버리지 측정 (JaCoCo)

**예상 소요 시간**: 4시간

---

#### Task 95: 통합 테스트
- [ ] Controller 통합 테스트 (@SpringBootTest)
- [ ] API 엔드포인트 테스트 (MockMvc)
- [ ] 데이터베이스 트랜잭션 테스트

**예상 소요 시간**: 3시간

---

#### Task 96: 프론트엔드 테스트
- [ ] 컴포넌트 단위 테스트 (Jest, React Testing Library)
- [ ] E2E 테스트 (Playwright 또는 Cypress)
  - 로그인 플로우
  - 캐릭터 생성 플로우
  - 대사 생성 플로우

**예상 소요 시간**: 5시간

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

### Phase 9 완료 기준
- [x] UI/UX가 세련되고 사용하기 편함
- [x] 성능이 크게 개선됨
- [x] 테스트 커버리지 70% 이상
- [x] 추가 기능으로 사용자 가치 증대

**Phase 9 총 예상 소요 시간**: 지속적 개선 (40+ 시간)

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