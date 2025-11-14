"""
스크립트 분석 서비스
소설/시나리오 등 스크립트 종합 분석
"""

import logging
from typing import List
from app.core.llm_provider_manager import LLMProviderManager
from app.utils.json_parser import JSONParser
from app.utils.prompt_templates import PromptTemplates
from app.utils.error_handlers import with_llm_fallback
from app.models.script_analysis_models import (
    ScriptAnalysisInput,
    ScriptAnalysisResponse,
    ExtractedCharacter,
    ExtractedDialogue,
    ExtractedScene,
    ExtractedRelationship
)

logger = logging.getLogger(__name__)


class ScriptAnalysisService:
    """스크립트 분석 서비스"""

    def __init__(self, llm_manager: LLMProviderManager):
        self.llm_manager = llm_manager

    @with_llm_fallback(fallback_func=lambda self, inp: self._generate_empty_analysis())
    def analyze_script(self, inp: ScriptAnalysisInput) -> ScriptAnalysisResponse:
        """
        스크립트 종합 분석

        Args:
            inp: 스크립트 분석 입력

        Returns:
            캐릭터, 대사, 장면, 관계 분석 결과
        """
        logger.info(
            f"Analyzing script: length={len(inp.content)} chars, "
            f"format={inp.formatHint}, provider={inp.provider}"
        )

        # 프롬프트 생성
        system_prompt = PromptTemplates.SCRIPT_ANALYSIS_SYSTEM
        user_prompt = PromptTemplates.script_analysis_user_prompt(inp.content, inp.formatHint)

        logger.info(
            f"Analysis prompts: system={len(system_prompt)} chars, user={len(user_prompt)} chars"
        )

        # LLM 호출
        generated_text = self.llm_manager.generate(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=2000,
            temperature=0.3,
            provider=inp.provider,
        )

        if not generated_text:
            logger.warning("No analysis result, returning empty structure")
            return self._generate_empty_analysis()

        # JSON 파싱
        logger.info(f"Raw LLM response preview: {generated_text[:500]}")
        analysis_data = JSONParser.parse_json_response(generated_text)

        if not analysis_data:
            logger.warning("Failed to parse JSON, returning empty structure")
            return self._generate_empty_analysis()

        # Pydantic 모델로 변환
        characters = [
            ExtractedCharacter(**c) for c in analysis_data.get("characters", [])
        ]
        dialogues = [
            ExtractedDialogue(**d) for d in analysis_data.get("dialogues", [])
        ]
        scenes = [
            ExtractedScene(**s) for s in analysis_data.get("scenes", [])
        ]
        relationships = [
            ExtractedRelationship(**r) for r in analysis_data.get("relationships", [])
        ]

        logger.info(
            f"Analysis complete: {len(characters)} characters, {len(dialogues)} dialogues, "
            f"{len(scenes)} scenes, {len(relationships)} relationships"
        )

        return ScriptAnalysisResponse(
            characters=characters,
            dialogues=dialogues,
            scenes=scenes,
            relationships=relationships,
        )

    def _generate_empty_analysis(self) -> ScriptAnalysisResponse:
        """Return empty analysis structure when LLM fails."""
        return ScriptAnalysisResponse(
            characters=[],
            dialogues=[],
            scenes=[],
            relationships=[],
        )
