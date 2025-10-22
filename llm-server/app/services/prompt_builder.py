"""
Prompt builder utilities for the LLM server.
Builds system and user prompts from character info and request context.
"""

from typing import Dict, List, Optional, Tuple
import logging

logger = logging.getLogger(__name__)


class PromptBuilder:
    """Prompt construction from character persona and request intent."""

    @staticmethod
    def build_system_prompt(character_info: Dict) -> str:
        """
        Build a system prompt using character information.

        Args:
            character_info: Dict with fields like name, description, personality,
                speakingStyle, vocabulary, toneKeywords, examples,
                prohibitedWords, sentencePatterns

        Returns:
            System prompt string
        """
        logger.debug(f"Building system prompt for character: {character_info.get('name', 'Unknown')}")

        name = character_info.get("name", "Character")
        description = character_info.get("description", "")
        personality = character_info.get("personality", "")
        speaking_style = character_info.get("speakingStyle", "")
        vocabulary = character_info.get("vocabulary", "")
        tone_keywords = character_info.get("toneKeywords", "")
        examples = character_info.get("examples", "")
        prohibited_words = character_info.get("prohibitedWords", "")
        sentence_patterns = character_info.get("sentencePatterns", "")

        prompt = f"""
You are an AI that generates short, natural-sounding dialogue lines for the character below.

## Character Profile
- Name: {name}
- Description: {description}
- Personality: {personality}
- Speaking style: {speaking_style}
"""

        if vocabulary:
            prompt += f"- Common vocabulary: {vocabulary}\n"

        if tone_keywords:
            prompt += f"- Tone keywords: {tone_keywords}\n"

        if sentence_patterns:
            prompt += "\n## Sentence Patterns\n"
            for pattern in sentence_patterns.split("\n"):
                if pattern.strip():
                    prompt += f"- {pattern.strip()}\n"

        if prohibited_words:
            prompt += "\n## Prohibited Words/Expressions\n"
            prompt += (
                "Avoid using the following words/phrases in outputs: "
                f"{prohibited_words}\n"
            )

        if examples:
            prompt += "\n## Example Lines\n"
            for example in examples.split("\n"):
                if example.strip():
                    prompt += f'- "{example.strip()}"\n'

        prompt += """

## Output Rules
1. Reflect the character's personality and speaking style.
2. Prefer the character's common vocabulary when natural.
3. Follow sentence patterns when given.
4. Do not use prohibited expressions.
5. Keep outputs short and conversational.
6. Respect the requested honorific style.
"""

        result = prompt.strip()
        logger.debug(f"System prompt built: {len(result)} chars")
        return result

    @staticmethod
    def build_user_prompt(
        intent: str,
        honorific: str,
        target_names: List[str],
        max_len: int,
        n_candidates: int,
        context: Optional[str] = None,
    ) -> str:
        """
        Build a user prompt describing intent and constraints.

        Args:
            intent: dialogue intent (e.g., reconcile, argue, comfort, greet)
            honorific: speech level (banmal, jondae, mixed)
            target_names: names of target interlocutors
            max_len: maximum length for each line
            n_candidates: number of lines to generate
            context: optional additional context

        Returns:
            User prompt string
        """
        logger.debug(f"Building user prompt: intent={intent}, honorific={honorific}, "
                    f"targets={target_names}, max_len={max_len}, n_candidates={n_candidates}")

        target_str = ", ".join(target_names) if target_names else "unknown"

        prompt = f"""
Generate {n_candidates} short candidate lines that satisfy the conditions below.

## Conditions
- Targets: {target_str}
- Intent: {intent}
- Honorific: {honorific}
- Max length: {max_len} characters
"""

        if context:
            prompt += f"- Context: {context}\n"

        prompt += """

## Output Format
Return one line per candidate, without numbering or bullets.
Example:
Hi! Long time no see.
How have you been?
I missed you a lot.

Now generate the candidates:
"""

        result = prompt.strip()
        logger.debug(f"User prompt built: {len(result)} chars")
        return result

    @staticmethod
    def build_full_prompt(
        character_info: Dict,
        intent: str,
        honorific: str,
        target_names: List[str],
        max_len: int,
        n_candidates: int,
        context: Optional[str] = None,
    ) -> Tuple[str, str]:
        """
        Build both system and user prompts.

        Returns:
            Tuple of (system_prompt, user_prompt)
        """
        logger.info(f"Building full prompt: character={character_info.get('name', 'Unknown')}, "
                   f"intent={intent}, honorific={honorific}")

        system_prompt = PromptBuilder.build_system_prompt(character_info)
        user_prompt = PromptBuilder.build_user_prompt(
            intent=intent,
            honorific=honorific,
            target_names=target_names,
            max_len=max_len,
            n_candidates=n_candidates,
            context=context,
        )

        logger.info(f"Full prompt built: system={len(system_prompt)} chars, user={len(user_prompt)} chars")
        return system_prompt, user_prompt

