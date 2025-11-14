"""
API 인증 미들웨어
"""

from fastapi import Request, HTTPException, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
import os
import logging

logger = logging.getLogger(__name__)


class APIKeyMiddleware(BaseHTTPMiddleware):
    """
    API Key 기반 인증 미들웨어
    헤더에 X-API-Key가 있어야 하며, 환경 변수 API_KEY와 일치해야 함
    """

    def __init__(self, app, api_key: str = None):
        super().__init__(app)
        self.api_key = api_key
        self.enabled = bool(api_key and api_key.strip())

        # 인증이 필요 없는 경로 (헬스 체크, 문서 등)
        self.exempt_paths = {
            "/",
            "/health",
            "/docs",
            "/redoc",
            "/openapi.json",
        }

    async def dispatch(self, request: Request, call_next):
        """요청 처리"""

        # 인증이 비활성화된 경우 또는 제외 경로인 경우 통과
        if not self.enabled or request.url.path in self.exempt_paths:
            return await call_next(request)

        # API Key 확인
        api_key = request.headers.get("X-API-Key")

        if not api_key:
            logger.warning(f"Missing API key for {request.url.path}")
            return JSONResponse(
                status_code=status.HTTP_401_UNAUTHORIZED,
                content={
                    "detail": "API key required. Please provide X-API-Key header."
                }
            )

        if api_key != self.api_key:
            logger.warning(f"Invalid API key attempt for {request.url.path}")
            return JSONResponse(
                status_code=status.HTTP_403_FORBIDDEN,
                content={
                    "detail": "Invalid API key"
                }
            )

        # 인증 성공 - 요청 처리
        response = await call_next(request)
        return response
