# LLM Server

AI 기반 소설/대본 작성을 위한 LLM 서버입니다. 캐릭터 대사 생성, 시나리오 분석, 에피소드 요약 등의 기능을 제공합니다.

## 📋 주요 기능

- **대사 생성 (Dialogue Generation)**: 캐릭터 페르소나 기반 대사 제안
- **시나리오 생성 (Scenario Generation)**: 다중 캐릭터 대화 시나리오 생성
- **대본 분석 (Script Analysis)**: 캐릭터, 대사, 장면, 관계 추출
- **에피소드 분석 (Episode Analysis)**: 요약, 캐릭터 분석, 장면 분석, 맞춤법 검사
- **실시간 스트리밍 (SSE Streaming)**: Server-Sent Events 기반 실시간 대사 생성
- **멀티 LLM 프로바이더**: OpenAI GPT, Anthropic Claude, Google Gemini 지원

## 🛠 기술 스택

- **Framework**: FastAPI 0.115.0+
- **Language**: Python 3.11+
- **LLM Providers**:
  - OpenAI (GPT-3.5-turbo)
  - Anthropic Claude (claude-3-haiku-20240307)
  - Google Gemini (gemini-pro)
- **Architecture**: Controller-Service Pattern
- **Test Coverage**: 89% (109 tests)
- **Type Safety**: Pydantic 2.10.0+

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# 가상 환경 생성 및 활성화
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 환경 변수 설정
cp .env.example .env
# .env 파일을 열어 API 키 입력
```

### 2. 환경 변수 설정

`.env` 파일에 다음 환경 변수를 설정하세요:

```bash
# LLM Provider 선택 (openai, claude, gemini 중 택 1)
DEFAULT_LLM_PROVIDER=openai

# OpenAI 설정
OPENAI_API_KEY=your-openai-api-key-here
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_TEMPERATURE=0.8
OPENAI_MAX_TOKENS=150

# Anthropic Claude 설정
ANTHROPIC_API_KEY=your-anthropic-api-key-here
ANTHROPIC_MODEL=claude-3-haiku-20240307
ANTHROPIC_TEMPERATURE=0.8
ANTHROPIC_MAX_TOKENS=150

# Google Gemini 설정
GOOGLE_API_KEY=your-google-api-key-here
GEMINI_MODEL=gemini-pro
GEMINI_TEMPERATURE=0.8
GEMINI_MAX_TOKENS=150
```

### 3. 서버 실행

```bash
# 개발 모드 (자동 재시작)
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 프로덕션 모드
python app/main.py
```

서버가 실행되면 다음 주소에서 접근 가능합니다:
- API 서버: http://localhost:8000
- API 문서 (Swagger UI): http://localhost:8000/docs
- API 문서 (ReDoc): http://localhost:8000/redoc

## 📡 API 엔드포인트

### 시스템 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | 서버 정보 및 버전 |
| GET | `/health` | 헬스 체크 |
| GET | `/providers` | 사용 가능한 LLM 프로바이더 목록 |

### 대사 생성 엔드포인트

#### POST `/gen/suggest`
캐릭터 페르소나 기반 대사 제안

**Request Body:**
```json
{
  "character_name": "김민수",
  "personality": "밝고 긍정적인 성격",
  "speaking_style": "친근하고 격식 없는 말투",
  "context": "친구와 카페에서 커피를 마시며 대화",
  "honorific": "해요체"
}
```

**Response:**
```json
{
  "suggestions": [
    {
      "dialogue": "오늘 날씨 정말 좋죠?",
      "reason": "밝은 성격과 긍정적인 어조가 드러남",
      "score": 0.95
    }
  ]
}
```

#### POST `/gen/suggest-stream`
SSE 기반 실시간 대사 생성 (동일한 Request Body)

### 시나리오 생성 엔드포인트

#### POST `/gen/scenario`
다중 캐릭터 대화 시나리오 생성

**Request Body:**
```json
{
  "scenario_description": "커피숍에서 우연히 만난 옛 친구들의 재회",
  "characters": [
    {
      "name": "김민수",
      "personality": "밝고 긍정적",
      "speaking_style": "친근한 말투"
    }
  ],
  "tone": "따뜻하고 감동적인",
  "num_dialogues": 5
}
```

### 대본 분석 엔드포인트

#### POST `/gen/analyze-script`
대본에서 캐릭터, 대사, 장면, 관계 추출

**Request Body:**
```json
{
  "script_text": "김민수: 오랜만이야!\n이영희: 정말 오랜만이네!"
}
```

**Response:**
```json
{
  "characters": [...],
  "dialogues": [...],
  "scenes": [...],
  "relationships": [...]
}
```

### 에피소드 분석 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/gen/episode/summary` | 에피소드 요약 생성 |
| POST | `/gen/episode/characters` | 에피소드 내 캐릭터 분석 |
| POST | `/gen/episode/scenes` | 장면 분석 |
| POST | `/gen/episode/dialogues` | 대사 분석 |
| POST | `/gen/episode/spell-check` | 맞춤법 검사 |

**Request Body (공통):**
```json
{
  "title": "에피소드 제목",
  "content": "에피소드 내용..."
}
```

## 🏗 아키텍처

프로젝트는 **Controller-Service 패턴**을 따릅니다:

```
llm-server/
├── app/
│   ├── main.py                    # FastAPI 앱 진입점, 의존성 주입
│   ├── core/
│   │   └── llm_provider_manager.py  # LLM 프로바이더 관리
│   ├── controllers/               # HTTP 요청 처리, 라우팅
│   │   ├── system_controller.py
│   │   ├── dialogue_controller.py
│   │   ├── scenario_controller.py
│   │   ├── script_analysis_controller.py
│   │   ├── episode_analysis_controller.py
│   │   └── streaming_controller.py
│   ├── services/                  # 비즈니스 로직, LLM 호출
│   │   ├── dialogue_service.py
│   │   ├── scenario_service.py
│   │   ├── script_analysis_service.py
│   │   └── episode_analysis_service.py
│   ├── models/                    # Pydantic 요청/응답 모델
│   │   └── ...
│   └── utils/                     # 유틸리티 (JSON 파싱, 프롬프트 템플릿)
│       ├── json_parser.py
│       ├── prompt_builder.py
│       └── prompt_templates.py
├── tests/                         # 테스트 (89% 커버리지)
├── Dockerfile                     # Docker 이미지 빌드
├── requirements.txt               # Python 의존성
└── .env.example                   # 환경 변수 예시
```

### 주요 설계 원칙

1. **관심사의 분리 (Separation of Concerns)**
   - Controller: HTTP 요청/응답 처리
   - Service: 비즈니스 로직 및 LLM 호출
   - Model: 데이터 검증 및 타입 안전성

2. **의존성 주입 (Dependency Injection)**
   - `main.py`에서 서비스 인스턴스 생성 및 컨트롤러에 주입
   - 테스트 용이성 및 유연성 향상

3. **에러 처리 및 Fallback**
   - 모든 서비스 메서드에서 예외 처리
   - LLM 호출 실패 시 기본 응답 반환

4. **멀티 프로바이더 지원**
   - `LLMProviderManager`를 통한 통합 인터페이스
   - 환경 변수로 프로바이더 선택

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
pytest

# 커버리지 포함 테스트
pytest --cov=app --cov-report=html

# 특정 테스트 파일 실행
pytest tests/test_dialogue_service.py
```

### 테스트 커버리지

현재 테스트 커버리지: **89%** (109개 테스트 통과)

| 모듈 | 커버리지 |
|------|---------|
| Controllers | 79-100% |
| Services | 66-94% |
| Models | 100% |
| Utils | 84-100% |
| Core | 79% |

상세 커버리지 리포트: [coverage_report.md](./coverage_report.md)

## 🐳 Docker 배포

### 이미지 빌드 및 실행

```bash
# Docker 이미지 빌드
docker build -t llm-server:latest .

# 컨테이너 실행
docker run -d \
  -p 8000:8000 \
  --env-file .env \
  --name llm-server \
  llm-server:latest

# 로그 확인
docker logs -f llm-server
```

## 🔧 개발 가이드

### 새로운 엔드포인트 추가

1. **Pydantic 모델 정의** (`app/models/`)
   ```python
   class MyRequest(BaseModel):
       field: str
   ```

2. **서비스 로직 구현** (`app/services/`)
   ```python
   class MyService:
       def __init__(self, llm_manager: LLMProviderManager):
           self.llm_manager = llm_manager

       def process(self, request: MyRequest) -> MyResponse:
           # 비즈니스 로직
           pass
   ```

3. **컨트롤러 생성** (`app/controllers/`)
   ```python
   def create_my_router(service: MyService) -> APIRouter:
       router = APIRouter()

       @router.post("/my-endpoint")
       def my_endpoint(request: MyRequest):
           return service.process(request)

       return router
   ```

4. **라우터 등록** (`app/main.py`)
   ```python
   my_service = MyService(llm_manager)
   my_router = create_my_router(my_service)
   app.include_router(my_router)
   ```

### 프롬프트 템플릿 관리

프롬프트 템플릿은 `app/utils/prompt_templates.py`에서 관리합니다:

```python
DIALOGUE_SUGGESTION_TEMPLATE = """
당신은 소설 작가의 어시스턴트입니다.
캐릭터: {character_name}
성격: {personality}
...
"""
```

### 에러 핸들링

모든 서비스 메서드는 try-except로 에러를 처리하고 fallback을 제공합니다:

```python
try:
    # LLM 호출
    result = llm_manager.generate(...)
except Exception as e:
    logger.error(f"Error: {e}")
    # Fallback 응답 반환
    return default_response
```

## 📊 로깅

로깅 레벨: `INFO`

로그 형식:
```
2024-01-15 10:30:45 - app.services.dialogue_service - INFO - Generating dialogues...
```

주요 로그 이벤트:
- LLM 프로바이더 초기화
- API 호출 시작/완료
- 에러 발생 시 상세 정보

## 🤝 기여 가이드

1. 브랜치 생성: `git checkout -b feature/your-feature`
2. 코드 작성 및 테스트 추가
3. 테스트 실행: `pytest --cov=app`
4. 커밋: `git commit -m "feat: add your feature"`
5. Push: `git push origin feature/your-feature`
6. Pull Request 생성

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다.

## 🔗 관련 문서

- [FastAPI 공식 문서](https://fastapi.tiangolo.com/)
- [OpenAI API 문서](https://platform.openai.com/docs)
- [Anthropic Claude API 문서](https://docs.anthropic.com/)
- [Google Gemini API 문서](https://ai.google.dev/)
