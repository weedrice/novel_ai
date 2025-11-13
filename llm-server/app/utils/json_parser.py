"""
JSON 파싱 유틸리티
LLM 응답에서 JSON을 추출하고 파싱하는 공통 기능
"""

import json
import logging
from typing import Any, Dict, Optional

logger = logging.getLogger(__name__)


class JSONParser:
    """LLM 응답에서 JSON을 파싱하는 유틸리티 클래스"""

    @staticmethod
    def extract_json_from_response(response_text: str) -> Optional[str]:
        """
        LLM 응답에서 JSON 텍스트 추출
        마크다운 코드 블록이 있으면 제거

        Args:
            response_text: LLM 응답 원문

        Returns:
            추출된 JSON 문자열 또는 None
        """
        if not response_text:
            return None

        text = response_text.strip()

        # ```json ... ``` 패턴 추출
        if "```json" in text:
            try:
                text = text.split("```json")[1].split("```")[0].strip()
            except IndexError:
                logger.warning("Failed to extract JSON from ```json block")

        # ``` ... ``` 패턴 추출
        elif "```" in text:
            try:
                text = text.split("```")[1].split("```")[0].strip()
            except IndexError:
                logger.warning("Failed to extract JSON from ``` block")

        return text

    @staticmethod
    def parse_json_response(response_text: str) -> Optional[Dict[str, Any]]:
        """
        LLM 응답을 JSON으로 파싱

        Args:
            response_text: LLM 응답 원문

        Returns:
            파싱된 JSON 딕셔너리 또는 None
        """
        try:
            # JSON 추출
            json_text = JSONParser.extract_json_from_response(response_text)
            if not json_text:
                logger.warning("No JSON text extracted from response")
                return None

            # JSON 파싱
            data = json.loads(json_text)
            logger.info(f"Successfully parsed JSON with {len(data)} top-level keys")
            return data

        except json.JSONDecodeError as e:
            logger.error(f"Failed to parse JSON: {e}")
            logger.debug(f"Failed JSON text: {response_text[:500]}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error parsing JSON: {e}")
            return None

    @staticmethod
    def safe_get(data: Optional[Dict[str, Any]], key: str, default: Any = None) -> Any:
        """
        안전하게 딕셔너리에서 값 가져오기

        Args:
            data: 딕셔너리
            key: 키
            default: 기본값

        Returns:
            값 또는 기본값
        """
        if data is None:
            return default
        return data.get(key, default)
