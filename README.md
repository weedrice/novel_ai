# Novel AI

## 프로젝트 개요
Novel AI는 소설과 웹툰 등 스토리텔링 콘텐츠의 기획과 제작을 돕기 위해 설계된 생성형 AI 도구입니다. 캐릭터, 배경 설정, 장면 시나리오를 구조화하여 대규모 언어 모델(LLM)이 문맥과 말투를 이해하도록 돕고, 작가가 빠르게 초안을 만들고 다듬을 수 있도록 지원합니다.

## 주요 기능

### 사용자 인증 및 프로젝트 관리
- **사용자 인증**: JWT 기반 회원가입/로그인 시스템으로 안전한 사용자 관리를 제공합니다.
- **프로젝트 관리**: 사용자별로 여러 프로젝트를 생성하고 관리할 수 있습니다.
- **데이터 분리**: 프로젝트별로 캐릭터, 에피소드, 스크립트 등의 데이터를 완전히 분리하여 저장합니다.
- **보안**: 다른 사용자의 프로젝트 데이터에 접근할 수 없도록 강력한 권한 검증을 적용합니다.

### 캐릭터 및 세계관 관리
- **캐릭터 관리**: 등장인물의 이름, 외형, 성격, MBTI 등 상세 프로필을 관리합니다.
- **말투 프로필**: 캐릭터별 존댓말/반말, 어미, 자주 쓰는 어휘, 문체 특징을 정의하여 일관된 대사를 생성합니다.
- **캐릭터 관계**: 등장인물 간의 관계(친구, 적대, 가족 등)를 정의하고 관계도를 시각화합니다.
- **프로젝트별 관리**: 각 프로젝트마다 독립적인 캐릭터 및 관계 데이터를 유지합니다.

### 시나리오 작성 및 편집
- **에피소드/장면 구조화**: 에피소드별로 장면(Scene)을 추가하고 계층적으로 관리합니다.
- **기존 대사 입력**: 장면별로 기존 대사를 캐릭터, 대사 내용, 말투로 입력/편집/삭제합니다.
- **LLM 기반 시나리오 생성**: 장면 설정과 캐릭터 정보를 바탕으로 LLM이 새로운 대사를 자동 생성합니다.
- **대사 편집 기능**: 생성된 대사와 기존 대사를 인라인으로 수정하거나 삭제할 수 있습니다.
- **시나리오 버전 관리**: 장면별로 시나리오를 버전으로 저장하고, 이전 버전을 불러올 수 있습니다.

### 멀티 LLM 프로바이더 지원
- **다양한 LLM 선택**: OpenAI GPT, Anthropic Claude, Google Gemini 중 선택하여 사용할 수 있습니다.
- **프로바이더별 최적화**: 각 LLM의 특성에 맞는 프롬프트 형식과 파라미터를 자동으로 적용합니다.
- **유연한 전환**: UI에서 간편하게 LLM 프로바이더를 전환할 수 있습니다.

### 스크립트 분석 도구
- **다양한 형식 지원**: 소설, 시나리오, 묘사, 대화 등 다양한 텍스트 형식을 자동으로 분석합니다.
- **자동 캐릭터 추출**: 텍스트에서 캐릭터 이름, 성격, 말투, 대사 예시를 자동으로 추출합니다.
- **장면 정보 파싱**: 장면 위치, 분위기, 참여 캐릭터를 자동으로 인식합니다.
- **대사 및 관계 분석**: 캐릭터별 대사를 추출하고 캐릭터 간 관계를 자동으로 분석합니다.
- **LLM 기반 지능형 분석**: 정규식이 아닌 LLM을 활용하여 자연어 이해 기반의 유연한 파싱을 제공합니다.

### 내보내기 및 통합
- **JSON 내보내기**: 작성한 시나리오를 JSON 형식으로 내보낼 수 있습니다.
- **마이크로서비스 아키텍처**: 프론트엔드, API 서버, LLM 서버를 분리하여 확장성과 배포 유연성을 확보했습니다.

## 시스템 구성
프로젝트는 서비스별 독립 저장소/디렉터리를 기반으로 동작하는 마이크로서비스 구조를 따릅니다.

| 서비스 | 경로 | 기술 스택 | 주요 역할 |
| --- | --- | --- | --- |
| 프론트엔드 | `frontend/` | Next.js 15, React 18, TypeScript | 캐릭터/에피소드 관리, 장면 편집, 시나리오 생성 요청 UI |
| API 서버 | `api-server/` | Spring Boot 3.4, Java 21, Gradle 8 | 비즈니스 로직, 캐릭터/대사/버전 CRUD, LLM 서버 연동 |
| LLM 서버 | `llm-server/` | FastAPI, Python 3.11 | 멀티 LLM 프로바이더 통합, 프롬프트 생성 및 시나리오 생성 |

각 서비스는 독립적으로 배포할 수 있으며, 내부 통신은 REST API 혹은 gRPC(선택 사항)로 확장 가능합니다.

## 기술 스택
- **프론트엔드**: Next.js 15, React 18, TypeScript, Tailwind CSS v4
- **백엔드**: Spring Boot 3.4 (Java 21), Spring Data JPA/Hibernate, Gradle 8, Spring Security 6
- **LLM 서비스**: FastAPI, Python 3.11, 멀티 프로바이더 (OpenAI GPT, Anthropic Claude, Google Gemini)
- **인프라**: Docker, Docker Compose, (예정) Kubernetes, GitHub Actions 기반 CI/CD
- **데이터베이스**: H2 (개발 환경), PostgreSQL (프로덕션)

## 사전 요구 사항
- Docker 및 Docker Compose (권장)
- Node.js 20 이상과 npm (개별 실행 시)
- Java 21 이상 (개별 실행 시, Gradle toolchain이 자동으로 감지/다운로드)
- Python 3.11 이상 (개별 실행 시)
- LLM API 키 (선택 사항, 시나리오 생성 기능 사용 시):
  - OpenAI API Key (GPT 사용 시)
  - Anthropic API Key (Claude 사용 시)
  - Google API Key (Gemini 사용 시)

## 설치 및 로컬 실행

### 🐳 Docker Compose로 전체 시스템 실행 (권장)
```bash
# 모든 서비스를 한 번에 빌드하고 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up --build -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

실행 후 접속:
- **Frontend**: http://localhost:3000
- **API Server**: http://localhost:8080
- **LLM Server**: http://localhost:8000

### 개별 서비스 로컬 실행

#### 환경 설정 (LLM 서버)
LLM 서버를 사용하려면 환경 변수를 설정해야 합니다. `llm-server/.env` 파일을 생성하고 사용할 LLM 프로바이더의 API 키를 설정하세요:

```bash
# llm-server/.env
OPENAI_API_KEY=your_openai_api_key_here
ANTHROPIC_API_KEY=your_anthropic_api_key_here
GOOGLE_API_KEY=your_google_api_key_here
```

#### 서비스 실행
```bash
# 1. 프론트엔드
cd frontend
npm install
npm run dev
# http://localhost:3000 에서 UI 확인

# 2. API 서버
cd ../api-server
./gradlew bootRun
# http://localhost:8080

# 3. LLM 서버 (.env 파일 설정 후)
cd ../llm-server
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
# http://localhost:8000
```

로컬 개발 시 CORS 설정이 필요합니다. 기본 설정은 API 서버의 `application.properties`에 정의되어 있으며, `http://localhost:3000`에서 프론트엔드 요청을 허용하도록 구성되어 있습니다.

### 동작 확인
1. 브라우저에서 http://localhost:3000 접속
2. "에피소드 불러오기" 버튼 클릭 → API Server 연동 확인
3. "대사 제안 받기" 버튼 클릭 → API Server → LLM Server 연동 확인
4. 각 서비스의 health check:
   - `curl http://localhost:8080/health`
   - `curl http://localhost:8000/health`

## 개발 워크플로
1. 피처별 브랜치를 생성하여 작업합니다.
2. LLM 서버와 API 서버를 동시에 실행한 뒤 통합 기능을 확인합니다.
3. PR 생성 시 유닛 테스트(`./gradlew test`, `pytest`)와 ESLint/Prettier 검사를 통과하도록 합니다.

## 테스트

### 백엔드 테스트
```bash
# API 서버 전체 테스트 실행
cd api-server
./gradlew test

# 커버리지 리포트 생성
./gradlew test jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html

# 특정 테스트만 실행
./gradlew test --tests "*IntegrationTest"
./gradlew test --tests "*ServiceTest"
```

### 프론트엔드 테스트
```bash
cd frontend

# 단위 테스트 (Jest)
npm test

# 커버리지 포함
npm run test:coverage

# E2E 테스트 (Playwright)
npm run test:e2e

# E2E 테스트 UI 모드
npm run test:e2e:ui

# 특정 E2E 테스트만 실행
npm run test:e2e -- responsive.spec.ts
```

### LLM 서버 테스트
```bash
cd llm-server
pytest
```

### 테스트 통계 (2025-10-30 기준)
- **백엔드**: 138개 (단위: 118, 통합: 20) | 커버리지: 67%
- **프론트엔드**: 49개 (컴포넌트: 18, E2E: 31) | 반응형 E2E: 33 시나리오
- **총 테스트**: 187개 ✅

## 현재 구현 상태

### ✅ 완료된 기능 (2025-10-31 기준)
- **Phase 0-6 완료**: 프로젝트 초기 설정, 도메인 모델 구축, 관계 그래프 시각화, LLM 연동, 시나리오 생성, 스크립트 분석, 사용자 인증 및 프로젝트 관리
- **Phase 10 일부 완료**: UI/UX 개선, 인증 고도화, 데모 모드, 테스트 인프라
- **백엔드 (api-server)**:
  - **인증 시스템**:
    - JWT 기반 회원가입/로그인, Spring Security 통합
    - Refresh Token 시스템 (자동 세션 연장)
    - JWT 토큰 만료 처리 및 자동 로그아웃
  - **프로젝트 관리**: 사용자별 프로젝트 생성/조회/수정/삭제, 프로젝트별 데이터 분리
  - 완전한 CRUD API (Character, Episode, Scene, Dialogue, Relationship, Script, Project, User)
  - 시나리오 버전 관리 시스템
  - 스크립트 업로드 및 분석 API
  - 포괄적인 로깅 인프라 (모든 서비스 레이어)
  - H2 인메모리 데이터베이스 (개발 환경)
  - 프로젝트별 데이터 필터링 (모든 Repository 및 Service)
  - **테스트 인프라**:
    - JUnit 기반 Service 계층 단위 테스트 (118개)
    - 백엔드 통합 테스트 (20개 - Auth, Project, Database)
    - 프론트엔드 컴포넌트 테스트 (Jest, 18개)
    - 프론트엔드 E2E 테스트 (Playwright, 31개)
    - 반응형 디자인 E2E 테스트 (33 시나리오)
    - JaCoCo 커버리지 측정 및 리포트 생성 (67%)
- **프론트엔드 (frontend)**:
  - **인증 UI**:
    - 로그인/회원가입 페이지, JWT 토큰 관리, Axios Interceptor
    - Refresh Token 자동 갱신
    - 토큰 만료 시 자동 로그아웃 및 알림
  - **프로젝트 관리 UI**: 프로젝트 선택 드롭다운, 프로젝트 생성 모달, 네비게이션 바
  - **프로젝트 컨텍스트**: 전역 프로젝트 상태 관리, 자동 프로젝트 선택
  - 캐릭터 관리 UI (말투 프로필 편집)
  - 관계 그래프 시각화 (React Flow)
  - 시나리오 편집기 (장면별 대화 생성)
  - 스크립트 분석 UI (소설/시나리오 업로드 및 자동 분석)
  - LLM 프로바이더 선택 (GPT, Claude, Gemini)
  - **UI/UX 개선**:
    - 다크 모드 완성 (Tailwind CSS v4 기반, 테마 전환 토글)
    - 키보드 단축키 (Ctrl+K 검색, ESC 닫기 등)
    - 접근성 향상 (ARIA 레이블, 키보드 네비게이션)
    - 로딩 스피너 및 에러 메시지 개선
    - **반응형 디자인** (모바일, 태블릿, 데스크톱 완전 지원)
      - 모바일 햄버거 메뉴 및 터치 최적화
      - 브레이크포인트별 레이아웃 조정 (sm, md, lg, xl)
      - 반응형 그리드 및 폼 레이아웃
  - **데모 모드**: 비로그인 사용자에게 예시 데이터 및 로그인 유도 UI
- **LLM 서버 (llm-server)**:
  - 멀티 프로바이더 지원 (OpenAI, Anthropic, Google)
  - 캐릭터 맞춤 프롬프트 엔지니어링
  - Few-shot 학습 기반 말투 일관성 유지
  - LLM 기반 스크립트 분석 (캐릭터, 장면, 대사, 관계 추출)
  - Fallback 더미 응답 시스템

### 🔧 최근 수정 사항 (2025-10-31)
- **Gradle 환경 설정 개선**:
  - gradle.properties에서 하드코딩된 Java 경로 제거
  - Gradle toolchain 자동 감지/다운로드 활성화
  - PC 환경에 독립적인 빌드 설정 구축

- **이전 수정 사항 (2025-10-30)**:
- **Phase 6 구현 완료**: 사용자 인증 및 프로젝트 관리 시스템
  - **백엔드 인증**:
    - User 엔티티 및 UserRepository 구현
    - Spring Security 6.x 통합 및 SecurityConfig 설정
    - JWT 토큰 생성/검증 (jjwt 0.12.3)
    - JwtAuthenticationFilter, CustomUserDetailsService 구현
    - POST /auth/signup, POST /auth/login 엔드포인트
  - **프로젝트 관리**:
    - Project 엔티티 및 ProjectRepository 구현
    - ProjectService (getCurrentProject 자동 프로젝트 생성 포함)
    - ProjectController (CRUD API)
    - 모든 엔티티에 Project 연관관계 추가 (Character, Episode, Script)
    - 간접 연관관계 @Query 구현 (Scene, Dialogue, Relationship)
  - **데이터 분리**:
    - 모든 Repository에 프로젝트별 조회 메서드 추가
    - 모든 Service에 프로젝트 필터링 로직 추가
    - 자동 프로젝트 설정 (생성 시)
  - **프론트엔드 인증 UI**:
    - 로그인/회원가입 페이지 (/login, /signup)
    - lib/api.ts (Axios Interceptor - JWT 자동 추가, 401 에러 처리)
    - lib/auth.ts (인증 유틸리티 함수)
  - **프론트엔드 프로젝트 UI**:
    - lib/project.ts (프로젝트 API 함수)
    - ProjectContext (전역 프로젝트 상태 관리)
    - Navbar 컴포넌트 (프로젝트 선택, 사용자 정보)
    - 프로젝트 생성 모달
    - 자동 프로젝트 선택 및 로컬스토리지 저장
- **Phase 5 구현 완료**: LLM 기반 스크립트 분석 도구
  - 다양한 텍스트 형식 지원 (소설, 시나리오, 묘사, 대화)
  - 자동 캐릭터 추출 (이름, 성격, 말투, 대사 예시)
  - 장면 정보 추출 (위치, 분위기, 참여자)
  - 대사 추출 및 화자 매칭
  - 캐릭터 간 관계 분석 (관계 유형, 친밀도)
- **버그 수정**: RelationshipService.java:108 타입 에러 해결 (Integer → Double)
- **인프라 개선 (2025-10-24)**:
  - **CORS 설정 문제 해결**: CorsConfig 및 SecurityConfig 수정으로 CORS 충돌 해결
    - `allowedOriginPatterns` 사용 및 명시적 헤더 지정
    - CorsConfigurationSource Bean 방식으로 통합
  - **전체 서비스 점검 완료**:
    - 모든 Docker 컨테이너 정상 작동 (API, LLM, Frontend, Neo4j, PostgreSQL)
    - PostgreSQL `novel_ai` 데이터베이스 생성 및 초기 데이터 로드 완료
    - JWT 인증 API 정상 작동 확인
    - Health Check 엔드포인트 모두 정상
- **Phase 10 일부 완료 (2025-10-30)**:
  - **UI/UX 개선**:
    - ✅ 다크 모드 완성 (Tailwind CSS v4 기반)
    - ✅ 테마 전환 토글 및 사용자 설정 저장
    - ✅ 키보드 단축키 구현 (Ctrl+K 검색, ESC 닫기 등)
    - ✅ 접근성 향상 (ARIA 레이블, 키보드 네비게이션)
    - ✅ 로딩 스피너 및 에러 메시지 개선
  - **반응형 디자인 (Task 87 완료)**:
    - ✅ 모바일 레이아웃 최적화 (375px~640px)
      - 모바일 햄버거 메뉴 추가
      - 그리드 레이아웃 1열 조정
      - 터치 타겟 크기 44px 이상 확보
    - ✅ 태블릿 레이아웃 최적화 (768px~1024px)
      - 그리드 레이아웃 2~3열 조정
      - 폼 필드 가로 배치
    - ✅ 브레이크포인트별 E2E 테스트 (33 시나리오)
      - Mobile: iPhone SE (375x667)
      - Tablet: iPad Mini (768x1024)
      - Desktop: 1920x1080
  - **인증 시스템 고도화**:
    - ✅ Refresh Token 시스템 구현 (자동 세션 연장)
    - ✅ JWT 토큰 만료 처리 및 자동 로그아웃 개선
    - ✅ HttpOnly 쿠키 기반 Refresh Token 저장
  - **데모 모드 구현**:
    - ✅ 비로그인 사용자에게 예시 데이터 표시
    - ✅ 로그인 유도 메시지 및 UI
    - ✅ 데모 데이터 자동 생성 및 제공
  - **테스트 인프라 (총 187개 테스트)**:
    - ✅ 백엔드 단위 테스트 (118개 - Service, Repository)
    - ✅ 백엔드 통합 테스트 (20개 - Auth, Project, Database)
    - ✅ 프론트엔드 컴포넌트 테스트 (18개 - Jest, React Testing Library)
    - ✅ 프론트엔드 E2E 테스트 (31개 - Playwright)
    - ✅ 반응형 디자인 테스트 (33 시나리오)
    - ✅ JaCoCo 커버리지 측정 (67% 전체, Service: 79%, Security: 95%)

### ⚠️ 알려진 이슈 및 제한사항
1. **데이터베이스 설정**:
   - PostgreSQL 사용 중이나 Docker 컨테이너 재시작 시 데이터 초기화 가능
   - 볼륨 마운트로 데이터 영속성 확보 필요
2. **Neo4j 관계 데이터**:
   - Neo4j 컨테이너는 실행 중이나 관계 데이터 아직 미로드
   - Phase 9에서 관계형 DB의 Relationship을 Neo4j로 마이그레이션 예정

### 📋 다음 단계 (우선순위 순)
1. **Phase 10 (대부분 완료)**: 고급 기능 및 최적화
   - ✅ 디자인 시스템 구축 (Task 86)
   - ✅ 반응형 디자인 (Task 87)
   - ✅ 다크 모드 완성 (Task 88)
   - ✅ UX 개선 - 로딩, 키보드 단축키, 접근성 (Task 89)
   - ✅ 백엔드 테스트 (Task 94, 95)
   - ✅ 프론트엔드 테스트 (Task 96)
   - ⏳ 성능 최적화 (API 캐싱, DB 쿼리 최적화)
   - ⏳ 추가 기능 (TTS, 이미지 생성 등)
2. **Phase 7**: Vector DB 및 의미 검색 (선택적)
3. **Phase 8**: Docker 및 배포 자동화 (PostgreSQL 마이그레이션, CI/CD)
4. **Phase 9**: Neo4j GraphDB 전환 (선택적)

자세한 개발 로드맵은 [NEXT_TASKS.md](NEXT_TASKS.md)를 참고하세요.

## 배포 및 운영 계획
현재는 개발 단계로, 마이크로서비스별 컨테이너 이미지를 생성하고 GitHub Actions를 활용한 CI/CD 파이프라인을 구성하는 것을 목표로 하고 있습니다. 장기적으로는 Kubernetes 환경에서 서비스 메쉬(예: Istio)와 오토스케일링을 도입하는 방안을 검토 중입니다.

## 라이선스
이 프로젝트는 [LICENSE](LICENSE) 파일에 명시된 내용을 따릅니다.
