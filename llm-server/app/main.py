from fastapi import FastAPI, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field
from typing import List, Optional
from dotenv import load_dotenv
import logging
import json

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


# ============================================================
# Episode Analysis Endpoints
# ============================================================

class EpisodeAnalysisInput(BaseModel):
    scriptText: str = Field(..., description="Episode script text")
    scriptFormat: Optional[str] = Field(None, description="Format: novel, scenario, description, dialogue")
    provider: str = Field("openai", description="LLM provider")


class SummaryResponse(BaseModel):
    summary: str
    keyPoints: List[str] = []
    wordCount: int = 0


@app.post("/gen/episode/summary", response_model=SummaryResponse)
async def generate_summary(inp: EpisodeAnalysisInput) -> SummaryResponse:
    """Generate AI summary of the episode script."""
    logger.info(f"Generating summary: length={len(inp.scriptText)} chars, provider={inp.provider}")

    try:
        system_prompt = """
당신은 전문 작가이자 편집자입니다. 제공된 스크립트를 분석하여 간결하고 명확한 요약을 작성하세요.

요약 시 다음을 포함하세요:
1. 전체적인 줄거리 (2-3문장)
2. 주요 사건 및 전개
3. 핵심 주제나 메시지

JSON 형식으로 응답하세요:
{
  "summary": "전체 요약 (150-300자)",
  "keyPoints": ["핵심 포인트 1", "핵심 포인트 2", "핵심 포인트 3"],
  "wordCount": 글자수
}
"""

        format_hint = f"형식: {inp.scriptFormat}" if inp.scriptFormat else ""
        user_prompt = f"""
{format_hint}
다음 스크립트를 요약해주세요:

{inp.scriptText}

JSON 형식으로만 응답하세요.
"""

        generated = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=1000,
            temperature=0.3,
            n_candidates=1,
            provider=inp.provider,
        )

        if not generated or not generated[0]:
            return SummaryResponse(
                summary="요약을 생성할 수 없습니다.",
                keyPoints=[],
                wordCount=len(inp.scriptText)
            )

        response_text = generated[0].strip()

        # Extract JSON from markdown code blocks
        if "```json" in response_text:
            response_text = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            response_text = response_text.split("```")[1].split("```")[0].strip()

        data = json.loads(response_text)

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


class CharacterAnalysisResponse(BaseModel):
    characters: List[ExtractedCharacter]


@app.post("/gen/episode/characters", response_model=CharacterAnalysisResponse)
async def analyze_characters(inp: EpisodeAnalysisInput) -> CharacterAnalysisResponse:
    """Extract and analyze characters from the episode script."""
    logger.info(f"Analyzing characters: length={len(inp.scriptText)} chars, provider={inp.provider}")

    try:
        system_prompt = """
당신은 캐릭터 분석 전문가입니다. 스크립트에서 등장하는 모든 캐릭터를 추출하고 분석하세요.

각 캐릭터에 대해:
1. 이름
2. 외모나 배경 설명
3. 성격 특성
4. 말투/화법 특징
5. 대사 예시 (실제 대사에서 추출)

JSON 형식으로 응답하세요:
{
  "characters": [
    {
      "name": "캐릭터 이름",
      "description": "간단한 설명",
      "personality": "성격 특성",
      "speakingStyle": "말투 특징",
      "dialogueExamples": ["대사 예시 1", "대사 예시 2"]
    }
  ]
}
"""

        format_hint = f"형식: {inp.scriptFormat}" if inp.scriptFormat else ""
        user_prompt = f"""
{format_hint}
다음 스크립트에서 캐릭터를 추출하고 분석해주세요:

{inp.scriptText}

JSON 형식으로만 응답하세요.
"""

        generated = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=1500,
            temperature=0.3,
            n_candidates=1,
            provider=inp.provider,
        )

        if not generated or not generated[0]:
            return CharacterAnalysisResponse(characters=[])

        response_text = generated[0].strip()

        # Extract JSON from markdown code blocks
        if "```json" in response_text:
            response_text = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            response_text = response_text.split("```")[1].split("```")[0].strip()

        data = json.loads(response_text)
        characters = [ExtractedCharacter(**c) for c in data.get("characters", [])]

        logger.info(f"Extracted {len(characters)} characters")
        return CharacterAnalysisResponse(characters=characters)

    except Exception as e:
        logger.error(f"Error analyzing characters: {e}")
        return CharacterAnalysisResponse(characters=[])


class SceneAnalysisResponse(BaseModel):
    scenes: List[ExtractedScene]


@app.post("/gen/episode/scenes", response_model=SceneAnalysisResponse)
async def analyze_scenes(inp: EpisodeAnalysisInput) -> SceneAnalysisResponse:
    """Extract scenes from the episode script."""
    logger.info(f"Analyzing scenes: length={len(inp.scriptText)} chars, provider={inp.provider}")

    try:
        system_prompt = """
당신은 장면 분석 전문가입니다. 스크립트를 장면 단위로 나누고 각 장면을 분석하세요.

각 장면에 대해:
1. 장면 번호 (1부터 시작)
2. 장소/배경
3. 분위기
4. 장면 설명
5. 참여 캐릭터들

장면 구분 기준:
- 시간이나 장소가 바뀔 때
- 등장인물이 크게 바뀔 때
- 주요 사건이 발생할 때

JSON 형식으로 응답하세요:
{
  "scenes": [
    {
      "sceneNumber": 1,
      "location": "장소",
      "mood": "분위기",
      "description": "장면 설명",
      "participants": ["캐릭터1", "캐릭터2"]
    }
  ]
}
"""

        format_hint = f"형식: {inp.scriptFormat}" if inp.scriptFormat else ""
        user_prompt = f"""
{format_hint}
다음 스크립트를 장면 단위로 분석해주세요:

{inp.scriptText}

JSON 형식으로만 응답하세요.
"""

        generated = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=1500,
            temperature=0.3,
            n_candidates=1,
            provider=inp.provider,
        )

        if not generated or not generated[0]:
            return SceneAnalysisResponse(scenes=[])

        response_text = generated[0].strip()

        # Extract JSON from markdown code blocks
        if "```json" in response_text:
            response_text = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            response_text = response_text.split("```")[1].split("```")[0].strip()

        data = json.loads(response_text)
        scenes = [ExtractedScene(**s) for s in data.get("scenes", [])]

        logger.info(f"Extracted {len(scenes)} scenes")
        return SceneAnalysisResponse(scenes=scenes)

    except Exception as e:
        logger.error(f"Error analyzing scenes: {e}")
        return SceneAnalysisResponse(scenes=[])


class DialogueAnalysisResponse(BaseModel):
    dialogues: List[ExtractedDialogue]
    statistics: dict = {}


@app.post("/gen/episode/dialogues", response_model=DialogueAnalysisResponse)
async def analyze_dialogues(inp: EpisodeAnalysisInput) -> DialogueAnalysisResponse:
    """Extract and analyze dialogues from the episode script."""
    logger.info(f"Analyzing dialogues: length={len(inp.scriptText)} chars, provider={inp.provider}")

    try:
        system_prompt = """
당신은 대사 분석 전문가입니다. 스크립트에서 모든 대사를 추출하고 분석하세요.

각 대사에 대해:
1. 화자 이름
2. 대사 내용
3. 장면 번호 (추정)

추가로 대사 통계도 제공하세요:
- 총 대사 수
- 캐릭터별 대사 수
- 평균 대사 길이

JSON 형식으로 응답하세요:
{
  "dialogues": [
    {
      "characterName": "화자 이름",
      "text": "대사 내용",
      "sceneNumber": 1
    }
  ],
  "statistics": {
    "totalCount": 10,
    "byCharacter": {"캐릭터1": 5, "캐릭터2": 5},
    "averageLength": 30
  }
}
"""

        format_hint = f"형식: {inp.scriptFormat}" if inp.scriptFormat else ""
        user_prompt = f"""
{format_hint}
다음 스크립트에서 대사를 추출하고 분석해주세요:

{inp.scriptText}

JSON 형식으로만 응답하세요.
"""

        generated = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=2000,
            temperature=0.3,
            n_candidates=1,
            provider=inp.provider,
        )

        if not generated or not generated[0]:
            return DialogueAnalysisResponse(dialogues=[], statistics={})

        response_text = generated[0].strip()

        # Extract JSON from markdown code blocks
        if "```json" in response_text:
            response_text = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            response_text = response_text.split("```")[1].split("```")[0].strip()

        data = json.loads(response_text)
        dialogues = [ExtractedDialogue(**d) for d in data.get("dialogues", [])]
        statistics = data.get("statistics", {})

        logger.info(f"Extracted {len(dialogues)} dialogues")
        return DialogueAnalysisResponse(dialogues=dialogues, statistics=statistics)

    except Exception as e:
        logger.error(f"Error analyzing dialogues: {e}")
        return DialogueAnalysisResponse(dialogues=[], statistics={})


class SpellCheckIssue(BaseModel):
    type: str  # spelling, grammar, punctuation, style
    original: str
    suggestion: str
    position: int = 0
    description: str = ""


class SpellCheckResponse(BaseModel):
    issues: List[SpellCheckIssue]
    summary: dict = {}


@app.post("/gen/episode/spell-check", response_model=SpellCheckResponse)
async def spell_check(inp: EpisodeAnalysisInput) -> SpellCheckResponse:
    """Check spelling and grammar in the episode script."""
    logger.info(f"Spell checking: length={len(inp.scriptText)} chars, provider={inp.provider}")

    try:
        system_prompt = """
당신은 맞춤법 및 문법 검사 전문가입니다. 제공된 텍스트의 맞춤법, 문법, 띄어쓰기, 문체 오류를 찾아주세요.

각 오류에 대해:
1. 오류 타입 (spelling/grammar/punctuation/style)
2. 원본 텍스트
3. 수정 제안
4. 위치 (대략적인 문자 위치)
5. 설명

추가로 요약 정보도 제공하세요:
- 총 오류 수
- 타입별 오류 수
- 전반적인 평가

JSON 형식으로 응답하세요:
{
  "issues": [
    {
      "type": "spelling",
      "original": "잘못된 표현",
      "suggestion": "올바른 표현",
      "position": 100,
      "description": "설명"
    }
  ],
  "summary": {
    "totalIssues": 5,
    "byType": {"spelling": 2, "grammar": 3},
    "overallScore": 85
  }
}
"""

        format_hint = f"형식: {inp.scriptFormat}" if inp.scriptFormat else ""
        user_prompt = f"""
{format_hint}
다음 텍스트의 맞춤법과 문법을 검사해주세요:

{inp.scriptText}

JSON 형식으로만 응답하세요.
"""

        generated = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            max_tokens=2000,
            temperature=0.2,
            n_candidates=1,
            provider=inp.provider,
        )

        if not generated or not generated[0]:
            return SpellCheckResponse(issues=[], summary={})

        response_text = generated[0].strip()

        # Extract JSON from markdown code blocks
        if "```json" in response_text:
            response_text = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            response_text = response_text.split("```")[1].split("```")[0].strip()

        data = json.loads(response_text)
        issues = [SpellCheckIssue(**i) for i in data.get("issues", [])]
        summary = data.get("summary", {})

        logger.info(f"Found {len(issues)} spelling/grammar issues")
        return SpellCheckResponse(issues=issues, summary=summary)

    except Exception as e:
        logger.error(f"Error checking spelling: {e}")
        return SpellCheckResponse(issues=[], summary={})


# ============================================================
# Task 92: 스트리밍 응답 엔드포인트
# ============================================================

@app.post("/gen/suggest-stream")
async def gen_suggest_stream(request: Request, inp: SuggestInput = None):
    """
    스트리밍 방식으로 대사 제안 생성
    Server-Sent Events (SSE) 형식으로 실시간 응답 전송
    """
    logger.info(
        f"Generating streaming dialogue for speaker={inp.speakerId}, intent={inp.intent}, provider={inp.provider or llm_service.default_provider}"
    )

    async def event_generator():
        """SSE 형식의 이벤트 스트림 생성"""
        try:
            if not inp.characterInfo:
                logger.warning("No character info provided for streaming")
                yield f"data: {json.dumps({'error': 'Character info required'})}\n\n"
                return

            character_dict = inp.characterInfo.model_dump()
            target_names = inp.targetNames or inp.targetIds

            system_prompt, user_prompt = PromptBuilder.build_full_prompt(
                character_info=character_dict,
                intent=inp.intent,
                honorific=inp.honorific,
                target_names=target_names,
                max_len=inp.maxLen,
                n_candidates=1,  # 스트리밍은 단일 후보만 생성
                context=inp.context,
            )

            # 스트리밍 시작 이벤트
            yield f"data: {json.dumps({'type': 'start', 'message': 'Streaming started'})}\n\n"

            # LLM 스트리밍 호출
            async for chunk in llm_service.generate_dialogue_stream(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                provider=inp.provider,
            ):
                # 각 텍스트 청크를 SSE 형식으로 전송
                yield f"data: {json.dumps({'type': 'chunk', 'text': chunk})}\n\n"

            # 스트리밍 완료 이벤트
            yield f"data: {json.dumps({'type': 'done', 'message': 'Streaming completed'})}\n\n"

        except Exception as e:
            logger.error(f"Error during streaming: {e}")
            yield f"data: {json.dumps({'type': 'error', 'message': str(e)})}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",  # Nginx 버퍼링 비활성화
        }
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)

