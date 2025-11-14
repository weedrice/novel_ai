"""
LLM Server Main Application
Controller-Service 구조로 리팩토링된 FastAPI 애플리케이션
"""

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from contextlib import asynccontextmanager
import logging
import os
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

from app.core.llm_provider_manager import LLMProviderManager
from app.services.dialogue_service import DialogueService
from app.services.scenario_service import ScenarioService
from app.services.script_analysis_service import ScriptAnalysisService
from app.services.episode_analysis_service import EpisodeAnalysisService
from app.controllers.system_controller import create_system_router
from app.controllers.dialogue_controller import create_dialogue_router
from app.controllers.scenario_controller import create_scenario_router
from app.controllers.script_analysis_controller import create_script_analysis_router
from app.controllers.episode_analysis_controller import create_episode_analysis_router
from app.controllers.streaming_controller import create_streaming_router
from app.middleware.auth import APIKeyMiddleware

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Rate Limiter 설정
limiter = Limiter(key_func=get_remote_address)


# ============================================================
# Lifespan Context Manager
# ============================================================

@asynccontextmanager
async def lifespan(app: FastAPI):
    """애플리케이션 생명주기 관리"""
    # Startup
    logger.info("=" * 60)
    logger.info("LLM Server Starting...")
    logger.info("=" * 60)

    yield  # 애플리케이션 실행

    # Shutdown
    logger.info("LLM Server Shutting Down...")


# Create FastAPI app with lifespan
app = FastAPI(
    title="Character Tone LLM Server",
    description="AI-powered dialogue tone suggestion and script analysis service",
    version="0.3.0",
    lifespan=lifespan,
)

# Rate Limiter를 FastAPI 앱에 연결
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS middleware
# 환경 변수에서 허용할 origin을 읽어옴 (쉼표로 구분)
allowed_origins_str = os.getenv("ALLOWED_ORIGINS", "http://localhost:3000")
allowed_origins = [origin.strip() for origin in allowed_origins_str.split(",")]

logger.info(f"CORS Allowed Origins: {allowed_origins}")

app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API Key Authentication Middleware
# 환경 변수에서 API_KEY를 읽어옴 (빈 값이면 인증 비활성화)
api_key = os.getenv("API_KEY", "").strip()
if api_key:
    logger.info("API Key authentication enabled")
    app.add_middleware(APIKeyMiddleware, api_key=api_key)
else:
    logger.warning("API Key authentication disabled - set API_KEY environment variable to enable")


# ============================================================
# Dependency Injection - 싱글톤 인스턴스 생성
# ============================================================

logger.info("Initializing LLM Provider Manager...")
llm_manager = LLMProviderManager()
logger.info(f"Available Providers: {llm_manager.get_available_providers()}")
logger.info(f"Default Provider: {llm_manager.default_provider}")

logger.info("Initializing Services...")
dialogue_service = DialogueService(llm_manager)
scenario_service = ScenarioService(llm_manager)
script_analysis_service = ScriptAnalysisService(llm_manager)
episode_analysis_service = EpisodeAnalysisService(llm_manager)

logger.info("Creating Routers...")
system_router = create_system_router(llm_manager)
dialogue_router = create_dialogue_router(dialogue_service, limiter)
scenario_router = create_scenario_router(scenario_service, limiter)
script_analysis_router = create_script_analysis_router(script_analysis_service)
episode_analysis_router = create_episode_analysis_router(episode_analysis_service, limiter)
streaming_router = create_streaming_router(llm_manager, limiter)


# ============================================================
# Register Routers
# ============================================================

app.include_router(system_router)
app.include_router(dialogue_router)
app.include_router(scenario_router)
app.include_router(script_analysis_router)
app.include_router(episode_analysis_router)
app.include_router(streaming_router)


# ============================================================
# Run Server
# ============================================================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
