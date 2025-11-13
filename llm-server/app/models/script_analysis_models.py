"""
스크립트 분석 관련 Pydantic 모델
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class ScriptAnalysisInput(BaseModel):
    """스크립트 분석 입력"""
    content: str = Field(..., description="Script content to analyze")
    formatHint: Optional[str] = Field(None, description="Format hint: novel, scenario, description")
    provider: str = Field("openai", description="LLM provider")


class ExtractedCharacter(BaseModel):
    """추출된 캐릭터"""
    name: str
    description: str = ""
    personality: str = ""
    speakingStyle: str = ""
    dialogueExamples: List[str] = []


class ExtractedDialogue(BaseModel):
    """추출된 대사"""
    characterName: str
    text: str
    sceneNumber: int = 1


class ExtractedScene(BaseModel):
    """추출된 장면"""
    sceneNumber: int
    location: str = ""
    mood: str = ""
    description: str = ""
    participants: List[str] = []


class ExtractedRelationship(BaseModel):
    """추출된 관계"""
    fromCharacter: str
    toCharacter: str
    relationType: str
    closeness: float = 5.0
    description: str = ""


class ScriptAnalysisResponse(BaseModel):
    """스크립트 분석 응답"""
    characters: List[ExtractedCharacter]
    dialogues: List[ExtractedDialogue]
    scenes: List[ExtractedScene]
    relationships: List[ExtractedRelationship]
