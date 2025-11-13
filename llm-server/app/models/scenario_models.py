"""
시나리오 생성 관련 Pydantic 모델
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class ParticipantInfo(BaseModel):
    """시나리오 참여자 정보"""
    characterId: str
    name: str
    personality: str = ""
    speakingStyle: str = ""


class ScenarioInput(BaseModel):
    """시나리오 생성 입력"""
    sceneDescription: str = Field(..., description="Scene description")
    location: Optional[str] = Field(None, description="Location")
    mood: Optional[str] = Field(None, description="Mood")
    participants: List[ParticipantInfo] = Field(..., description="Participants")
    dialogueCount: int = Field(5, ge=1, le=50, description="Number of lines")
    provider: str = Field("openai", description="LLM provider")


class DialogueItem(BaseModel):
    """대화 항목"""
    speaker: str
    characterId: str
    text: str
    order: int


class ScenarioResponse(BaseModel):
    """시나리오 생성 응답"""
    dialogues: List[DialogueItem]
