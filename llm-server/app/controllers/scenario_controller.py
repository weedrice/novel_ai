"""
시나리오 생성 컨트롤러
"""

from fastapi import APIRouter, Request
from app.services.scenario_service import ScenarioService
from app.models.scenario_models import ScenarioInput, ScenarioResponse
from app.core.rate_limiter import limiter

router = APIRouter(prefix="/gen", tags=["scenario"])


def create_scenario_router(scenario_service: ScenarioService) -> APIRouter:
    """시나리오 생성 라우터 생성"""

    @router.post("/scenario", response_model=ScenarioResponse)
    @limiter.limit("10/minute")  # 분당 10회 요청 제한 (무거운 작업)
    async def gen_scenario(request: Request, inp: ScenarioInput) -> ScenarioResponse:
        """다중 캐릭터 대화 시나리오 생성"""
        return scenario_service.generate_scenario(inp)

    return router
