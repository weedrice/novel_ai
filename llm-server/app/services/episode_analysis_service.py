"""
에피소드 분석 서비스
에피소드별 요약, 캐릭터, 장면, 대사, 맞춤법 분석
"""

import logging
from app.core.llm_provider_manager import LLMProviderManager
from app.utils.json_parser import JSONParser
from app.utils.prompt_templates import PromptTemplates
from app.utils.error_handlers import with_error_handling
from app.models.episode_analysis_models import (
    EpisodeAnalysisInput,
    SummaryResponse,
    CharacterAnalysisResponse,
    SceneAnalysisResponse,
    DialogueAnalysisResponse,
    SpellCheckResponse,
    SpellCheckIssue
)
from app.models.script_analysis_models import (
    ExtractedCharacter,
    ExtractedScene,
    ExtractedDialogue
)

logger = logging.getLogger(__name__)


class EpisodeAnalysisService:
    """에피소드 분석 서비스"""

    def __init__(self, llm_manager: LLMProviderManager):
        self.llm_manager = llm_manager

    def generate_summary(self, inp: EpisodeAnalysisInput) -> SummaryResponse:
        """AI 요약 생성"""
        logger.info(f"Generating summary: length={len(inp.scriptText)} chars, provider={inp.provider}")

        try:
            system_prompt = PromptTemplates.EPISODE_SUMMARY_SYSTEM
            user_prompt = PromptTemplates.episode_summary_user_prompt(inp.scriptText, inp.scriptFormat)

            generated_text = self.llm_manager.generate(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                max_tokens=1000,
                temperature=0.3,
                provider=inp.provider,
            )

            if not generated_text:
                return SummaryResponse(
                    summary="요약을 생성할 수 없습니다.",
                    keyPoints=[],
                    wordCount=len(inp.scriptText)
                )

            data = JSONParser.parse_json_response(generated_text)
            if not data:
                return SummaryResponse(
                    summary="요약을 생성할 수 없습니다.",
                    keyPoints=[],
                    wordCount=len(inp.scriptText)
                )

            return SummaryResponse(
                summary=data.get("summary", ""),
                keyPoints=data.get("keyPoints", []),
                wordCount=data.get("wordCount", len(inp.scriptText))
            )

        except Exception as e:
            logger.error(f"Error generating summary: {e}")
            return SummaryResponse(
                summary=f"요약 생성 중 오류가 발생했습니다: {str(e)}",
                keyPoints=[],
                wordCount=len(inp.scriptText)
            )

    def analyze_characters(self, inp: EpisodeAnalysisInput) -> CharacterAnalysisResponse:
        """캐릭터 분석"""
        logger.info(f"Analyzing characters: length={len(inp.scriptText)} chars, provider={inp.provider}")

        try:
            system_prompt = PromptTemplates.CHARACTER_ANALYSIS_SYSTEM
            user_prompt = PromptTemplates.character_analysis_user_prompt(inp.scriptText, inp.scriptFormat)

            generated_text = self.llm_manager.generate(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                max_tokens=1500,
                temperature=0.3,
                provider=inp.provider,
            )

            if not generated_text:
                return CharacterAnalysisResponse(characters=[])

            data = JSONParser.parse_json_response(generated_text)
            if not data:
                return CharacterAnalysisResponse(characters=[])

            characters = [ExtractedCharacter(**c) for c in data.get("characters", [])]

            logger.info(f"Extracted {len(characters)} characters")
            return CharacterAnalysisResponse(characters=characters)

        except Exception as e:
            logger.error(f"Error analyzing characters: {e}")
            return CharacterAnalysisResponse(characters=[])

    def analyze_scenes(self, inp: EpisodeAnalysisInput) -> SceneAnalysisResponse:
        """장면 분석"""
        logger.info(f"Analyzing scenes: length={len(inp.scriptText)} chars, provider={inp.provider}")

        try:
            system_prompt = PromptTemplates.SCENE_ANALYSIS_SYSTEM
            user_prompt = PromptTemplates.scene_analysis_user_prompt(inp.scriptText, inp.scriptFormat)

            generated_text = self.llm_manager.generate(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                max_tokens=1500,
                temperature=0.3,
                provider=inp.provider,
            )

            if not generated_text:
                return SceneAnalysisResponse(scenes=[])

            data = JSONParser.parse_json_response(generated_text)
            if not data:
                return SceneAnalysisResponse(scenes=[])

            scenes = [ExtractedScene(**s) for s in data.get("scenes", [])]

            logger.info(f"Extracted {len(scenes)} scenes")
            return SceneAnalysisResponse(scenes=scenes)

        except Exception as e:
            logger.error(f"Error analyzing scenes: {e}")
            return SceneAnalysisResponse(scenes=[])

    def analyze_dialogues(self, inp: EpisodeAnalysisInput) -> DialogueAnalysisResponse:
        """대사 분석"""
        logger.info(f"Analyzing dialogues: length={len(inp.scriptText)} chars, provider={inp.provider}")

        try:
            system_prompt = PromptTemplates.DIALOGUE_ANALYSIS_SYSTEM
            user_prompt = PromptTemplates.dialogue_analysis_user_prompt(inp.scriptText, inp.scriptFormat)

            generated_text = self.llm_manager.generate(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                max_tokens=2000,
                temperature=0.3,
                provider=inp.provider,
            )

            if not generated_text:
                return DialogueAnalysisResponse(dialogues=[], statistics={})

            data = JSONParser.parse_json_response(generated_text)
            if not data:
                return DialogueAnalysisResponse(dialogues=[], statistics={})

            dialogues = [ExtractedDialogue(**d) for d in data.get("dialogues", [])]
            statistics = data.get("statistics", {})

            logger.info(f"Extracted {len(dialogues)} dialogues")
            return DialogueAnalysisResponse(dialogues=dialogues, statistics=statistics)

        except Exception as e:
            logger.error(f"Error analyzing dialogues: {e}")
            return DialogueAnalysisResponse(dialogues=[], statistics={})

    def spell_check(self, inp: EpisodeAnalysisInput) -> SpellCheckResponse:
        """맞춤법 검사"""
        logger.info(f"Spell checking: length={len(inp.scriptText)} chars, provider={inp.provider}")

        try:
            system_prompt = PromptTemplates.SPELL_CHECK_SYSTEM
            user_prompt = PromptTemplates.spell_check_user_prompt(inp.scriptText, inp.scriptFormat)

            generated_text = self.llm_manager.generate(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                max_tokens=2000,
                temperature=0.2,
                provider=inp.provider,
            )

            if not generated_text:
                return SpellCheckResponse(issues=[], summary={})

            data = JSONParser.parse_json_response(generated_text)
            if not data:
                return SpellCheckResponse(issues=[], summary={})

            issues = [SpellCheckIssue(**i) for i in data.get("issues", [])]
            summary = data.get("summary", {})

            logger.info(f"Found {len(issues)} spelling/grammar issues")
            return SpellCheckResponse(issues=issues, summary=summary)

        except Exception as e:
            logger.error(f"Error checking spelling: {e}")
            return SpellCheckResponse(issues=[], summary={})
