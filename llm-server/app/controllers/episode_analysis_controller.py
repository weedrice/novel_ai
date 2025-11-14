"""
에피소드 분석 컨트롤러
"""

from fastapi import APIRouter
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


def create_episode_analysis_router(episode_analysis_service: EpisodeAnalysisService) -> APIRouter:
    """에피소드 분석 라우터 생성"""

    @router.post("/summary", response_model=SummaryResponse)
    async def generate_summary(inp: EpisodeAnalysisInput) -> SummaryResponse:
        """AI 요약 생성"""
        return episode_analysis_service.generate_summary(inp)

    @router.post("/characters", response_model=CharacterAnalysisResponse)
    async def analyze_characters(inp: EpisodeAnalysisInput) -> CharacterAnalysisResponse:
        """캐릭터 분석"""
        return episode_analysis_service.analyze_characters(inp)

    @router.post("/scenes", response_model=SceneAnalysisResponse)
    async def analyze_scenes(inp: EpisodeAnalysisInput) -> SceneAnalysisResponse:
        """장면 분석"""
        return episode_analysis_service.analyze_scenes(inp)

    @router.post("/dialogues", response_model=DialogueAnalysisResponse)
    async def analyze_dialogues(inp: EpisodeAnalysisInput) -> DialogueAnalysisResponse:
        """대사 분석"""
        return episode_analysis_service.analyze_dialogues(inp)

    @router.post("/spell-check", response_model=SpellCheckResponse)
    async def spell_check(inp: EpisodeAnalysisInput) -> SpellCheckResponse:
        """맞춤법 검사"""
        return episode_analysis_service.spell_check(inp)

    return router
