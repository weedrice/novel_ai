"""
시나리오 생성 서비스
다중 캐릭터 대화 시나리오 자동 생성
"""

import logging
from typing import List
from app.core.llm_provider_manager import LLMProviderManager
from app.utils.prompt_templates import PromptTemplates
from app.models.scenario_models import (
    ScenarioInput,
    ScenarioResponse,
    DialogueItem,
    ParticipantInfo
)

logger = logging.getLogger(__name__)


class ScenarioService:
    """시나리오 생성 서비스"""

    def __init__(self, llm_manager: LLMProviderManager):
        self.llm_manager = llm_manager

    def generate_scenario(self, inp: ScenarioInput) -> ScenarioResponse:
        """
        시나리오 생성

        Args:
            inp: 시나리오 생성 입력

        Returns:
            대화 시나리오
        """
        logger.info(
            f"Generating scenario with {len(inp.participants)} participants, "
            f"{inp.dialogueCount} lines"
        )

        try:
            # 참여자 정보 문자열 생성
            participants_desc = "\n".join([
                f"- {p.name} ({p.characterId}): {p.personality}, style: {p.speakingStyle}"
                for p in inp.participants
            ])

            # 프롬프트 생성
            system_prompt = PromptTemplates.scenario_system_prompt(
                inp.sceneDescription,
                inp.location,
                inp.mood,
                participants_desc
            )
            user_prompt = PromptTemplates.scenario_user_prompt(inp.dialogueCount)

            logger.info(
                f"Scenario prompts: system={len(system_prompt)} chars, "
                f"user={len(user_prompt)} chars"
            )

            # LLM 호출
            generated_text = self.llm_manager.generate(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                max_tokens=500,
                temperature=0.8,
                provider=inp.provider,
            )

            # 대화 파싱
            dialogues = self._parse_scenario_dialogues(generated_text, inp.participants)

            if not dialogues:
                logger.warning("No dialogues generated, using fallback scenario")
                dialogues = self._generate_fallback_scenario(inp)

            return ScenarioResponse(dialogues=dialogues[: inp.dialogueCount])

        except Exception as e:
            logger.error(f"Error generating scenario: {e}")
            dialogues = self._generate_fallback_scenario(inp)
            return ScenarioResponse(dialogues=dialogues[: inp.dialogueCount])

    def _parse_scenario_dialogues(
        self,
        text: str,
        participants: List[ParticipantInfo]
    ) -> List[DialogueItem]:
        """
        LLM 응답을 파싱하여 대화 목록 생성

        Args:
            text: LLM 응답 텍스트
            participants: 참여자 목록

        Returns:
            대화 항목 목록
        """
        dialogues: List[DialogueItem] = []
        lines = [ln.strip() for ln in text.strip().split("\n") if ln.strip()]

        name_to_char = {p.name: p for p in participants}

        order = 1
        for line in lines:
            if ":" not in line or line.startswith(("#", "//")):
                continue

            speaker_part, text_part = line.split(":", 1)
            speaker_part = speaker_part.strip().strip("[]")
            text_part = text_part.strip()

            character = None
            for name, char in name_to_char.items():
                if name.lower() in speaker_part.lower():
                    character = char
                    break

            if character and text_part:
                dialogues.append(
                    DialogueItem(
                        speaker=character.name,
                        characterId=character.characterId,
                        text=text_part,
                        order=order,
                    )
                )
                order += 1

        return dialogues

    def _generate_fallback_scenario(self, inp: ScenarioInput) -> List[DialogueItem]:
        """Fallback 시나리오 생성"""
        dialogues: List[DialogueItem] = []
        templates = [
            "Hey, how have you been?",
            "I've been okay. Busy lately.",
            "Same here. Want to catch up soon?",
            "Sure. How about this weekend?",
            "Sounds good. Let's text details later.",
        ]

        for i in range(min(inp.dialogueCount, len(templates))):
            participant = inp.participants[i % len(inp.participants)]
            dialogues.append(
                DialogueItem(
                    speaker=participant.name,
                    characterId=participant.characterId,
                    text=templates[i],
                    order=i + 1,
                )
            )

        return dialogues
