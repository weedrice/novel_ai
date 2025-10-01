# Novel AI

## 프로젝트 개요
Novel AI는 소설과 웹툰 등 스토리텔링 콘텐츠의 기획과 제작을 돕기 위해 설계된 생성형 AI 도구입니다. 캐릭터, 배경 설정, 장면 시나리오를 구조화하여 대규모 언어 모델(LLM)이 문맥과 말투를 이해하도록 돕고, 작가가 빠르게 초안을 만들고 다듬을 수 있도록 지원합니다.

## 주요 기능
- **설정/캐릭터 관리**: 등장인물 간의 관계와 설정을 그래프로 관리하고, 시각화 UI를 통해 쉽게 편집합니다.
- **말투 및 스타일 프로필링**: 캐릭터별 말투, 어휘, 문체를 정의하고 LLM 프롬프트에 반영하여 일관된 대사를 생성합니다.
- **LLM 기반 시나리오 제안**: 장면 혹은 상황을 입력하면 조건에 맞는 시나리오, 대사 초안을 자동으로 만들어 줍니다.
- **스크립트 검수 도구**: 작성된 스크립트를 분석하여 등장인물, 장면, 관계 정보를 추출하고 교정 제안을 제공합니다.
- **마이크로서비스 아키텍처**: 프론트엔드, API 서버, 인증 서버, LLM 서버를 분리하여 확장성과 배포 유연성을 확보했습니다.

## 시스템 구성
프로젝트는 서비스별 독립 저장소/디렉터리를 기반으로 동작하는 마이크로서비스 구조를 따릅니다.

| 서비스 | 경로 | 기술 스택 | 주요 역할 |
| --- | --- | --- | --- |
| 프론트엔드 | `frontend/` | Next.js 15, React 18, TypeScript | 설정 편집, 관계 그래프, 시나리오 요청 UI 제공 |
| API 서버 | `api-server/` | Spring Boot 3.4, Java 21, Gradle 8 | 핵심 비즈니스 로직, 설정/캐릭터 CRUD, LLM 요청 흐름 관리 |
| LLM 서버 | `llm-server/` | FastAPI, Python 3.11 | 대사 톤 제안 API, LLM 프롬프트 생성·실행, 벡터 DB 연동 (예정) |

각 서비스는 독립적으로 배포할 수 있으며, 내부 통신은 REST API 혹은 gRPC(선택 사항)로 확장 가능합니다.

## 기술 스택
- **프론트엔드**: Next.js, React, Zustand, Tailwind CSS
- **백엔드**: Spring Boot (Java 17), Spring Security, JPA/Hibernate, Gradle
- **LLM 서비스**: FastAPI, LangChain, PyTorch 또는 Hugging Face Transformers, Vector DB (예: Pinecone, Qdrant)
- **인프라**: Docker, Docker Compose, (예정) Kubernetes, GitHub Actions 기반 CI/CD
- **데이터베이스**: PostgreSQL (운영), H2 (로컬 테스트)

## 사전 요구 사항
- Docker 및 Docker Compose (권장)
- Node.js 20 이상과 npm (개별 실행 시)
- Java 21 이상 (개별 실행 시)
- Python 3.11 이상 (개별 실행 시)

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

# 3. LLM 서버
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
