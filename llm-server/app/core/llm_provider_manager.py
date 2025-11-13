"""
LLM Provider Manager
여러 LLM 프로바이더(OpenAI, Claude, Gemini)를 통합 관리
"""

import os
import logging
from typing import List, Optional, AsyncIterator
from openai import OpenAI, OpenAIError
import anthropic
import google.generativeai as genai
from google.api_core import exceptions as google_exceptions

logger = logging.getLogger(__name__)


class LLMProviderManager:
    """LLM 프로바이더 통합 관리 클래스"""

    def __init__(self):
        """LLM 클라이언트 초기화"""
        self.default_provider = os.getenv("DEFAULT_LLM_PROVIDER", "openai")

        # OpenAI 설정
        self._init_openai()

        # Anthropic (Claude) 설정
        self._init_anthropic()

        # Google Gemini 설정
        self._init_gemini()

    def _init_openai(self):
        """OpenAI 클라이언트 초기화"""
        self.openai_api_key = os.getenv("OPENAI_API_KEY")
        self.openai_model = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")
        self.openai_temperature = float(os.getenv("OPENAI_TEMPERATURE", "0.8"))
        self.openai_max_tokens = int(os.getenv("OPENAI_MAX_TOKENS", "150"))

        if self.openai_api_key and self.openai_api_key != "your-openai-api-key-here":
            self.openai_client = OpenAI(api_key=self.openai_api_key)
            logger.info("OpenAI client initialized")
        else:
            self.openai_client = None
            logger.warning("OpenAI API key not set")

    def _init_anthropic(self):
        """Anthropic Claude 클라이언트 초기화"""
        self.anthropic_api_key = os.getenv("ANTHROPIC_API_KEY")
        self.anthropic_model = os.getenv("ANTHROPIC_MODEL", "claude-3-haiku-20240307")
        self.anthropic_temperature = float(os.getenv("ANTHROPIC_TEMPERATURE", "0.8"))
        self.anthropic_max_tokens = int(os.getenv("ANTHROPIC_MAX_TOKENS", "150"))

        if self.anthropic_api_key and self.anthropic_api_key != "your-anthropic-api-key-here":
            self.anthropic_client = anthropic.Anthropic(api_key=self.anthropic_api_key)
            logger.info("Anthropic client initialized")
        else:
            self.anthropic_client = None
            logger.warning("Anthropic API key not set")

    def _init_gemini(self):
        """Google Gemini 클라이언트 초기화"""
        self.google_api_key = os.getenv("GOOGLE_API_KEY")
        self.gemini_model = os.getenv("GEMINI_MODEL", "gemini-2.5-pro")
        self.gemini_temperature = float(os.getenv("GEMINI_TEMPERATURE", "0.8"))
        self.gemini_max_tokens = int(os.getenv("GEMINI_MAX_TOKENS", "150"))

        if self.google_api_key and self.google_api_key != "your-google-api-key-here":
            genai.configure(api_key=self.google_api_key)
            self.gemini_model_instance = genai.GenerativeModel(self.gemini_model)
            logger.info("Google Gemini client initialized")
        else:
            self.gemini_model_instance = None
            logger.warning("Google API key not set")

    def get_available_providers(self) -> List[str]:
        """
        사용 가능한 LLM 프로바이더 목록 반환

        Returns:
            사용 가능한 프로바이더 목록
        """
        providers = []
        if self.openai_client:
            providers.append("openai")
        if self.anthropic_client:
            providers.append("claude")
        if self.gemini_model_instance:
            providers.append("gemini")

        return providers if providers else ["fallback"]

    def generate(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int] = None,
        temperature: Optional[float] = None,
        n_candidates: int = 1,
        provider: Optional[str] = None
    ) -> str:
        """
        LLM을 호출하여 텍스트 생성

        Args:
            system_prompt: 시스템 프롬프트
            user_prompt: 사용자 프롬프트
            max_tokens: 최대 토큰 수
            temperature: 온도 파라미터
            n_candidates: 생성할 후보 개수 (OpenAI만 지원)
            provider: LLM 프로바이더 (openai, claude, gemini)

        Returns:
            생성된 텍스트
        """
        provider = provider or self.default_provider
        logger.info(f"Using LLM provider: {provider}")

        if provider == "openai":
            return self._generate_with_openai(system_prompt, user_prompt, max_tokens, temperature, n_candidates)
        elif provider == "claude":
            return self._generate_with_claude(system_prompt, user_prompt, max_tokens, temperature, n_candidates)
        elif provider == "gemini":
            return self._generate_with_gemini(system_prompt, user_prompt, max_tokens, temperature, n_candidates)
        else:
            raise ValueError(f"Unknown provider: {provider}")

    def _generate_with_openai(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int],
        temperature: Optional[float],
        n_candidates: int
    ) -> str:
        """OpenAI GPT로 텍스트 생성"""
        if not self.openai_client:
            raise RuntimeError("OpenAI client not available")

        logger.info(f"Calling OpenAI API: model={self.openai_model}, n_candidates={n_candidates}")

        response = self.openai_client.chat.completions.create(
            model=self.openai_model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            max_tokens=max_tokens or self.openai_max_tokens,
            temperature=temperature or self.openai_temperature,
            n=n_candidates
        )

        # 첫 번째 응답 반환
        return response.choices[0].message.content.strip()

    def _generate_with_claude(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int],
        temperature: Optional[float],
        n_candidates: int
    ) -> str:
        """Anthropic Claude로 텍스트 생성"""
        if not self.anthropic_client:
            raise RuntimeError("Anthropic client not available")

        # Claude는 여러 후보를 직접 생성하지 못하므로 프롬프트에 명시
        if n_candidates > 1:
            user_prompt = f"{user_prompt}\n\n{n_candidates}개의 다른 대사 후보를 각 줄에 하나씩 생성해주세요."

        logger.info(f"Calling Claude API: model={self.anthropic_model}")

        message = self.anthropic_client.messages.create(
            model=self.anthropic_model,
            max_tokens=max_tokens or self.anthropic_max_tokens,
            temperature=temperature or self.anthropic_temperature,
            system=system_prompt,
            messages=[
                {"role": "user", "content": user_prompt}
            ]
        )

        return message.content[0].text.strip()

    def _generate_with_gemini(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int],
        temperature: Optional[float],
        n_candidates: int
    ) -> str:
        """Google Gemini로 텍스트 생성"""
        if not self.gemini_model_instance:
            raise RuntimeError("Gemini client not available")

        # Gemini는 system prompt를 instruction으로 처리
        if n_candidates > 1:
            full_prompt = f"{system_prompt}\n\n{user_prompt}\n\n{n_candidates}개의 다른 대사 후보를 각 줄에 하나씩 생성해주세요."
        else:
            full_prompt = f"{system_prompt}\n\n{user_prompt}"

        logger.info(f"Calling Gemini API: model={self.gemini_model}")

        generation_config = genai.GenerationConfig(
            temperature=temperature or self.gemini_temperature,
            max_output_tokens=max_tokens or self.gemini_max_tokens,
        )

        # Safety settings를 완화하여 차단 최소화
        safety_settings = [
            {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "block_none"},
            {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "block_none"},
            {"category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "block_none"},
            {"category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "block_none"}
        ]

        response = self.gemini_model_instance.generate_content(
            full_prompt,
            generation_config=generation_config,
            safety_settings=safety_settings
        )

        # 응답 처리
        if not response.candidates:
            raise RuntimeError("Gemini returned no candidates")

        candidate = response.candidates[0]
        finish_reason = candidate.finish_reason

        if finish_reason == 2:  # SAFETY
            logger.warning("Gemini response blocked by safety filter")
            raise RuntimeError("Response blocked by safety filter")
        elif finish_reason == 3:  # RECITATION
            logger.warning("Gemini response blocked due to recitation")
            raise RuntimeError("Response blocked due to recitation")
        elif finish_reason not in [0, 1]:  # 0=UNSPECIFIED, 1=STOP
            raise RuntimeError(f"Unexpected finish reason: {finish_reason}")

        return response.text.strip()

    async def generate_stream(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int] = None,
        temperature: Optional[float] = None,
        provider: Optional[str] = None
    ) -> AsyncIterator[str]:
        """
        LLM을 호출하여 스트리밍 방식으로 텍스트 생성

        Args:
            system_prompt: 시스템 프롬프트
            user_prompt: 사용자 프롬프트
            max_tokens: 최대 토큰 수
            temperature: 온도 파라미터
            provider: LLM 프로바이더

        Yields:
            생성된 텍스트 청크
        """
        provider = provider or self.default_provider
        logger.info(f"Using LLM provider for streaming: {provider}")

        if provider == "openai":
            async for chunk in self._stream_with_openai(system_prompt, user_prompt, max_tokens, temperature):
                yield chunk
        elif provider == "claude":
            async for chunk in self._stream_with_claude(system_prompt, user_prompt, max_tokens, temperature):
                yield chunk
        elif provider == "gemini":
            async for chunk in self._stream_with_gemini(system_prompt, user_prompt, max_tokens, temperature):
                yield chunk
        else:
            raise ValueError(f"Unknown provider: {provider}")

    async def _stream_with_openai(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int],
        temperature: Optional[float]
    ) -> AsyncIterator[str]:
        """OpenAI GPT로 스트리밍 텍스트 생성"""
        if not self.openai_client:
            raise RuntimeError("OpenAI client not available")

        logger.info(f"Streaming from OpenAI: model={self.openai_model}")

        stream = self.openai_client.chat.completions.create(
            model=self.openai_model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            max_tokens=max_tokens or self.openai_max_tokens,
            temperature=temperature or self.openai_temperature,
            stream=True
        )

        for chunk in stream:
            if chunk.choices[0].delta.content is not None:
                yield chunk.choices[0].delta.content

    async def _stream_with_claude(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int],
        temperature: Optional[float]
    ) -> AsyncIterator[str]:
        """Anthropic Claude로 스트리밍 텍스트 생성"""
        if not self.anthropic_client:
            raise RuntimeError("Anthropic client not available")

        logger.info(f"Streaming from Claude: model={self.anthropic_model}")

        with self.anthropic_client.messages.stream(
            model=self.anthropic_model,
            max_tokens=max_tokens or self.anthropic_max_tokens,
            temperature=temperature or self.anthropic_temperature,
            system=system_prompt,
            messages=[
                {"role": "user", "content": user_prompt}
            ]
        ) as stream:
            for text in stream.text_stream:
                yield text

    async def _stream_with_gemini(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: Optional[int],
        temperature: Optional[float]
    ) -> AsyncIterator[str]:
        """Google Gemini로 스트리밍 텍스트 생성"""
        if not self.gemini_model_instance:
            raise RuntimeError("Gemini client not available")

        logger.info(f"Streaming from Gemini: model={self.gemini_model}")

        full_prompt = f"{system_prompt}\n\n{user_prompt}"

        generation_config = genai.GenerationConfig(
            temperature=temperature or self.gemini_temperature,
            max_output_tokens=max_tokens or self.gemini_max_tokens,
        )

        safety_settings = [
            {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "block_none"},
            {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "block_none"},
            {"category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "block_none"},
            {"category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "block_none"}
        ]

        response = self.gemini_model_instance.generate_content(
            full_prompt,
            generation_config=generation_config,
            safety_settings=safety_settings,
            stream=True
        )

        for chunk in response:
            if chunk.text:
                yield chunk.text
