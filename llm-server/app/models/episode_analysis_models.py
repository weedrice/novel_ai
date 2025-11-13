"""
에피소드 분석 관련 Pydantic 모델
"""

from pydantic import BaseModel, Field
from typing import List, Optional
from app.models.script_analysis_models import (
    ExtractedCharacter,
    ExtractedDialogue,
    ExtractedScene
)


class EpisodeAnalysisInput(BaseModel):
    """에피소드 분석 입력"""
    scriptText: str = Field(..., description="Episode script text")
    scriptFormat: Optional[str] = Field(None, description="Format: novel, scenario, description, dialogue")
    provider: str = Field("openai", description="LLM provider")


class SummaryResponse(BaseModel):
    """요약 응답"""
    summary: str
    keyPoints: List[str] = []
    wordCount: int = 0


class CharacterAnalysisResponse(BaseModel):
    """캐릭터 분석 응답"""
    characters: List[ExtractedCharacter]


class SceneAnalysisResponse(BaseModel):
    """장면 분석 응답"""
    scenes: List[ExtractedScene]


class DialogueAnalysisResponse(BaseModel):
    """대사 분석 응답"""
    dialogues: List[ExtractedDialogue]
    statistics: dict = {}


class SpellCheckIssue(BaseModel):
    """맞춤법 검사 이슈"""
    type: str  # spelling, grammar, punctuation, style
    original: str
    suggestion: str
    position: int = 0
    description: str = ""


class SpellCheckResponse(BaseModel):
    """맞춤법 검사 응답"""
    issues: List[SpellCheckIssue]
    summary: dict = {}
