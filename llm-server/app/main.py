from fastapi import FastAPI, Request
from pydantic import BaseModel, Field
from typing import List, Optional
from dotenv import load_dotenv
import logging

from services.prompt_builder import PromptBuilder
from services.llm_service import LLMService

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
async def gen_suggest(request: Request, inp: SuggestInput = None) -> SuggestResponse:
    """Generate short dialogue suggestions based on persona and intent."""

    # Log raw request for debugging
    try:
        body = await request.body()
        logger.info(f"Raw request body: {body.decode('utf-8')[:500]}")
    except Exception as e:
        logger.error(f"Could not read request body: {e}")

    if inp is None:
        logger.error("Input is None!")
        return _generate_fallback_response(SuggestInput(
            speakerId="unknown",
            targetIds=[],
            intent="greet",
            honorific="banmal"
        ))

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


class ScriptAnalysisInput(BaseModel):
    content: str = Field(..., description="Script content to analyze")
    formatHint: Optional[str] = Field(None, description="Format hint: novel, scenario, description")
    provider: str = Field("openai", description="LLM provider")


class ExtractedCharacter(BaseModel):
    name: str
    description: str = ""
    personality: str = ""
    speakingStyle: str = ""
    dialogueExamples: List[str] = []


class ExtractedDialogue(BaseModel):
    characterName: str
    text: str
    sceneNumber: int = 1


class ExtractedScene(BaseModel):
    sceneNumber: int
    location: str = ""
    mood: str = ""
    description: str = ""
    participants: List[str] = []


class ExtractedRelationship(BaseModel):
    fromCharacter: str
    toCharacter: str
    relationType: str
    closeness: float = 5.0
    description: str = ""


class ScriptAnalysisResponse(BaseModel):
    characters: List[ExtractedCharacter]
    dialogues: List[ExtractedDialogue]
    scenes: List[ExtractedScene]
    relationships: List[ExtractedRelationship]


@app.post("/gen/analyze-script", response_model=ScriptAnalysisResponse)
async def analyze_script(inp: ScriptAnalysisInput) -> ScriptAnalysisResponse:
    """
    Analyze script content and extract structured information.
    Supports various formats: novels, scenarios, descriptions.
    """
    logger.info(
        f"Analyzing script: length={len(inp.content)} chars, format={inp.formatHint}, provider={inp.provider}"
    )

    try:
        system_prompt = _build_script_analysis_system_prompt()
        user_prompt = _build_script_analysis_user_prompt(inp.content, inp.formatHint)

        logger.info(
            f"Analysis prompts: system={len(system_prompt)} chars, user={len(user_prompt)} chars"
        )

        # Request JSON-structured response from LLM
        generated = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=2000,
            temperature=0.3,  # Lower temperature for more structured output
            n_candidates=1,
            provider=inp.provider,
        )

        if not generated or not generated[0]:
            logger.warning("No analysis result, returning empty structure")
            return _generate_empty_analysis()

        # Parse LLM response
        import json

        response_text = generated[0].strip()
        logger.info(f"Raw LLM response preview: {response_text[:500]}")

        # Extract JSON from markdown code blocks if present
        if "```json" in response_text:
            response_text = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            response_text = response_text.split("```")[1].split("```")[0].strip()

        analysis_data = json.loads(response_text)

        # Convert to Pydantic models
        characters = [ExtractedCharacter(**c) for c in analysis_data.get("characters", [])]
        dialogues = [ExtractedDialogue(**d) for d in analysis_data.get("dialogues", [])]
        scenes = [ExtractedScene(**s) for s in analysis_data.get("scenes", [])]
        relationships = [ExtractedRelationship(**r) for r in analysis_data.get("relationships", [])]

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

    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse LLM JSON response: {e}")
        logger.error(f"Response text: {response_text}")
        return _generate_empty_analysis()
    except Exception as e:
        logger.error(f"Error analyzing script: {e}")
        import traceback
        logger.error(traceback.format_exc())
        return _generate_empty_analysis()


def _build_script_analysis_system_prompt() -> str:
    return """
You are an expert script analyzer for storytelling content (novels, scenarios, screenplays).

Your task is to analyze the provided text and extract:
1. **Characters**: All characters mentioned with their traits
2. **Dialogues**: All spoken lines with speaker identification
3. **Scenes**: Scene divisions with location and mood
4. **Relationships**: Character relationships inferred from interactions

## Output Format
You MUST respond with ONLY valid JSON in this exact structure:

{
  "characters": [
    {
      "name": "Character name",
      "description": "Brief description",
      "personality": "Personality traits",
      "speakingStyle": "How they speak (formal, casual, etc.)",
      "dialogueExamples": ["Example line 1", "Example line 2"]
    }
  ],
  "dialogues": [
    {
      "characterName": "Speaker name",
      "text": "What they said",
      "sceneNumber": 1
    }
  ],
  "scenes": [
    {
      "sceneNumber": 1,
      "location": "Where it happens",
      "mood": "Mood/atmosphere",
      "description": "What happens in this scene",
      "participants": ["Character1", "Character2"]
    }
  ],
  "relationships": [
    {
      "fromCharacter": "Character1",
      "toCharacter": "Character2",
      "relationType": "friend/rival/family/etc",
      "closeness": 7.5,
      "description": "Nature of their relationship"
    }
  ]
}

## Rules
- Extract ALL characters, even if mentioned briefly
- For novels with narrative text, extract dialogue from quotation marks
- Infer scenes from context clues (location changes, time jumps)
- Estimate closeness on a scale of 0-10 based on interactions
- Use Korean for character names and content if the input is in Korean
- Return ONLY the JSON, no additional explanation
""".strip()


def _build_script_analysis_user_prompt(content: str, format_hint: Optional[str]) -> str:
    hint_text = f"Format hint: This appears to be a {format_hint}.\n" if format_hint else ""

    return f"""
{hint_text}
Please analyze the following script and extract characters, dialogues, scenes, and relationships in JSON format.

## Script Content:
{content}

Remember: Return ONLY valid JSON with the exact structure specified.
""".strip()


def _generate_empty_analysis() -> ScriptAnalysisResponse:
    """Return empty analysis structure when LLM fails."""
    return ScriptAnalysisResponse(
        characters=[],
        dialogues=[],
        scenes=[],
        relationships=[],
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)

