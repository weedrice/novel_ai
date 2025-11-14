"""
Redis 캐시 매니저
LLM 응답 캐싱을 통한 성능 최적화 및 비용 절감
"""

import hashlib
import json
import logging
from typing import Optional
import redis.asyncio as redis
from redis.exceptions import RedisError

logger = logging.getLogger(__name__)


class CacheManager:
    """Redis 기반 캐시 매니저"""

    def __init__(self, redis_url: str = "redis://localhost:6379", enabled: bool = True):
        """
        Args:
            redis_url: Redis 연결 URL
            enabled: 캐싱 활성화 여부 (False면 항상 cache miss)
        """
        self.redis_url = redis_url
        self.enabled = enabled
        self.redis_client: Optional[redis.Redis] = None

        if self.enabled:
            try:
                self.redis_client = redis.from_url(
                    redis_url,
                    encoding="utf-8",
                    decode_responses=True,
                    socket_connect_timeout=5,
                )
                logger.info(f"Cache Manager initialized with Redis: {redis_url}")
            except Exception as e:
                logger.warning(f"Failed to connect to Redis: {e}. Caching disabled.")
                self.enabled = False

    async def get_cached_response(self, prompt_hash: str) -> Optional[str]:
        """
        캐시에서 LLM 응답 가져오기

        Args:
            prompt_hash: 프롬프트 해시값

        Returns:
            캐시된 응답 텍스트, 없으면 None
        """
        if not self.enabled or not self.redis_client:
            return None

        try:
            cached = await self.redis_client.get(f"llm:{prompt_hash}")
            if cached:
                logger.info(f"Cache HIT: {prompt_hash[:16]}...")
                return cached
            else:
                logger.debug(f"Cache MISS: {prompt_hash[:16]}...")
                return None
        except RedisError as e:
            logger.error(f"Redis get error: {e}")
            return None

    async def cache_response(
        self, prompt_hash: str, response: str, ttl: int = 3600
    ) -> bool:
        """
        LLM 응답을 캐시에 저장

        Args:
            prompt_hash: 프롬프트 해시값
            response: LLM 응답 텍스트
            ttl: Time-To-Live (초 단위, 기본 1시간)

        Returns:
            성공 여부
        """
        if not self.enabled or not self.redis_client:
            return False

        try:
            await self.redis_client.setex(f"llm:{prompt_hash}", ttl, response)
            logger.debug(f"Cached response: {prompt_hash[:16]}... (TTL: {ttl}s)")
            return True
        except RedisError as e:
            logger.error(f"Redis set error: {e}")
            return False

    @staticmethod
    def hash_prompt(
        system_prompt: str,
        user_prompt: str,
        provider: str = "",
        temperature: float = 0.0,
        max_tokens: int = 0,
        **kwargs,
    ) -> str:
        """
        프롬프트를 해시로 변환

        동일한 프롬프트와 파라미터는 동일한 해시값 생성
        캐시 키로 사용

        Args:
            system_prompt: 시스템 프롬프트
            user_prompt: 유저 프롬프트
            provider: LLM 프로바이더
            temperature: 온도 파라미터
            max_tokens: 최대 토큰 수
            **kwargs: 기타 파라미터

        Returns:
            SHA256 해시값 (64자)
        """
        # 정확한 캐시 키를 위해 모든 파라미터 포함
        cache_key_data = {
            "system": system_prompt,
            "user": user_prompt,
            "provider": provider,
            "temperature": temperature,
            "max_tokens": max_tokens,
            **kwargs,
        }

        # JSON으로 직렬화 (키 정렬로 일관성 보장)
        cache_key_str = json.dumps(cache_key_data, sort_keys=True, ensure_ascii=False)

        # SHA256 해시 생성
        return hashlib.sha256(cache_key_str.encode("utf-8")).hexdigest()

    async def clear_cache(self, pattern: str = "llm:*") -> int:
        """
        캐시 삭제 (주의: 프로덕션에서는 신중하게 사용)

        Args:
            pattern: 삭제할 키 패턴 (기본: 모든 LLM 캐시)

        Returns:
            삭제된 키 개수
        """
        if not self.enabled or not self.redis_client:
            return 0

        try:
            keys = []
            async for key in self.redis_client.scan_iter(match=pattern):
                keys.append(key)

            if keys:
                deleted = await self.redis_client.delete(*keys)
                logger.info(f"Cleared {deleted} cache entries with pattern: {pattern}")
                return deleted
            return 0
        except RedisError as e:
            logger.error(f"Redis clear error: {e}")
            return 0

    async def get_cache_stats(self) -> dict:
        """
        캐시 통계 조회

        Returns:
            캐시 통계 정보
        """
        if not self.enabled or not self.redis_client:
            return {"enabled": False}

        try:
            info = await self.redis_client.info("stats")
            keyspace = await self.redis_client.info("keyspace")

            # LLM 캐시 키 개수 카운트
            llm_key_count = 0
            async for _ in self.redis_client.scan_iter(match="llm:*", count=1000):
                llm_key_count += 1

            return {
                "enabled": True,
                "total_connections": info.get("total_connections_received", 0),
                "total_commands": info.get("total_commands_processed", 0),
                "llm_cache_keys": llm_key_count,
                "keyspace": keyspace,
            }
        except RedisError as e:
            logger.error(f"Redis stats error: {e}")
            return {"enabled": True, "error": str(e)}

    async def close(self):
        """Redis 연결 종료"""
        if self.redis_client:
            await self.redis_client.close()
            logger.info("Cache Manager connection closed")
