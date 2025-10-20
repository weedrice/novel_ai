from fastapi import FastAPI, Request
from pydantic import BaseModel, Field
from typing import List, Optional
from dotenv import load_dotenv
import logging

from app.services.prompt_builder import PromptBuilder
from app.services.llm_service import LLMService

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Character Tone LLM Server",
    description="AI-powered dialogue tone suggestion service",
    version="0.2.0",
)

# Initialize LLM Service
llm_service = LLMService()


class CharacterInfo(BaseModel):
    name: str = Field(..., description="Character name")
    description: Optional[str] = Field(None, description="Character description")
    personality: Optional[str] = Field(None, description="Personality traits")
    speakingStyle: Optional[str] = Field(None, description="Speaking style")
    vocabulary: Optional[str] = Field(None, description="Common vocabulary")
    toneKeywords: Optional[str] = Field(None, description="Tone keywords")
    examples: Optional[str] = Field(None, description="Example lines")
    prohibitedWords: Optional[str] = Field(None, description="Prohibited words")
    sentencePatterns: Optional[str] = Field(None, description="Sentence patterns")


class SuggestInput(BaseModel):
    speakerId: str = Field(..., description="Speaker character ID")
    targetIds: List[str] = Field(..., description="Target character IDs")
    intent: str = Field(..., description="Intent: reconcile, argue, comfort, etc.")
    honorific: str = Field(..., description="Speech level: banmal, jondae, mixed")
    maxLen: int = Field(default=80, ge=10, le=300, description="Max line length")
    nCandidates: int = Field(default=3, ge=1, le=10, description="Number of candidates")
    characterInfo: Optional[CharacterInfo] = Field(None, description="Speaker character info")
    targetNames: Optional[List[str]] = Field(None, description="Target character names")
    context: Optional[str] = Field(None, description="Additional context")
    provider: Optional[str] = Field(None, description="LLM provider (openai, claude, gemini)")


class Candidate(BaseModel):
    text: str = Field(..., description="Candidate line")
    score: float = Field(..., description="Confidence score (0.0~1.0)")


class SuggestResponse(BaseModel):
    candidates: List[Candidate]


@app.get("/")
def root():
    return {"message": "Character Tone LLM Server is running", "version": "0.2.0"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/providers")
def get_providers():
    available = llm_service.get_available_providers()
    return {
        "available": available,
        "default": llm_service.default_provider,
        "providers": {
            "openai": {
                "name": "OpenAI GPT",
                "models": ["gpt-3.5-turbo", "gpt-4", "gpt-4-turbo"],
                "available": "openai" in available,
            },
            "claude": {
                "name": "Anthropic Claude",
                "models": [
                    "claude-3-haiku-20240307",
                    "claude-3-sonnet-20240229",
                    "claude-3-opus-20240229",
                ],
                "available": "claude" in available,
            },
            "gemini": {
                "name": "Google Gemini",
                "models": ["gemini-pro", "gemini-pro-vision"],
                "available": "gemini" in available,
            },
        },
    }


@app.post("/gen/suggest", response_model=SuggestResponse)
async def gen_suggest(request: Request) -> SuggestResponse:
    """Generate short dialogue suggestions based on persona and intent."""
    try:
        body = await request.body()
        logger.info(f"Received request body: {body.decode('utf-8', errors='ignore')}")
        import json

        data = json.loads(body)
        inp = SuggestInput(**data)
    except Exception as e:
        logger.error(f"Error parsing request: {e}")
        raise

    logger.info(
        f"Generating dialogue for speaker={inp.speakerId}, intent={inp.intent}, provider={inp.provider or llm_service.default_provider}"
    )

    if not inp.characterInfo:
        logger.warning("No character info provided, using fallback templates")
        return _generate_fallback_response(inp)

    try:
        character_dict = inp.characterInfo.model_dump()
        target_names = inp.targetNames or inp.targetIds

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

        generated_dialogues = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            n_candidates=inp.nCandidates,
            provider=inp.provider,
        )

        logger.info(f"Generated {len(generated_dialogues)} dialogues")

        candidates: List[Candidate] = []
        for i, text in enumerate(generated_dialogues):
            if len(text) > inp.maxLen:
                text = text[: inp.maxLen - 3] + "..."
            score = max(0.0, min(1.0, 0.95 - (i * 0.05)))
            candidates.append(Candidate(text=text, score=round(score, 2)))

        if not candidates:
            logger.warning("No candidates generated, using fallback")
            return _generate_fallback_response(inp)

        return SuggestResponse(candidates=candidates)

    except Exception as e:
        logger.error(f"Error generating dialogue: {e}")
        return _generate_fallback_response(inp)


def _generate_fallback_response(inp: SuggestInput) -> SuggestResponse:
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
        # Very naive politeness transform
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


class ParticipantInfo(BaseModel):
    characterId: str
    name: str
    personality: str = ""
    speakingStyle: str = ""


class ScenarioInput(BaseModel):
    sceneDescription: str = Field(..., description="Scene description")
    location: Optional[str] = Field(None, description="Location")
    mood: Optional[str] = Field(None, description="Mood")
    participants: List[ParticipantInfo] = Field(..., description="Participants")
    dialogueCount: int = Field(5, ge=1, le=50, description="Number of lines")
    provider: str = Field("openai", description="LLM provider")


class DialogueItem(BaseModel):
    speaker: str
    characterId: str
    text: str
    order: int


class ScenarioResponse(BaseModel):
    dialogues: List[DialogueItem]


@app.post("/gen/scenario", response_model=ScenarioResponse)
async def gen_scenario(inp: ScenarioInput) -> ScenarioResponse:
    """Generate a simple multi-participant dialogue scenario."""
    logger.info(
        f"Generating scenario with {len(inp.participants)} participants, {inp.dialogueCount} lines"
    )

    try:
        system_prompt = _build_scenario_system_prompt(inp)
        user_prompt = _build_scenario_user_prompt(inp)

        logger.info(
            f"Scenario prompts: system={len(system_prompt)} chars, user={len(user_prompt)} chars"
        )

        generated_texts = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=500,
            temperature=0.8,
            n_candidates=1,
            provider=inp.provider,
        )

        dialogues = _parse_scenario_dialogues(
            generated_texts[0] if generated_texts else "", inp.participants
        )

        if not dialogues:
            logger.warning("No dialogues generated, using fallback scenario")
            dialogues = _generate_fallback_scenario(inp)

        return ScenarioResponse(dialogues=dialogues[: inp.dialogueCount])

    except Exception as e:
        logger.error(f"Error generating scenario: {e}")
        dialogues = _generate_fallback_scenario(inp)
        return ScenarioResponse(dialogues=dialogues[: inp.dialogueCount])


def _build_scenario_system_prompt(inp: ScenarioInput) -> str:
    participants_desc = "\n".join(
        [
            f"- {p.name} ({p.characterId}): {p.personality}, style: {p.speakingStyle}"
            for p in inp.participants
        ]
    )

    prompt = f"""
You are a creative dialogue writer. Based on the scene and participants below, generate short back-and-forth lines that feel natural and reflect each character.

## Scene
- Location: {inp.location or 'N/A'}
- Mood: {inp.mood or 'neutral'}
- Description: {inp.sceneDescription}

## Participants
{participants_desc}

## Rules
1. Keep each line concise and conversational.
2. Alternate speakers naturally; avoid narration.
3. Reflect each character's personality and style.
4. Do not include numbering or brackets; just lines like "Name: text".
"""

    return prompt.strip()


def _build_scenario_user_prompt(inp: ScenarioInput) -> str:
    return (
        f"Generate {inp.dialogueCount} short lines in a conversation-like format.\n"
        "Use the format: Name: text"
    )


def _parse_scenario_dialogues(
    text: str, participants: List[ParticipantInfo]
) -> List[DialogueItem]:
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


def _generate_fallback_scenario(inp: ScenarioInput) -> List[DialogueItem]:
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


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)

