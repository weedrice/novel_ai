# Novel AI

## 프로젝트 개요
Novel AI는 소설과 웹툰 등 스토리텔링 콘텐츠의 기획과 제작을 돕기 위해 설계된 생성형 AI 도구입니다. 캐릭터, 배경 설정, 장면 시나리오를 구조화하여 대규모 언어 모델(LLM)이 문맥과 말투를 이해하도록 돕고, 작가가 빠르게 초안을 만들고 다듬을 수 있도록 지원합니다.

## 주요 기능

### 캐릭터 및 세계관 관리
- **캐릭터 관리**: 등장인물의 이름, 외형, 성격, MBTI 등 상세 프로필을 관리합니다.
- **말투 프로필**: 캐릭터별 존댓말/반말, 어미, 자주 쓰는 어휘, 문체 특징을 정의하여 일관된 대사를 생성합니다.
- **캐릭터 관계**: 등장인물 간의 관계(친구, 적대, 가족 등)를 정의하고 관계도를 시각화합니다.

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

## 배포 및 운영 계획
현재는 개발 단계로, 마이크로서비스별 컨테이너 이미지를 생성하고 GitHub Actions를 활용한 CI/CD 파이프라인을 구성하는 것을 목표로 하고 있습니다. 장기적으로는 Kubernetes 환경에서 서비스 메쉬(예: Istio)와 오토스케일링을 도입하는 방안을 검토 중입니다.

## 라이선스
이 프로젝트는 [LICENSE](LICENSE) 파일에 명시된 내용을 따릅니다.
