"""
에피소드 분석 컨트롤러
"""

from fastapi import APIRouter, Request
from slowapi import Limiter
from app.services.episode_analysis_service import EpisodeAnalysisService
from app.models.episode_analysis_models import (
    EpisodeAnalysisInput,
    SummaryResponse,
    CharacterAnalysisResponse,
    SceneAnalysisResponse,
    DialogueAnalysisResponse,
    SpellCheckResponse
)

router = APIRouter(prefix="/gen/episode", tags=["episode-analysis"])


def create_episode_analysis_router(episode_analysis_service: EpisodeAnalysisService, limiter: Limiter = None) -> APIRouter:
    """에피소드 분석 라우터 생성"""

    # Rate limiting: 분당 15회 요청 제한
    rate_limit = "15/minute" if limiter else None

    @router.post("/summary", response_model=SummaryResponse)
    async def generate_summary(request: Request, inp: EpisodeAnalysisInput) -> SummaryResponse:
        """AI 요약 생성"""
        if limiter:
            await limiter.check_request(request, rate_limit)
        return episode_analysis_service.generate_summary(inp)

    @router.post("/characters", response_model=CharacterAnalysisResponse)
    async def analyze_characters(request: Request, inp: EpisodeAnalysisInput) -> CharacterAnalysisResponse:
        """캐릭터 분석"""
        if limiter:
            await limiter.check_request(request, rate_limit)
        return episode_analysis_service.analyze_characters(inp)

    @router.post("/scenes", response_model=SceneAnalysisResponse)
    async def analyze_scenes(request: Request, inp: EpisodeAnalysisInput) -> SceneAnalysisResponse:
        """장면 분석"""
        if limiter:
            await limiter.check_request(request, rate_limit)
        return episode_analysis_service.analyze_scenes(inp)

    @router.post("/dialogues", response_model=DialogueAnalysisResponse)
    async def analyze_dialogues(request: Request, inp: EpisodeAnalysisInput) -> DialogueAnalysisResponse:
        """대사 분석"""
        if limiter:
            await limiter.check_request(request, rate_limit)
        return episode_analysis_service.analyze_dialogues(inp)

    @router.post("/spell-check", response_model=SpellCheckResponse)
    async def spell_check(request: Request, inp: EpisodeAnalysisInput) -> SpellCheckResponse:
        """맞춤법 검사"""
        if limiter:
            await limiter.check_request(request, rate_limit)
        return episode_analysis_service.spell_check(inp)

    return router
