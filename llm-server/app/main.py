"""
LLM Server Main Application
Controller-Service 구조로 리팩토링된 FastAPI 애플리케이션
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
import logging

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

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="Character Tone LLM Server",
    description="AI-powered dialogue tone suggestion and script analysis service",
    version="0.3.0",
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 프로덕션에서는 특정 origin으로 제한
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============================================================
# Dependency Injection - 싱글톤 인스턴스 생성
# ============================================================

logger.info("Initializing LLM Provider Manager...")
llm_manager = LLMProviderManager()

logger.info("Initializing Services...")
dialogue_service = DialogueService(llm_manager)
scenario_service = ScenarioService(llm_manager)
script_analysis_service = ScriptAnalysisService(llm_manager)
episode_analysis_service = EpisodeAnalysisService(llm_manager)

logger.info("Creating Routers...")
system_router = create_system_router(llm_manager)
dialogue_router = create_dialogue_router(dialogue_service)
scenario_router = create_scenario_router(scenario_service)
script_analysis_router = create_script_analysis_router(script_analysis_service)
episode_analysis_router = create_episode_analysis_router(episode_analysis_service)
streaming_router = create_streaming_router(llm_manager)


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
# Startup Event
# ============================================================

@app.on_event("startup")
async def startup_event():
    """애플리케이션 시작 시 실행"""
    logger.info("=" * 60)
    logger.info("LLM Server Starting...")
    logger.info(f"Available Providers: {llm_manager.get_available_providers()}")
    logger.info(f"Default Provider: {llm_manager.default_provider}")
    logger.info("=" * 60)


@app.on_event("shutdown")
async def shutdown_event():
    """애플리케이션 종료 시 실행"""
    logger.info("LLM Server Shutting Down...")


# ============================================================
# Run Server
# ============================================================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
