"""
에피소드 분석 컨트롤러
"""

from fastapi import APIRouter, Request
from app.services.episode_analysis_service import EpisodeAnalysisService
from app.models.episode_analysis_models import (
    EpisodeAnalysisInput,
    SummaryResponse,
    CharacterAnalysisResponse,
    SceneAnalysisResponse,
    DialogueAnalysisResponse,
    SpellCheckResponse
)
from app.core.rate_limiter import limiter

router = APIRouter(prefix="/gen/episode", tags=["episode-analysis"])


def create_episode_analysis_router(episode_analysis_service: EpisodeAnalysisService) -> APIRouter:
    """에피소드 분석 라우터 생성"""

    @router.post("/summary", response_model=SummaryResponse)
    @limiter.limit("15/minute")  # 분당 15회 요청 제한
    async def generate_summary(request: Request, inp: EpisodeAnalysisInput) -> SummaryResponse:
        """AI 요약 생성"""
        return episode_analysis_service.generate_summary(inp)

    @router.post("/characters", response_model=CharacterAnalysisResponse)
    @limiter.limit("15/minute")
    async def analyze_characters(request: Request, inp: EpisodeAnalysisInput) -> CharacterAnalysisResponse:
        """캐릭터 분석"""
        return episode_analysis_service.analyze_characters(inp)

    @router.post("/scenes", response_model=SceneAnalysisResponse)
    @limiter.limit("15/minute")
    async def analyze_scenes(request: Request, inp: EpisodeAnalysisInput) -> SceneAnalysisResponse:
        """장면 분석"""
        return episode_analysis_service.analyze_scenes(inp)

    @router.post("/dialogues", response_model=DialogueAnalysisResponse)
    @limiter.limit("15/minute")
    async def analyze_dialogues(request: Request, inp: EpisodeAnalysisInput) -> DialogueAnalysisResponse:
        """대사 분석"""
        return episode_analysis_service.analyze_dialogues(inp)

    @router.post("/spell-check", response_model=SpellCheckResponse)
    @limiter.limit("15/minute")
    async def spell_check(request: Request, inp: EpisodeAnalysisInput) -> SpellCheckResponse:
        """맞춤법 검사"""
        return episode_analysis_service.spell_check(inp)

    return router
