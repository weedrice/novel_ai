"""
LLM 서비스
OpenAI API를 호출하여 대사를 생성합니다.
"""

import os
from typing import List, Dict
from openai import OpenAI, OpenAIError
import logging

logger = logging.getLogger(__name__)


class LLMService:
    """OpenAI API 기반 대사 생성 서비스"""

    def __init__(self):
        """OpenAI 클라이언트 초기화"""
        self.api_key = os.getenv("OPENAI_API_KEY")
        self.model = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")
        self.temperature = float(os.getenv("OPENAI_TEMPERATURE", "0.8"))
        self.max_tokens = int(os.getenv("OPENAI_MAX_TOKENS", "150"))

        if not self.api_key or self.api_key == "your-api-key-here":
            logger.warning("OpenAI API key not set. LLM service will use fallback responses.")
            self.client = None
        else:
            self.client = OpenAI(api_key=self.api_key)

    def generate_dialogue(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = None,
        temperature: float = None,
        n_candidates: int = 1
    ) -> List[str]:
        """
        LLM을 호출하여 대사 생성

        Args:
            system_prompt: 시스템 프롬프트 (캐릭터 페르소나)
            user_prompt: 사용자 프롬프트 (대사 생성 요청)
            max_tokens: 최대 토큰 수 (선택적)
            temperature: 온도 파라미터 (선택적)
            n_candidates: 생성할 후보 개수

        Returns:
            생성된 대사 목록
        """
        if not self.client:
            # API 키가 없을 경우 더미 응답 반환
            logger.info("Using fallback dialogue generation (no API key)")
            return self._generate_fallback_dialogues(n_candidates)

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                max_tokens=max_tokens or self.max_tokens,
                temperature=temperature or self.temperature,
                n=1  # OpenAI API는 한 번에 하나씩 생성
            )

            # 응답에서 대사 추출
            content = response.choices[0].message.content.strip()

            # 줄바꿈으로 구분된 대사들을 분리
            dialogues = [line.strip() for line in content.split('\n') if line.strip()]

            # 번호나 기호 제거 (예: "1. ", "- ", "• " 등)
            cleaned_dialogues = []
            for dialogue in dialogues:
                # 숫자, 점, 대시, 불릿 포인트 제거
                cleaned = dialogue
                import re
                cleaned = re.sub(r'^\d+[\.\)]\s*', '', cleaned)  # "1. " or "1) "
                cleaned = re.sub(r'^[-•\*]\s*', '', cleaned)    # "- " or "• " or "* "
                cleaned = re.sub(r'^["\']|["\']$', '', cleaned)  # 앞뒤 따옴표 제거
                if cleaned:
                    cleaned_dialogues.append(cleaned)

            # 요청한 개수만큼 반환 (부족하면 있는 만큼)
            return cleaned_dialogues[:n_candidates]

        except OpenAIError as e:
            logger.error(f"OpenAI API error: {e}")
            return self._generate_fallback_dialogues(n_candidates)

        except Exception as e:
            logger.error(f"Unexpected error in LLM service: {e}")
            return self._generate_fallback_dialogues(n_candidates)

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

    def validate_api_key(self) -> bool:
        """
        API 키 유효성 검증

        Returns:
            API 키가 설정되어 있고 유효하면 True
        """
        if not self.client:
            return False

        try:
            # 간단한 테스트 요청
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user", "content": "Hello"}],
                max_tokens=5
            )
            return True
        except OpenAIError:
            return False
        except Exception:
            return False