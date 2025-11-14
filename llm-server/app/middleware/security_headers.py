"""
보안 헤더 미들웨어
OWASP 권장 보안 헤더 추가
"""

from starlette.middleware.base import BaseHTTPMiddleware
from fastapi import Request
import logging

logger = logging.getLogger(__name__)


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    """
    보안 헤더 자동 추가 미들웨어

    OWASP Top 10 대응을 위한 HTTP 보안 헤더:
    - X-Content-Type-Options: MIME 스니핑 방지
    - X-Frame-Options: 클릭재킹 방지
    - X-XSS-Protection: XSS 공격 방지
    - Strict-Transport-Security: HTTPS 강제
    - Content-Security-Policy: 악의적 스크립트 실행 방지
    - Referrer-Policy: Referrer 정보 제어
    - Permissions-Policy: 브라우저 기능 접근 제어
    """

    def __init__(
        self,
        app,
        include_hsts: bool = True,
        hsts_max_age: int = 31536000,  # 1년
        include_csp: bool = True,
        csp_directives: str = "default-src 'self'",
    ):
        """
        Args:
            app: FastAPI 앱
            include_hsts: HSTS 헤더 포함 여부 (HTTPS 전용)
            hsts_max_age: HSTS 최대 기간 (초)
            include_csp: CSP 헤더 포함 여부
            csp_directives: CSP 정책 지시문
        """
        super().__init__(app)
        self.include_hsts = include_hsts
        self.hsts_max_age = hsts_max_age
        self.include_csp = include_csp
        self.csp_directives = csp_directives

    async def dispatch(self, request: Request, call_next):
        """요청 처리 및 보안 헤더 추가"""

        response = await call_next(request)

        # 1. X-Content-Type-Options: MIME 타입 스니핑 방지
        response.headers["X-Content-Type-Options"] = "nosniff"

        # 2. X-Frame-Options: 클릭재킹(Clickjacking) 방지
        response.headers["X-Frame-Options"] = "DENY"

        # 3. X-XSS-Protection: 브라우저 XSS 필터 활성화
        response.headers["X-XSS-Protection"] = "1; mode=block"

        # 4. Strict-Transport-Security: HTTPS 강제 (프로덕션 환경에서만)
        if self.include_hsts:
            response.headers["Strict-Transport-Security"] = (
                f"max-age={self.hsts_max_age}; includeSubDomains; preload"
            )

        # 5. Content-Security-Policy: XSS 및 데이터 인젝션 공격 방지
        if self.include_csp:
            response.headers["Content-Security-Policy"] = self.csp_directives

        # 6. Referrer-Policy: Referrer 정보 제어
        response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"

        # 7. Permissions-Policy: 브라우저 기능 접근 제어
        response.headers["Permissions-Policy"] = (
            "geolocation=(), microphone=(), camera=(), payment=()"
        )

        # 8. X-Permitted-Cross-Domain-Policies: Adobe 제품 정책
        response.headers["X-Permitted-Cross-Domain-Policies"] = "none"

        return response
