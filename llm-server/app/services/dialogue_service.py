"""
대사 생성 서비스
캐릭터 페르소나 기반 대사 제안 생성
"""

import logging
import re
from typing import List
from app.core.llm_provider_manager import LLMProviderManager
from app.services.prompt_builder import PromptBuilder
from app.models.dialogue_models import SuggestInput, Candidate, SuggestResponse
from app.utils.error_handlers import with_llm_fallback

logger = logging.getLogger(__name__)


class DialogueService:
    """대사 생성 서비스"""

    def __init__(self, llm_manager: LLMProviderManager):
        self.llm_manager = llm_manager

    @with_llm_fallback(fallback_func=lambda self, inp: self._generate_fallback_response(inp))
    def generate_dialogue_suggestions(self, inp: SuggestInput) -> SuggestResponse:
        """
        대사 제안 생성

        Args:
            inp: 대사 생성 입력

        Returns:
            대사 후보 목록
        """
        logger.info(
            f"Generating dialogue for speaker={inp.speakerId}, intent={inp.intent}, "
            f"provider={inp.provider or self.llm_manager.default_provider}"
        )

        if not inp.characterInfo:
            logger.warning("No character info provided, using fallback templates")
            return self._generate_fallback_response(inp)

        character_dict = inp.characterInfo.model_dump()
        target_names = inp.targetNames or inp.targetIds

        # 프롬프트 빌드
        system_prompt, user_prompt = PromptBuilder.build_full_prompt(
            character_info=character_dict,
            intent=inp.intent,
            honorific=inp.honorific,
            target_names=target_names,
            max_len=inp.maxLen,
            n_candidates=inp.nCandidates,
            context=inp.context,
        )

        logger.info(
            f"Prompt sizes: system={len(system_prompt)} chars, user={len(user_prompt)} chars"
        )

        # LLM 호출
        generated_text = self.llm_manager.generate(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            n_candidates=inp.nCandidates,
            provider=inp.provider,
        )

        # 대사 파싱
        dialogues = self._parse_dialogues(generated_text, inp.nCandidates)

        logger.info(f"Generated {len(dialogues)} dialogues")

        # Candidate 객체 생성
        candidates: List[Candidate] = []
        for i, text in enumerate(dialogues):
            if len(text) > inp.maxLen:
                text = text[: inp.maxLen - 3] + "..."
            score = max(0.0, min(1.0, 0.95 - (i * 0.05)))
            candidates.append(Candidate(text=text, score=round(score, 2)))

        if not candidates:
            logger.warning("No candidates generated, using fallback")
            return self._generate_fallback_response(inp)

        return SuggestResponse(candidates=candidates)

    def _parse_dialogues(self, content: str, n_candidates: int) -> List[str]:
        """
        LLM 응답을 파싱하여 대사 목록으로 변환

        Args:
            content: LLM 응답 텍스트
            n_candidates: 필요한 후보 개수

        Returns:
            파싱된 대사 목록
        """
        logger.debug(f"Parsing dialogues: content_length={len(content)}, n_candidates={n_candidates}")

        # 줄바꿈으로 구분된 대사들을 분리
        dialogues = [line.strip() for line in content.split('\n') if line.strip()]
        logger.debug(f"Split into {len(dialogues)} lines")

        # 번호나 기호 제거
        cleaned_dialogues = []
        for idx, dialogue in enumerate(dialogues):
            # 숫자, 점, 대시, 불릿 포인트 제거
            cleaned = re.sub(r'^\d+[\.\)]\s*', '', dialogue)  # "1. " or "1) "
            cleaned = re.sub(r'^[-•\*]\s*', '', cleaned)      # "- " or "• " or "* "
            cleaned = re.sub(r'^["\']|["\']$', '', cleaned)    # 앞뒤 따옴표 제거
            if cleaned:
                cleaned_dialogues.append(cleaned)
                logger.debug(f"Cleaned dialogue {idx}: {cleaned[:50]}...")

        logger.info(f"Parsed {len(cleaned_dialogues)} dialogues from {len(dialogues)} lines")

        # 요청한 개수만큼 반환 (부족하면 있는 만큼)
        if not cleaned_dialogues:
            logger.warning("No dialogues parsed, using fallback")
            return self._generate_fallback_dialogues(n_candidates)

        return cleaned_dialogues[:n_candidates]

    def _generate_fallback_response(self, inp: SuggestInput) -> SuggestResponse:
        """Fallback suggestions when LLM is unavailable or fails."""
        intent_templates = {
            "reconcile": [
                "Sorry about earlier.",
                "Can we start over?",
                "I hope we can move on.",
            ],
            "argue": [
                "I don't agree with that.",
                "That doesn't sound right.",
                "Let's think this through.",
            ],
            "comfort": [
                "It's going to be okay.",
                "I'm here for you.",
                "Take it one step at a time.",
            ],
            "greet": ["Hey!", "Long time no see!", "How have you been?"],
            "thank": ["Thanks!", "I really appreciate it.", "You're the best."],
        }

        templates = intent_templates.get(
            inp.intent, ["Got it.", "Okay.", "Understood."]
        )

        if inp.honorific == "jondae":
            templates = [
                t if t.endswith(("yo", "umnida")) else (t + " please") for t in templates
            ]

        candidates: List[Candidate] = []
        for i in range(min(inp.nCandidates, len(templates))):
            text = templates[i]
            if len(text) > inp.maxLen:
                text = text[: inp.maxLen - 3] + "..."
            score = max(0.0, min(1.0, 0.85 - (i * 0.05)))
            candidates.append(Candidate(text=text, score=round(score, 2)))

        if not candidates:
            candidates.append(
                Candidate(
                    text=f"{inp.speakerId}: {inp.intent} ({inp.honorific})", score=0.75
                )
            )

        return SuggestResponse(candidates=candidates)

    def _generate_fallback_dialogues(self, n_candidates: int) -> List[str]:
        """Fallback dialogues when parsing fails."""
        fallback_dialogues = [
            "안녕? 오랜만이야!",
            "어, 안녕! 잘 지냈어?",
            "야! 여기서 뭐해?",
        ]
        return fallback_dialogues[:n_candidates]
