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
| API 서버 | `api-server/` | Spring Boot 3.4, Java 25, Gradle 8 | 비즈니스 로직, 캐릭터/대사/버전 CRUD, LLM 서버 연동 |
| LLM 서버 | `llm-server/` | FastAPI, Python 3.11 | 멀티 LLM 프로바이더 통합, 프롬프트 생성 및 시나리오 생성 |

각 서비스는 독립적으로 배포할 수 있으며, 내부 통신은 REST API 혹은 gRPC(선택 사항)로 확장 가능합니다.

## 기술 스택
- **프론트엔드**: Next.js 15, React 18, TypeScript, Tailwind CSS
- **백엔드**: Spring Boot 3.4 (Java 25), Spring Data JPA/Hibernate, Gradle 8
- **LLM 서비스**: FastAPI, Python 3.11, 멀티 프로바이더 (OpenAI GPT, Anthropic Claude, Google Gemini)
- **인프라**: Docker, Docker Compose, (예정) Kubernetes, GitHub Actions 기반 CI/CD
- **데이터베이스**: H2 (개발 환경), PostgreSQL (예정)

## 사전 요구 사항
- Docker 및 Docker Compose (권장)
- Node.js 20 이상과 npm (개별 실행 시)
- Java 25 이상 (개별 실행 시)
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
- `frontend/`: `npm run test`, `npm run lint`
- `api-server/`: `./gradlew test`
- `auth-server/`: `./gradlew test`
- `llm-server/`: `pytest`

## 현재 구현 상태

### ✅ 완료된 기능 (2025-10-23 기준)
- **Phase 0-6 완료**: 프로젝트 초기 설정, 도메인 모델 구축, 관계 그래프 시각화, LLM 연동, 시나리오 생성, 스크립트 분석, 사용자 인증 및 프로젝트 관리
- **백엔드 (api-server)**:
  - **인증 시스템**: JWT 기반 회원가입/로그인, Spring Security 통합
  - **프로젝트 관리**: 사용자별 프로젝트 생성/조회/수정/삭제, 프로젝트별 데이터 분리
  - 완전한 CRUD API (Character, Episode, Scene, Dialogue, Relationship, Script, Project, User)
  - 시나리오 버전 관리 시스템
  - 스크립트 업로드 및 분석 API
  - 포괄적인 로깅 인프라 (모든 서비스 레이어)
  - H2 인메모리 데이터베이스 (개발 환경)
  - 프로젝트별 데이터 필터링 (모든 Repository 및 Service)
- **프론트엔드 (frontend)**:
  - **인증 UI**: 로그인/회원가입 페이지, JWT 토큰 관리, Axios Interceptor
  - **프로젝트 관리 UI**: 프로젝트 선택 드롭다운, 프로젝트 생성 모달, 네비게이션 바
  - **프로젝트 컨텍스트**: 전역 프로젝트 상태 관리, 자동 프로젝트 선택
  - 캐릭터 관리 UI (말투 프로필 편집)
  - 관계 그래프 시각화 (React Flow)
  - 시나리오 편집기 (장면별 대화 생성)
  - 스크립트 분석 UI (소설/시나리오 업로드 및 자동 분석)
  - LLM 프로바이더 선택 (GPT, Claude, Gemini)
- **LLM 서버 (llm-server)**:
  - 멀티 프로바이더 지원 (OpenAI, Anthropic, Google)
  - 캐릭터 맞춤 프롬프트 엔지니어링
  - Few-shot 학습 기반 말투 일관성 유지
  - LLM 기반 스크립트 분석 (캐릭터, 장면, 대사, 관계 추출)
  - Fallback 더미 응답 시스템

### 🔧 최근 수정 사항 (2025-10-23)
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

### ⚠️ 알려진 이슈 및 제한사항
1. **데이터 영속성**:
   - H2 메모리 DB 사용으로 서버 재시작 시 데이터 초기화됨
   - `ddl-auto=create-drop` 설정으로 서버 종료 시 모든 테이블 삭제
   - **해결 방안**: Phase 8에서 PostgreSQL로 마이그레이션 예정
2. **데이터베이스 설정**:
   - docker-compose.yml에 PostgreSQL이 주석 처리되어 있음
   - 프로덕션 환경 DB 연동 필요

### 📋 다음 단계 (우선순위 순)
1. **Phase 7**: Vector DB 및 의미 검색 (선택적)
2. **Phase 8**: Docker 및 배포 자동화 (PostgreSQL 마이그레이션, CI/CD)
3. **Phase 9**: Neo4j GraphDB 전환 (선택적)
4. **Phase 10**: 고급 기능 및 최적화 (지속적 개선)

자세한 개발 로드맵은 [NEXT_TASKS.md](NEXT_TASKS.md)를 참고하세요.

## 배포 및 운영 계획
현재는 개발 단계로, 마이크로서비스별 컨테이너 이미지를 생성하고 GitHub Actions를 활용한 CI/CD 파이프라인을 구성하는 것을 목표로 하고 있습니다. 장기적으로는 Kubernetes 환경에서 서비스 메쉬(예: Istio)와 오토스케일링을 도입하는 방안을 검토 중입니다.

## 라이선스
이 프로젝트는 [LICENSE](LICENSE) 파일에 명시된 내용을 따릅니다.
