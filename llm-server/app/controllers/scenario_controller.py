"""
시나리오 생성 컨트롤러
"""

from fastapi import APIRouter
from app.services.scenario_service import ScenarioService
from app.models.scenario_models import ScenarioInput, ScenarioResponse

router = APIRouter(prefix="/gen", tags=["scenario"])


def create_scenario_router(scenario_service: ScenarioService) -> APIRouter:
    """시나리오 생성 라우터 생성"""

    @router.post("/scenario", response_model=ScenarioResponse)
    async def gen_scenario(inp: ScenarioInput) -> ScenarioResponse:
        """다중 캐릭터 대화 시나리오 생성"""
        return scenario_service.generate_scenario(inp)

    return router
