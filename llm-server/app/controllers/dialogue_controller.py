"""
대사 생성 컨트롤러
"""

from fastapi import APIRouter, Request
import logging
from slowapi import Limiter
from app.services.dialogue_service import DialogueService
from app.models.dialogue_models import SuggestInput, SuggestResponse

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/gen", tags=["dialogue"])


def create_dialogue_router(dialogue_service: DialogueService, limiter: Limiter = None) -> APIRouter:
    """대사 생성 라우터 생성"""

    # Rate limiting: 분당 20회 요청 제한
    rate_limit = "20/minute" if limiter else None

    @router.post("/suggest", response_model=SuggestResponse)
    async def gen_suggest(request: Request, inp: SuggestInput = None) -> SuggestResponse:
        """대사 제안 생성"""
        if limiter:
            await limiter.check_request(request, rate_limit)
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
