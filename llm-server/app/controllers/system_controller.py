"""
시스템 관련 컨트롤러
헬스체크, 프로바이더 정보 등
"""

from fastapi import APIRouter
from app.core.llm_provider_manager import LLMProviderManager

router = APIRouter()


def create_system_router(llm_manager: LLMProviderManager) -> APIRouter:
    """시스템 라우터 생성"""

    @router.get("/")
    def root():
        """루트 엔드포인트"""
        return {"message": "Character Tone LLM Server is running", "version": "0.3.0"}

    @router.get("/health")
    def health():
        """헬스체크"""
        return {"status": "ok"}

    @router.get("/providers")
    def get_providers():
        """사용 가능한 LLM 프로바이더 목록 조회"""
        available = llm_manager.get_available_providers()
        return {
            "available": available,
            "default": llm_manager.default_provider,
            "providers": {
                "openai": {
                    "name": "OpenAI GPT",
                    "models": ["gpt-3.5-turbo", "gpt-4", "gpt-4-turbo"],
                    "available": "openai" in available,
                },
                "claude": {
                    "name": "Anthropic Claude",
                    "models": [
                        "claude-3-haiku-20240307",
                        "claude-3-sonnet-20240229",
                        "claude-3-opus-20240229",
                    ],
                    "available": "claude" in available,
                },
                "gemini": {
                    "name": "Google Gemini",
                    "models": ["gemini-pro", "gemini-pro-vision"],
                    "available": "gemini" in available,
                },
            },
        }

    return router
