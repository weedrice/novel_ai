"""
LLM Server Main Application
Controller-Service 구조로 리팩토링된 FastAPI 애플리케이션
"""

from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from dotenv import load_dotenv
from slowapi import _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from prometheus_fastapi_instrumentator import Instrumentator

from app.core.config import get_settings
from app.core.logging_config import setup_logging, get_logger
from app.core.llm_provider_manager import LLMProviderManager
from app.core.cache_manager import CacheManager
from app.core.rate_limiter import limiter
from app.core.metrics import init_app_info
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
from app.middleware import RequestIDMiddleware, SecurityHeadersMiddleware

# Load environment variables
load_dotenv()

# Settings
settings = get_settings()

# Configure structured JSON logging
setup_logging(settings.log_level)
logger = get_logger(__name__)

# ============================================================
# Global Instances
# ============================================================

cache_manager: CacheManager = None  # type: ignore
llm_manager: LLMProviderManager = None  # type: ignore


# ============================================================
# Lifespan Context Manager
# ============================================================

@asynccontextmanager
async def lifespan(app: FastAPI):
    """애플리케이션 생명주기 관리"""
    global cache_manager, llm_manager

    # Startup
    logger.info("=" * 60)
    logger.info(f"LLM Server Starting... ({settings.environment})")
    logger.info(f"Version: {settings.app_version}")

    # Initialize Cache Manager
    logger.info("Initializing Cache Manager...")
    cache_manager = CacheManager(
        redis_url=settings.get_redis_url(), enabled=settings.cache_enabled
    )
    app.state.cache_manager = cache_manager

    # Initialize LLM Provider Manager
    logger.info("Initializing LLM Provider Manager...")
    llm_manager = LLMProviderManager()
    app.state.llm_manager = llm_manager

    # Initialize metrics
    if settings.metrics_enabled:
        init_app_info(settings.app_version, settings.environment)
        logger.info("Metrics initialized")

    logger.info(f"Available Providers: {llm_manager.get_available_providers()}")
    logger.info(f"Default Provider: {llm_manager.default_provider}")
    logger.info(f"Cache: {'Enabled' if settings.cache_enabled else 'Disabled'}")
    logger.info("=" * 60)

    yield

    # Shutdown
    logger.info("LLM Server Shutting Down...")
    if cache_manager:
        await cache_manager.close()
    logger.info("Shutdown complete")


# ============================================================
# Create FastAPI App
# ============================================================

app = FastAPI(
    title=settings.app_name,
    description="AI-powered dialogue tone suggestion and script analysis service",
    version=settings.app_version,
    lifespan=lifespan,
)

# ============================================================
# Middleware Configuration
# ============================================================

# 1. Request ID Middleware (가장 먼저 - 모든 요청 추적)
app.add_middleware(RequestIDMiddleware)

# 2. Security Headers
app.add_middleware(
    SecurityHeadersMiddleware,
    include_hsts=(settings.environment == "production"),
    include_csp=True,
)

# 3. GZIP Compression
app.add_middleware(GZipMiddleware, minimum_size=1000)

# 4. CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================
# Rate Limiter & Metrics
# ============================================================

# Rate Limiter
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Prometheus Metrics
if settings.metrics_enabled:
    Instrumentator().instrument(app).expose(app, endpoint="/metrics")


# ============================================================
# Dependency Injection - 싱글톤 인스턴스 생성
# ============================================================

logger.info("Initializing Services...")
llm_manager_init = LLMProviderManager()
dialogue_service = DialogueService(llm_manager_init)
scenario_service = ScenarioService(llm_manager_init)
script_analysis_service = ScriptAnalysisService(llm_manager_init)
episode_analysis_service = EpisodeAnalysisService(llm_manager_init)

logger.info("Creating Routers...")
system_router = create_system_router(llm_manager_init)
dialogue_router = create_dialogue_router(dialogue_service)
scenario_router = create_scenario_router(scenario_service)
script_analysis_router = create_script_analysis_router(script_analysis_service)
episode_analysis_router = create_episode_analysis_router(episode_analysis_service)
streaming_router = create_streaming_router(llm_manager_init)

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

    uvicorn.run(
        app,
        host=settings.host,
        port=settings.port,
        log_level=settings.log_level.lower(),
    )
