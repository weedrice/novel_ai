"""
Rate Limiter 설정
slowapi를 사용한 API Rate Limiting
"""

import os
from slowapi import Limiter
from slowapi.util import get_remote_address


def _create_limiter():
    """Rate Limiter 인스턴스 생성 (테스트 모드에서는 No-op)"""
    # 테스트 환경에서는 Rate Limiting을 비활성화
    if os.getenv("TESTING") == "true":
        # No-op limiter for testing
        class MockLimiter:
            def limit(self, *args, **kwargs):
                """테스트용 데코레이터 - 아무것도 하지 않음"""
                def decorator(func):
                    return func
                return decorator

        return MockLimiter()

    # 프로덕션 환경에서는 실제 Rate Limiter 사용
    return Limiter(key_func=get_remote_address)


# Rate Limiter 인스턴스
# IP 주소 기반으로 요청 제한
limiter = _create_limiter()
