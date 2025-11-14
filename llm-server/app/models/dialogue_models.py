"""
대사 생성 관련 Pydantic 모델
"""

from pydantic import BaseModel, Field, field_validator, ConfigDict
from typing import List, Optional
import re


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
    speakerId: str = Field(..., min_length=1, max_length=100, description="Speaker character ID")
    targetIds: List[str] = Field(..., description="Target character IDs")
    intent: str = Field(..., min_length=1, max_length=50, description="Intent: reconcile, argue, comfort, etc.")
    honorific: str = Field(..., description="Speech level: banmal, jondae, mixed")
    maxLen: int = Field(default=80, ge=10, le=300, description="Max line length")
    nCandidates: int = Field(default=3, ge=1, le=10, description="Number of candidates")
    characterInfo: Optional[CharacterInfo] = Field(None, description="Speaker character info")
    targetNames: Optional[List[str]] = Field(None, description="Target character names")
    context: Optional[str] = Field(None, max_length=2000, description="Additional context")
    provider: Optional[str] = Field(None, description="LLM provider (openai, claude, gemini)")

    @field_validator("intent")
    @classmethod
    def validate_intent(cls, v: str) -> str:
        """Intent는 영문 소문자와 언더스코어만 허용"""
        if not re.match(r"^[a-z_]+$", v):
            raise ValueError("Intent must contain only lowercase letters and underscores")
        return v

    @field_validator("honorific")
    @classmethod
    def validate_honorific(cls, v: str) -> str:
        """Honorific은 허용된 값만"""
        allowed = {"banmal", "jondae", "mixed", "formal"}
        if v not in allowed:
            raise ValueError(f"Honorific must be one of: {', '.join(allowed)}")
        return v

    @field_validator("context")
    @classmethod
    def sanitize_context(cls, v: Optional[str]) -> Optional[str]:
        """Context에서 위험한 패턴 검증"""
        if v is None:
            return v

        # XSS, Script injection 방지
        dangerous_patterns = [
            r"<script[^>]*>.*?</script>",  # <script> 태그
            r"javascript:",  # javascript: 프로토콜
            r"on\w+\s*=",  # onclick, onerror 등 이벤트 핸들러
            r"<iframe[^>]*>",  # iframe 태그
            r"eval\s*\(",  # eval 함수
            r"expression\s*\(",  # CSS expression
        ]

        for pattern in dangerous_patterns:
            if re.search(pattern, v, re.IGNORECASE):
                raise ValueError("Potentially malicious input detected")

        return v

    @field_validator("provider")
    @classmethod
    def validate_provider(cls, v: Optional[str]) -> Optional[str]:
        """Provider는 허용된 값만"""
        if v is None:
            return v

        allowed = {"openai", "claude", "gemini"}
        if v not in allowed:
            raise ValueError(f"Provider must be one of: {', '.join(allowed)}")
        return v

    @field_validator("targetIds")
    @classmethod
    def validate_target_ids(cls, v: List[str]) -> List[str]:
        """TargetIds 검증"""
        if not v:
            raise ValueError("At least one target ID is required")
        if len(v) > 10:
            raise ValueError("Too many target IDs (max 10)")
        for target_id in v:
            if not target_id or len(target_id) > 100:
                raise ValueError("Invalid target ID")
        return v


class Candidate(BaseModel):
    """대사 후보"""
    text: str = Field(..., description="Candidate line")
    score: float = Field(..., description="Confidence score (0.0~1.0)")


class SuggestResponse(BaseModel):
    """대사 제안 응답"""
    candidates: List[Candidate]
