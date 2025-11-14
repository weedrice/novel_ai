"""
스트리밍 응답 컨트롤러
"""

from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse
from slowapi import Limiter
import logging
import json

from app.core.llm_provider_manager import LLMProviderManager
from app.services.prompt_builder import PromptBuilder
from app.models.dialogue_models import SuggestInput

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/gen", tags=["streaming"])


def create_streaming_router(llm_manager: LLMProviderManager, limiter: Limiter = None) -> APIRouter:
    """스트리밍 라우터 생성"""

    # Rate limiting: 분당 10회 요청 제한 (스트리밍은 리소스 집약적)
    rate_limit = "10/minute" if limiter else None

    @router.post("/suggest-stream")
    async def gen_suggest_stream(request: Request, inp: SuggestInput = None):
        """
        스트리밍 방식으로 대사 제안 생성
        Server-Sent Events (SSE) 형식으로 실시간 응답 전송
        """
        if limiter:
            await limiter.check_request(request, rate_limit)

        logger.info(
            f"Generating streaming dialogue for speaker={inp.speakerId}, "
            f"intent={inp.intent}, provider={inp.provider or llm_manager.default_provider}"
        )

        async def event_generator():
            """SSE 형식의 이벤트 스트림 생성"""
            try:
                if not inp.characterInfo:
                    logger.warning("No character info provided for streaming")
                    yield f"data: {json.dumps({'error': 'Character info required'})}\n\n"
                    return

                character_dict = inp.characterInfo.model_dump()
                target_names = inp.targetNames or inp.targetIds

                system_prompt, user_prompt = PromptBuilder.build_full_prompt(
                    character_info=character_dict,
                    intent=inp.intent,
                    honorific=inp.honorific,
                    target_names=target_names,
                    max_len=inp.maxLen,
                    n_candidates=1,  # 스트리밍은 단일 후보만 생성
                    context=inp.context,
                )

                # 스트리밍 시작 이벤트
                yield f"data: {json.dumps({'type': 'start', 'message': 'Streaming started'})}\n\n"

                # LLM 스트리밍 호출
                async for chunk in llm_manager.generate_stream(
                    system_prompt=system_prompt,
                    user_prompt=user_prompt,
                    provider=inp.provider,
                ):
                    # 각 텍스트 청크를 SSE 형식으로 전송
                    yield f"data: {json.dumps({'type': 'chunk', 'text': chunk})}\n\n"

                # 스트리밍 완료 이벤트
                yield f"data: {json.dumps({'type': 'done', 'message': 'Streaming completed'})}\n\n"

            except Exception as e:
                logger.error(f"Error during streaming: {e}")
                yield f"data: {json.dumps({'type': 'error', 'message': str(e)})}\n\n"

        return StreamingResponse(
            event_generator(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no",  # Nginx 버퍼링 비활성화
            }
        )

    return router
