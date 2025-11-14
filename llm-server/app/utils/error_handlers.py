"""
에러 핸들링 데코레이터
"""

import logging
from functools import wraps
from typing import Callable, Any, TypeVar, Optional
from app.exceptions import LLMServerException

logger = logging.getLogger(__name__)

T = TypeVar('T')


def with_error_handling(
    fallback_value: Any = None,
    log_level: str = "error",
    suppress_exceptions: bool = True
):
    """
    서비스 메서드에 에러 핸들링을 추가하는 데코레이터

    Args:
        fallback_value: 에러 발생 시 반환할 기본값
        log_level: 로그 레벨 ("error", "warning", "info")
        suppress_exceptions: True이면 예외를 억제하고 fallback 반환, False이면 예외를 다시 발생

    Usage:
        @with_error_handling(fallback_value={"error": "Failed"})
        def my_service_method(self, ...):
            # 위험한 작업
            return result
    """

    def decorator(func: Callable[..., T]) -> Callable[..., T]:
        @wraps(func)
        def wrapper(*args, **kwargs) -> T:
            try:
                return func(*args, **kwargs)
            except LLMServerException as e:
                # 커스텀 예외는 상세 정보와 함께 로깅
                log_func = getattr(logger, log_level)
                log_func(
                    f"{func.__name__} failed: {e.message}",
                    extra={"details": e.details}
                )
                if not suppress_exceptions:
                    raise
                return fallback_value
            except Exception as e:
                # 일반 예외도 로깅
                log_func = getattr(logger, log_level)
                log_func(f"{func.__name__} failed with unexpected error: {str(e)}")
                if not suppress_exceptions:
                    raise
                return fallback_value

        return wrapper

    return decorator


def with_llm_fallback(fallback_func: Optional[Callable] = None):
    """
    LLM 호출 실패 시 fallback 함수를 실행하는 데코레이터

    Args:
        fallback_func: 실패 시 호출할 fallback 함수. None이면 빈 결과 반환

    Usage:
        def _generate_fallback_dialogue(self, inp):
            return {"suggestions": []}

        @with_llm_fallback(fallback_func=_generate_fallback_dialogue)
        def generate_dialogue(self, inp):
            # LLM 호출
            return result
    """

    def decorator(func: Callable) -> Callable:
        @wraps(func)
        def wrapper(self, *args, **kwargs):
            try:
                return func(self, *args, **kwargs)
            except Exception as e:
                logger.error(
                    f"LLM operation '{func.__name__}' failed: {str(e)}. "
                    f"Using fallback."
                )

                # fallback 함수가 있으면 실행
                if fallback_func:
                    return fallback_func(self, *args, **kwargs)

                # fallback 함수가 없으면 빈 결과 반환
                return None

        return wrapper

    return decorator


def retry_on_failure(max_retries: int = 3, delay: float = 1.0):
    """
    실패 시 재시도하는 데코레이터

    Args:
        max_retries: 최대 재시도 횟수
        delay: 재시도 간 대기 시간 (초)

    Usage:
        @retry_on_failure(max_retries=3, delay=2.0)
        def call_external_api(self):
            # API 호출
            return result
    """
    import time

    def decorator(func: Callable) -> Callable:
        @wraps(func)
        def wrapper(*args, **kwargs):
            last_exception = None

            for attempt in range(max_retries):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    last_exception = e
                    if attempt < max_retries - 1:
                        logger.warning(
                            f"{func.__name__} failed (attempt {attempt + 1}/{max_retries}). "
                            f"Retrying in {delay}s... Error: {str(e)}"
                        )
                        time.sleep(delay)
                    else:
                        logger.error(
                            f"{func.__name__} failed after {max_retries} attempts. "
                            f"Error: {str(e)}"
                        )

            # 모든 재시도 실패 시 마지막 예외 발생
            raise last_exception

        return wrapper

    return decorator
