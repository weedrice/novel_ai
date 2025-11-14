"""
환경 설정 관리
Pydantic Settings를 사용한 타입 안전한 설정
"""

from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache
from typing import List
import os


class Settings(BaseSettings):
    """애플리케이션 설정"""

    # Application
    app_name: str = "Character Tone LLM Server"
    app_version: str = "0.3.0"
    environment: str = "development"
    debug: bool = False

    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    # CORS
    allowed_origins: List[str] = ["http://localhost:3000", "http://localhost:3001"]

    # LLM Providers
    default_llm_provider: str = "openai"

    openai_api_key: str = ""
    openai_model: str = "gpt-3.5-turbo"
    openai_temperature: float = 0.8
    openai_max_tokens: int = 150
    openai_timeout: int = 30

    anthropic_api_key: str = ""
    anthropic_model: str = "claude-3-haiku-20240307"
    anthropic_temperature: float = 0.8
    anthropic_max_tokens: int = 150

    google_api_key: str = ""
    gemini_model: str = "gemini-pro"
    gemini_temperature: float = 0.8
    gemini_max_tokens: int = 150

    # Redis Cache
    redis_host: str = "redis"
    redis_port: int = 6379
    redis_url: str = ""  # 설정되면 redis_host/port 무시
    cache_enabled: bool = True
    cache_ttl: int = 3600  # 1시간

    # Security
    api_key: str = ""  # 빈 문자열이면 인증 비활성화

    # Logging
    log_level: str = "INFO"

    # Monitoring
    metrics_enabled: bool = True

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    def get_redis_url(self) -> str:
        """Redis URL 생성"""
        if self.redis_url:
            return self.redis_url
        return f"redis://{self.redis_host}:{self.redis_port}"


@lru_cache()
def get_settings() -> Settings:
    """
    설정 인스턴스 가져오기 (캐싱됨)

    Returns:
        Settings 인스턴스
    """
    return Settings()
