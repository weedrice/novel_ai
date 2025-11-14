"""
Request ID 미들웨어
모든 요청에 고유 ID 부여하여 추적 가능하게 함
"""

import uuid
import logging
from starlette.middleware.base import BaseHTTPMiddleware
from fastapi import Request

logger = logging.getLogger(__name__)


class RequestIDMiddleware(BaseHTTPMiddleware):
    """
    요청 추적 ID 미들웨어

    각 요청에 고유한 UUID를 부여하고:
    - request.state.request_id에 저장
    - 응답 헤더 X-Request-ID에 포함
    - 로그에 자동 포함 (LoggerAdapter 사용 시)
    """

    async def dispatch(self, request: Request, call_next):
        """요청 처리 및 Request ID 부여"""

        # Request ID 생성 (클라이언트가 제공한 것 우선 사용)
        request_id = request.headers.get("X-Request-ID")
        if not request_id:
            request_id = str(uuid.uuid4())

        # Request state에 저장
        request.state.request_id = request_id

        # 로그에 Request ID 포함
        logger.info(
            f"Request started",
            extra={
                "request_id": request_id,
                "method": request.method,
                "path": request.url.path,
                "client": request.client.host if request.client else "unknown",
            },
        )

        # 요청 처리
        try:
            response = await call_next(request)

            # 응답 헤더에 Request ID 추가
            response.headers["X-Request-ID"] = request_id

            # 요청 완료 로그
            logger.info(
                f"Request completed",
                extra={
                    "request_id": request_id,
                    "status_code": response.status_code,
                },
            )

            return response

        except Exception as e:
            # 에러 로그
            logger.error(
                f"Request failed: {str(e)}",
                extra={"request_id": request_id},
                exc_info=True,
            )
            raise


class RequestLoggerAdapter(logging.LoggerAdapter):
    """
    Request ID를 자동으로 로그에 포함하는 Adapter

    사용 예:
        logger = RequestLoggerAdapter(logging.getLogger(__name__), {"request": request})
        logger.info("Processing request")  # request_id 자동 포함
    """

    def process(self, msg, kwargs):
        """로그 메시지에 Request ID 추가"""
        request = self.extra.get("request")
        if request and hasattr(request.state, "request_id"):
            kwargs["extra"] = kwargs.get("extra", {})
            kwargs["extra"]["request_id"] = request.state.request_id
        return msg, kwargs
