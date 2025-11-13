"""
스크립트 분석 컨트롤러
"""

from fastapi import APIRouter
from app.services.script_analysis_service import ScriptAnalysisService
from app.models.script_analysis_models import (
    ScriptAnalysisInput,
    ScriptAnalysisResponse
)

router = APIRouter(prefix="/gen", tags=["script-analysis"])


def create_script_analysis_router(script_analysis_service: ScriptAnalysisService) -> APIRouter:
    """스크립트 분석 라우터 생성"""

    @router.post("/analyze-script", response_model=ScriptAnalysisResponse)
    async def analyze_script(inp: ScriptAnalysisInput) -> ScriptAnalysisResponse:
        """
        스크립트 종합 분석
        캐릭터, 대사, 장면, 관계 정보 추출
        """
        return script_analysis_service.analyze_script(inp)

    return router
