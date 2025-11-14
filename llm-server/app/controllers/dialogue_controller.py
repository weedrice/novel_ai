"""
대사 생성 컨트롤러
"""

from fastapi import APIRouter, Request
import logging
from app.services.dialogue_service import DialogueService
from app.models.dialogue_models import SuggestInput, SuggestResponse
from app.core.rate_limiter import limiter

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/gen", tags=["dialogue"])


def create_dialogue_router(dialogue_service: DialogueService) -> APIRouter:
    """대사 생성 라우터 생성"""

    @router.post("/suggest", response_model=SuggestResponse)
    @limiter.limit("20/minute")  # 분당 20회 요청 제한
    async def gen_suggest(request: Request, inp: SuggestInput = None) -> SuggestResponse:
        """대사 제안 생성"""
        # Log raw request for debugging
        try:
            body = await request.body()
            logger.info(f"Raw request body: {body.decode('utf-8')[:500]}")
        except Exception as e:
            logger.error(f"Could not read request body: {e}")

        if inp is None:
            logger.error("Input is None!")
            inp = SuggestInput(
                speakerId="unknown",
                targetIds=[],
                intent="greet",
                honorific="banmal"
            )

        return dialogue_service.generate_dialogue_suggestions(inp)

    return router
