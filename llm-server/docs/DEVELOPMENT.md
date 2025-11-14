# LLM Server 개발 가이드

이 문서는 LLM Server 프로젝트에 기여하거나 로컬에서 개발하려는 개발자를 위한 가이드입니다.

## 목차

- [개발 환경 설정](#개발-환경-설정)
- [프로젝트 구조](#프로젝트-구조)
- [개발 워크플로우](#개발-워크플로우)
- [테스트](#테스트)
- [코드 품질 도구](#코드-품질-도구)
- [디버깅 팁](#디버깅-팁)
- [문제 해결](#문제-해결)

---

## 개발 환경 설정

### 사전 요구사항

- **Python 3.11+**
- **Redis** (캐싱용)
- **Docker** (선택사항, 전체 스택 실행용)
- **Git**

### 1. 저장소 클론

```bash
git clone https://github.com/your-org/novel_ai.git
cd novel_ai/llm-server
```

### 2. 가상환경 생성

```bash
# 가상환경 생성
python -m venv venv

# 가상환경 활성화
# Windows
venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

### 3. 의존성 설치

```bash
# 프로덕션 의존성
pip install -r requirements.txt

# 개발 의존성 (권장)
pip install -r requirements-dev.txt
```

### 4. 환경 변수 설정

```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 편집 (API 키 등 설정)
```

**필수 환경 변수:**

```env
# LLM Provider (최소 1개 필요)
OPENAI_API_KEY=sk-...
# ANTHROPIC_API_KEY=sk-...
# GOOGLE_API_KEY=...

# Redis (로컬 개발)
REDIS_HOST=localhost
REDIS_PORT=6379

# 개발 환경
ENVIRONMENT=development
DEBUG=true
LOG_LEVEL=DEBUG
```

### 5. Redis 시작

**Docker 사용 (권장):**

```bash
docker run -d -p 6379:6379 --name redis redis:7-alpine
```

**또는 로컬 Redis 설치**

### 6. 서버 실행

```bash
# 개발 모드 (auto-reload)
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 또는 Python 직접 실행
python -m app.main
```

서버가 실행되면 다음 URL에 접근 가능합니다:
- API: http://localhost:8000
- Swagger UI: http://localhost:8000/docs
- Redoc: http://localhost:8000/redoc
- Prometheus 메트릭: http://localhost:8000/metrics

---

## 프로젝트 구조

```
llm-server/
├── app/
│   ├── controllers/        # API 엔드포인트 (라우터)
│   │   ├── dialogue_controller.py
│   │   ├── scenario_controller.py
│   │   ├── script_analysis_controller.py
│   │   ├── episode_analysis_controller.py
│   │   ├── streaming_controller.py
│   │   └── system_controller.py
│   │
│   ├── services/           # 비즈니스 로직
│   │   ├── dialogue_service.py
│   │   ├── scenario_service.py
│   │   ├── script_analysis_service.py
│   │   └── episode_analysis_service.py
│   │
│   ├── core/               # 핵심 인프라
│   │   ├── config.py               # 환경 설정
│   │   ├── llm_provider_manager.py # LLM 프로바이더 관리
│   │   ├── cache_manager.py        # Redis 캐시
│   │   ├── rate_limiter.py         # Rate Limiting
│   │   ├── logging_config.py       # 구조화된 로깅
│   │   └── metrics.py              # Prometheus 메트릭
│   │
│   ├── models/             # Pydantic 데이터 모델
│   │   ├── dialogue_models.py
│   │   ├── scenario_models.py
│   │   ├── script_analysis_models.py
│   │   └── episode_analysis_models.py
│   │
│   ├── middleware/         # 미들웨어
│   │   ├── auth.py                 # API Key 인증
│   │   ├── request_id.py           # Request ID 추적
│   │   └── security_headers.py     # 보안 헤더
│   │
│   ├── utils/              # 유틸리티
│   │   ├── json_parser.py
│   │   └── prompt_builder.py
│   │
│   └── main.py             # FastAPI 앱 진입점
│
├── tests/                  # 테스트
│   ├── e2e/                # E2E 테스트
│   ├── test_main.py
│   ├── test_services.py
│   ├── test_security.py
│   └── conftest.py
│
├── docs/                   # 문서
│   ├── DEVELOPMENT.md      # 이 파일
│   └── API.md
│
├── requirements.txt        # 프로덕션 의존성
├── requirements-dev.txt    # 개발 의존성
├── pyproject.toml          # 프로젝트 설정
├── Dockerfile
└── README.md
```

### 아키텍처 패턴

**Controller → Service → Provider 구조:**

```
HTTP Request
    ↓
Controller (app/controllers/)
    ↓
Service (app/services/)
    ↓
LLM Provider Manager (app/core/)
    ↓
OpenAI / Claude / Gemini API
```

**책임 분리:**
- **Controller**: HTTP 요청/응답 처리, 검증
- **Service**: 비즈니스 로직, 데이터 변환
- **Provider**: 외부 API 호출, 에러 처리

---

## 개발 워크플로우

### 1. 새 기능 개발

**브랜치 생성:**

```bash
git checkout -b feature/your-feature-name
```

**개발 순서:**

1. **Pydantic 모델 정의** (`app/models/`)
   - 요청/응답 스키마 정의
   - 입력 검증 규칙 추가

2. **서비스 로직 구현** (`app/services/`)
   - 비즈니스 로직 작성
   - LLM 호출 및 결과 처리

3. **컨트롤러 생성** (`app/controllers/`)
   - 라우터 및 엔드포인트 정의
   - Rate Limiting 설정

4. **테스트 작성** (`tests/`)
   - 단위 테스트
   - 통합 테스트
   - E2E 테스트 (필요시)

5. **문서 업데이트**
   - Docstring 작성
   - README 업데이트 (필요시)

**예제: 새 엔드포인트 추가**

```python
# 1. 모델 정의 (app/models/new_feature_models.py)
from pydantic import BaseModel, Field

class NewFeatureInput(BaseModel):
    text: str = Field(..., min_length=1, max_length=1000)

class NewFeatureResponse(BaseModel):
    result: str

# 2. 서비스 구현 (app/services/new_feature_service.py)
class NewFeatureService:
    def __init__(self, llm_manager):
        self.llm_manager = llm_manager

    def process(self, input: NewFeatureInput) -> NewFeatureResponse:
        # 비즈니스 로직
        result = self.llm_manager.generate(...)
        return NewFeatureResponse(result=result)

# 3. 컨트롤러 (app/controllers/new_feature_controller.py)
from fastapi import APIRouter, Request
from app.core.rate_limiter import limiter

router = APIRouter(prefix="/gen", tags=["new-feature"])

def create_new_feature_router(service):
    @router.post("/new-feature")
    @limiter.limit("10/minute")
    async def new_feature(request: Request, inp: NewFeatureInput):
        return service.process(inp)

    return router

# 4. main.py에 등록
from app.controllers.new_feature_controller import create_new_feature_router

# lifespan 함수 내에서:
new_feature_service = NewFeatureService(llm_manager)
new_feature_router = create_new_feature_router(new_feature_service)
app.include_router(new_feature_router)
```

### 2. 코드 커밋 전 체크리스트

```bash
# 1. 포맷팅
black app tests

# 2. 린팅
ruff check app tests

# 3. 타입 체크
mypy app

# 4. 테스트
pytest

# 5. 커버리지 확인
pytest --cov=app --cov-report=term-missing
```

### 3. Pull Request

PR 생성 시 포함할 내용:
- 변경 사항 요약
- 관련 이슈 번호 (#123)
- 테스트 결과 스크린샷
- API 변경 사항 (있는 경우)

---

## 테스트

### 테스트 실행

```bash
# 전체 테스트
pytest

# 특정 파일
pytest tests/test_services.py

# 특정 테스트
pytest tests/test_services.py::TestDialogueService::test_generate

# 커버리지 포함
pytest --cov=app --cov-report=html

# E2E 테스트만
pytest -m e2e

# E2E 제외
pytest -m "not e2e"

# Verbose 모드
pytest -vv

# 실패 시 즉시 중단
pytest -x
```

### 테스트 작성 가이드

**단위 테스트 (Unit Test):**

```python
# tests/test_services.py
import pytest
from app.services.dialogue_service import DialogueService
from unittest.mock import Mock, patch

@pytest.fixture
def dialogue_service():
    mock_llm = Mock()
    return DialogueService(mock_llm)

def test_generate_dialogue_with_character_info(dialogue_service):
    """캐릭터 정보가 있을 때 대사 생성"""
    input_data = SuggestInput(
        speakerId="char1",
        targetIds=["char2"],
        intent="greet",
        honorific="banmal",
        characterInfo=CharacterInfo(name="Alice")
    )

    result = dialogue_service.generate_dialogue_suggestions(input_data)

    assert result is not None
    assert len(result.candidates) > 0
```

**통합 테스트 (Integration Test):**

```python
# tests/test_main.py
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_dialogue_generation_endpoint():
    """대사 생성 엔드포인트 통합 테스트"""
    payload = {
        "speakerId": "char1",
        "targetIds": ["char2"],
        "intent": "greet",
        "honorific": "banmal"
    }

    response = client.post("/gen/suggest", json=payload)

    assert response.status_code == 200
    data = response.json()
    assert "candidates" in data
```

**E2E 테스트:**

```python
# tests/e2e/test_full_workflow.py
@pytest.mark.e2e
def test_complete_workflow():
    """전체 워크플로우 테스트"""
    # 1. Health check
    response = client.get("/health")
    assert response.status_code == 200

    # 2. Generate dialogue
    response = client.post("/gen/suggest", json=...)
    assert response.status_code == 200
```

---

## 코드 품질 도구

### Black (코드 포맷팅)

```bash
# 포맷팅 적용
black app tests

# 체크만 (변경 없이)
black --check app tests

# 특정 파일만
black app/services/dialogue_service.py
```

### Ruff (린팅)

```bash
# 린팅 실행
ruff check app tests

# 자동 수정 가능한 것 수정
ruff check --fix app tests

# 특정 규칙 무시
ruff check --ignore E501 app
```

### MyPy (타입 체크)

```bash
# 타입 체크
mypy app

# 특정 파일만
mypy app/services/

# 엄격 모드
mypy --strict app
```

### isort (Import 정렬)

```bash
# Import 정렬
isort app tests

# 체크만
isort --check-only app tests
```

### 통합 실행 (권장)

```bash
# 한 번에 실행
black app tests && \
ruff check --fix app tests && \
isort app tests && \
mypy app && \
pytest
```

---

## 디버깅 팁

### 1. 로그 레벨 변경

`.env` 파일:

```env
LOG_LEVEL=DEBUG
```

또는 런타임에:

```bash
LOG_LEVEL=DEBUG uvicorn app.main:app --reload
```

### 2. LLM 응답 확인

로그에서 LLM 응답 검색:

```bash
# JSON 로그 파싱
cat logs/app.log | jq 'select(.message | contains("Generated text"))'
```

### 3. 테스트 디버깅

```bash
# 로그 출력 포함
pytest -s tests/test_specific.py

# 실패 시 pdb 진입
pytest --pdb

# 마지막 실패한 테스트만 재실행
pytest --lf
```

### 4. Redis 캐시 확인

```bash
# Redis CLI 접속
redis-cli

# 캐시 키 조회
KEYS llm:*

# 특정 키 값 확인
GET llm:abc123...

# 캐시 전체 삭제 (주의!)
FLUSHDB
```

### 5. Request ID 추적

모든 요청에는 `X-Request-ID` 헤더가 포함됩니다. 로그에서 추적:

```bash
# 특정 Request ID 관련 로그만 필터링
cat logs/app.log | jq 'select(.request_id == "abc-123-def")'
```

---

## 문제 해결

### 문제: 의존성 설치 실패

```bash
# pip 업그레이드
python -m pip install --upgrade pip

# 캐시 삭제 후 재설치
pip cache purge
pip install -r requirements.txt --no-cache-dir
```

### 문제: Redis 연결 실패

```bash
# Redis 실행 확인
docker ps | grep redis

# Redis 로그 확인
docker logs redis

# 연결 테스트
redis-cli ping
# 응답: PONG
```

### 문제: 테스트 실패 (Rate Limiting)

테스트 환경에서는 Rate Limiting이 자동으로 비활성화됩니다 (`TESTING=true`).

만약 실패한다면:

```bash
# conftest.py 확인
cat tests/conftest.py

# TESTING 환경변수가 설정되는지 확인
```

### 문제: LLM API 호출 실패

1. API 키 확인:
   ```bash
   echo $OPENAI_API_KEY
   ```

2. 네트워크 연결 확인:
   ```bash
   curl https://api.openai.com/v1/models
   ```

3. 로그 확인:
   ```bash
   # LLM 에러 검색
   cat logs/app.log | jq 'select(.level == "ERROR" and .name | contains("llm"))'
   ```

### 문제: 포트 이미 사용 중

```bash
# Windows
netstat -ano | findstr :8000
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8000 | xargs kill -9
```

---

## 추가 리소스

- [FastAPI 공식 문서](https://fastapi.tiangolo.com/)
- [Pydantic 문서](https://docs.pydantic.dev/)
- [Pytest 문서](https://docs.pytest.org/)
- [OpenAI API 문서](https://platform.openai.com/docs)

---

## 도움이 필요하신가요?

- **이슈 제보**: [GitHub Issues](https://github.com/your-org/novel_ai/issues)
- **논의**: [GitHub Discussions](https://github.com/your-org/novel_ai/discussions)

Happy coding! 🚀
