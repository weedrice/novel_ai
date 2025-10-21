"""
LLM 서비스
여러 LLM 프로바이더(OpenAI, Claude, Gemini)를 지원합니다.
"""

import os
import re
from typing import List, Dict, Optional
from openai import OpenAI, OpenAIError
import anthropic
import google.generativeai as genai
from google.api_core import exceptions as google_exceptions
import logging

logger = logging.getLogger(__name__)


class LLMService:
    """멀티 LLM 프로바이더 지원 대사 생성 서비스"""

    def __init__(self):
        """LLM 클라이언트 초기화"""
        self.default_provider = os.getenv("DEFAULT_LLM_PROVIDER", "openai")

        # OpenAI 설정
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

        # Anthropic (Claude) 설정
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

        # Google Gemini 설정
        self.google_api_key = os.getenv("GOOGLE_API_KEY")
        self.gemini_model = os.getenv("GEMINI_MODEL", "gemini-pro")
        self.gemini_temperature = float(os.getenv("GEMINI_TEMPERATURE", "0.8"))
        self.gemini_max_tokens = int(os.getenv("GEMINI_MAX_TOKENS", "150"))

        if self.google_api_key and self.google_api_key != "your-google-api-key-here":
            genai.configure(api_key=self.google_api_key)
            self.gemini_model_instance = genai.GenerativeModel(self.gemini_model)
            logger.info("Google Gemini client initialized")
        else:
            self.gemini_model_instance = None
            logger.warning("Google API key not set")

    def generate_dialogue(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = None,
        temperature: float = None,
        n_candidates: int = 1,
        provider: Optional[str] = None
    ) -> List[str]:
        """
        LLM을 호출하여 대사 생성

        Args:
            system_prompt: 시스템 프롬프트 (캐릭터 페르소나)
            user_prompt: 사용자 프롬프트 (대사 생성 요청)
            max_tokens: 최대 토큰 수 (선택적)
            temperature: 온도 파라미터 (선택적)
            n_candidates: 생성할 후보 개수
            provider: LLM 프로바이더 (openai, claude, gemini) - 선택적

        Returns:
            생성된 대사 목록
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
            logger.error(f"Unknown provider: {provider}, using fallback")
            return self._generate_fallback_dialogues(n_candidates)

    def _generate_with_openai(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = None,
        temperature: float = None,
        n_candidates: int = 1
    ) -> List[str]:
        """OpenAI GPT로 대사 생성"""
        if not self.openai_client:
            logger.warning("OpenAI client not available, using fallback")
            return self._generate_fallback_dialogues(n_candidates)

        try:
            logger.info(f"Calling OpenAI API with model={self.openai_model}, n_candidates={n_candidates}")
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

            # 여러 후보가 있으면 모두 수집
            dialogues = []
            for choice in response.choices:
                content = choice.message.content.strip()
                logger.debug(f"OpenAI response choice: {content[:100]}...")
                parsed = self._parse_dialogues(content, 1)
                dialogues.extend(parsed)

            logger.info(f"OpenAI generated {len(dialogues)} dialogues")
            return dialogues[:n_candidates] if dialogues else self._generate_fallback_dialogues(n_candidates)

        except OpenAIError as e:
            logger.error(f"OpenAI API error: {e}")
            return self._generate_fallback_dialogues(n_candidates)
        except Exception as e:
            logger.error(f"Unexpected error with OpenAI: {e}")
            return self._generate_fallback_dialogues(n_candidates)

    def _generate_with_claude(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = None,
        temperature: float = None,
        n_candidates: int = 1
    ) -> List[str]:
        """Anthropic Claude로 대사 생성"""
        if not self.anthropic_client:
            logger.warning("Anthropic client not available, using fallback")
            return self._generate_fallback_dialogues(n_candidates)

        try:
            # Claude는 여러 후보를 직접 생성하지 못하므로 프롬프트에 명시
            if n_candidates > 1:
                modified_prompt = f"{user_prompt}\n\n{n_candidates}개의 다른 대사 후보를 각 줄에 하나씩 생성해주세요."
            else:
                modified_prompt = user_prompt

            logger.info(f"Calling Claude API with model={self.anthropic_model}, n_candidates={n_candidates}")
            message = self.anthropic_client.messages.create(
                model=self.anthropic_model,
                max_tokens=max_tokens or self.anthropic_max_tokens,
                temperature=temperature or self.anthropic_temperature,
                system=system_prompt,
                messages=[
                    {"role": "user", "content": modified_prompt}
                ]
            )

            content = message.content[0].text.strip()
            logger.debug(f"Claude response: {content[:100]}...")
            dialogues = self._parse_dialogues(content, n_candidates)
            logger.info(f"Claude generated {len(dialogues)} dialogues")
            return dialogues

        except anthropic.APIError as e:
            logger.error(f"Anthropic API error: {e}")
            return self._generate_fallback_dialogues(n_candidates)
        except Exception as e:
            logger.error(f"Unexpected error with Claude: {e}")
            return self._generate_fallback_dialogues(n_candidates)

    def _generate_with_gemini(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = None,
        temperature: float = None,
        n_candidates: int = 1
    ) -> List[str]:
        """Google Gemini로 대사 생성"""
        if not self.gemini_model_instance:
            logger.warning("Gemini client not available, using fallback")
            return self._generate_fallback_dialogues(n_candidates)

        try:
            # Gemini는 system prompt를 instruction으로 처리
            # n_candidates > 1이면 프롬프트에 명시
            if n_candidates > 1:
                full_prompt = f"{system_prompt}\n\n{user_prompt}\n\n{n_candidates}개의 다른 대사 후보를 각 줄에 하나씩 생성해주세요."
            else:
                full_prompt = f"{system_prompt}\n\n{user_prompt}"

            logger.info(f"Calling Gemini API with model={self.gemini_model}, n_candidates={n_candidates}")

            generation_config = genai.GenerationConfig(
                temperature=temperature or self.gemini_temperature,
                max_output_tokens=max_tokens or self.gemini_max_tokens,
                candidate_count=1,  # Gemini는 현재 candidate_count > 1 미지원, 프롬프트로 처리
            )

            # Safety settings를 제거하고 기본값 사용 (차단 방지)
            response = self.gemini_model_instance.generate_content(
                full_prompt,
                generation_config=generation_config
            )

            # 응답 처리
            if not response.candidates:
                logger.warning("Gemini returned no candidates")
                return self._generate_fallback_dialogues(n_candidates)

            # 첫 번째 candidate의 텍스트 추출
            content = response.text.strip()
            logger.debug(f"Gemini response: {content[:100]}...")

            dialogues = self._parse_dialogues(content, n_candidates)
            logger.info(f"Gemini generated {len(dialogues)} dialogues")
            return dialogues

        except google_exceptions.GoogleAPIError as e:
            logger.error(f"Google API error: {e}")
            return self._generate_fallback_dialogues(n_candidates)
        except google_exceptions.InvalidArgument as e:
            logger.error(f"Invalid argument to Gemini API: {e}")
            return self._generate_fallback_dialogues(n_candidates)
        except ValueError as e:
            logger.error(f"Gemini response blocked or invalid: {e}")
            return self._generate_fallback_dialogues(n_candidates)
        except Exception as e:
            logger.error(f"Unexpected error with Gemini: {e}", exc_info=True)
            return self._generate_fallback_dialogues(n_candidates)

    def _parse_dialogues(self, content: str, n_candidates: int) -> List[str]:
        """
        LLM 응답을 파싱하여 대사 목록으로 변환

        Args:
            content: LLM 응답 텍스트
            n_candidates: 필요한 후보 개수

        Returns:
            파싱된 대사 목록
        """
        # 줄바꿈으로 구분된 대사들을 분리
        dialogues = [line.strip() for line in content.split('\n') if line.strip()]

        # 번호나 기호 제거
        cleaned_dialogues = []
        for dialogue in dialogues:
            # 숫자, 점, 대시, 불릿 포인트 제거
            cleaned = re.sub(r'^\d+[\.\)]\s*', '', dialogue)  # "1. " or "1) "
            cleaned = re.sub(r'^[-•\*]\s*', '', cleaned)      # "- " or "• " or "* "
            cleaned = re.sub(r'^["\']|["\']$', '', cleaned)    # 앞뒤 따옴표 제거
            if cleaned:
                cleaned_dialogues.append(cleaned)

        # 요청한 개수만큼 반환 (부족하면 있는 만큼)
        return cleaned_dialogues[:n_candidates] if cleaned_dialogues else self._generate_fallback_dialogues(n_candidates)

    def _generate_fallback_dialogues(self, n_candidates: int) -> List[str]:
        """
        API 호출 실패 시 사용할 더미 대사 생성

        Args:
            n_candidates: 생성할 후보 개수

        Returns:
            더미 대사 목록
        """
        fallback_dialogues = [
            "안녕? 오랜만이야!",
            "어, 안녕! 잘 지냈어?",
            "야! 여기서 뭐해?",
        ]

        return fallback_dialogues[:n_candidates]

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