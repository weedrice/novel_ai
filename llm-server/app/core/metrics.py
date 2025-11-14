"""
Prometheus 메트릭 정의
성능 모니터링 및 관찰성 확보
"""

from prometheus_client import Counter, Histogram, Gauge, Info
import time
from functools import wraps
import logging
from typing import Callable

logger = logging.getLogger(__name__)

# ============================================================
# LLM 요청 메트릭
# ============================================================

llm_request_total = Counter(
    "llm_requests_total",
    "Total number of LLM requests",
    ["provider", "endpoint", "status"],
)

llm_request_duration_seconds = Histogram(
    "llm_request_duration_seconds",
    "LLM request duration in seconds",
    ["provider", "endpoint"],
    buckets=(0.1, 0.5, 1.0, 2.0, 5.0, 10.0, 30.0, 60.0, float("inf")),
)

llm_tokens_used_total = Counter(
    "llm_tokens_used_total",
    "Total tokens used by LLM requests",
    ["provider", "type"],  # type: prompt, completion
)

llm_error_total = Counter(
    "llm_errors_total",
    "Total number of LLM errors",
    ["provider", "error_type"],
)

# ============================================================
# 캐시 메트릭
# ============================================================

cache_hit_total = Counter(
    "cache_hit_total",
    "Total number of cache hits",
    ["cache_type"],
)

cache_miss_total = Counter(
    "cache_miss_total",
    "Total number of cache misses",
    ["cache_type"],
)

cache_size = Gauge(
    "cache_size_bytes",
    "Current cache size in bytes",
    ["cache_type"],
)

# ============================================================
# Rate Limiting 메트릭
# ============================================================

rate_limit_exceeded_total = Counter(
    "rate_limit_exceeded_total",
    "Total number of rate limit exceeded events",
    ["endpoint"],
)

# ============================================================
# 애플리케이션 메트릭
# ============================================================

app_info = Info(
    "app_info",
    "Application information",
)

active_requests = Gauge(
    "active_requests",
    "Number of requests currently being processed",
)

# ============================================================
# 데코레이터: 메트릭 자동 수집
# ============================================================


def track_llm_request(provider: str, endpoint: str):
    """
    LLM 요청 메트릭 추적 데코레이터

    사용 예:
        @track_llm_request("openai", "generate")
        def generate_with_openai(...):
            ...
    """

    def decorator(func: Callable) -> Callable:
        @wraps(func)
        def sync_wrapper(*args, **kwargs):
            start_time = time.time()
            status = "success"

            try:
                result = func(*args, **kwargs)
                return result

            except Exception as e:
                status = "error"
                error_type = type(e).__name__
                llm_error_total.labels(provider=provider, error_type=error_type).inc()
                raise

            finally:
                duration = time.time() - start_time
                llm_request_total.labels(
                    provider=provider, endpoint=endpoint, status=status
                ).inc()
                llm_request_duration_seconds.labels(
                    provider=provider, endpoint=endpoint
                ).observe(duration)

        @wraps(func)
        async def async_wrapper(*args, **kwargs):
            start_time = time.time()
            status = "success"

            try:
                result = await func(*args, **kwargs)
                return result

            except Exception as e:
                status = "error"
                error_type = type(e).__name__
                llm_error_total.labels(provider=provider, error_type=error_type).inc()
                raise

            finally:
                duration = time.time() - start_time
                llm_request_total.labels(
                    provider=provider, endpoint=endpoint, status=status
                ).inc()
                llm_request_duration_seconds.labels(
                    provider=provider, endpoint=endpoint
                ).observe(duration)

        # async 함수인지 확인
        import inspect

        if inspect.iscoroutinefunction(func):
            return async_wrapper
        else:
            return sync_wrapper

    return decorator


def track_cache_access(cache_type: str = "redis"):
    """
    캐시 접근 메트릭 추적 데코레이터

    사용 예:
        @track_cache_access("redis")
        async def get_from_cache(key):
            ...
    """

    def decorator(func: Callable) -> Callable:
        @wraps(func)
        async def wrapper(*args, **kwargs):
            result = await func(*args, **kwargs)

            # None이면 cache miss, 값이 있으면 cache hit
            if result is None:
                cache_miss_total.labels(cache_type=cache_type).inc()
            else:
                cache_hit_total.labels(cache_type=cache_type).inc()

            return result

        return wrapper

    return decorator


def get_cache_hit_rate(cache_type: str = "redis") -> float:
    """
    캐시 히트율 계산

    Args:
        cache_type: 캐시 유형

    Returns:
        히트율 (0.0 ~ 1.0)
    """
    try:
        hits = cache_hit_total.labels(cache_type=cache_type)._value._value
        misses = cache_miss_total.labels(cache_type=cache_type)._value._value

        total = hits + misses
        if total == 0:
            return 0.0

        return hits / total

    except Exception as e:
        logger.error(f"Failed to calculate cache hit rate: {e}")
        return 0.0


def init_app_info(version: str, environment: str):
    """
    애플리케이션 정보 초기화

    Args:
        version: 애플리케이션 버전
        environment: 환경 (development, staging, production)
    """
    app_info.info(
        {
            "version": version,
            "environment": environment,
            "name": "llm-server",
        }
    )
    logger.info(f"Metrics initialized: v{version} ({environment})")
