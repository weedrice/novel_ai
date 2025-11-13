"""
대사 생성 관련 Pydantic 모델
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class CharacterInfo(BaseModel):
    """캐릭터 정보"""
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
    """대사 제안 입력"""
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
    """대사 후보"""
    text: str = Field(..., description="Candidate line")
    score: float = Field(..., description="Confidence score (0.0~1.0)")


class SuggestResponse(BaseModel):
    """대사 제안 응답"""
    candidates: List[Candidate]
