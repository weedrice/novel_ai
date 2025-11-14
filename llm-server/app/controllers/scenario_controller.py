"""
시나리오 생성 컨트롤러
"""

from fastapi import APIRouter, Request
from slowapi import Limiter
from app.services.scenario_service import ScenarioService
from app.models.scenario_models import ScenarioInput, ScenarioResponse

router = APIRouter(prefix="/gen", tags=["scenario"])


def create_scenario_router(scenario_service: ScenarioService, limiter: Limiter = None) -> APIRouter:
    """시나리오 생성 라우터 생성"""

    # Rate limiting: 분당 10회 요청 제한 (시나리오 생성은 무거운 작업)
    rate_limit = "10/minute" if limiter else None

    @router.post("/scenario", response_model=ScenarioResponse)
    async def gen_scenario(request: Request, inp: ScenarioInput) -> ScenarioResponse:
        """다중 캐릭터 대화 시나리오 생성"""
        if limiter:
            await limiter.check_request(request, rate_limit)
        return scenario_service.generate_scenario(inp)

    return router
