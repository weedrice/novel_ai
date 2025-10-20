from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import List, Dict, Optional
import os
from dotenv import load_dotenv
import logging

from app.services.prompt_builder import PromptBuilder
from app.services.llm_service import LLMService

# 환경 변수 로드
load_dotenv()

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Character Tone LLM Server",
    description="AI-powered dialogue tone suggestion service",
    version="0.2.0"
)

# LLM 서비스 초기화
llm_service = LLMService()


class CharacterInfo(BaseModel):
    """캐릭터 정보 모델"""
    name: str = Field(..., description="캐릭터 이름")
    description: Optional[str] = Field(None, description="캐릭터 설명")
    personality: Optional[str] = Field(None, description="성격")
    speakingStyle: Optional[str] = Field(None, description="말투 특징")
    vocabulary: Optional[str] = Field(None, description="자주 사용하는 어휘")
    toneKeywords: Optional[str] = Field(None, description="말투 키워드")
    examples: Optional[str] = Field(None, description="대사 예시")
    prohibitedWords: Optional[str] = Field(None, description="사용하지 않는 단어")
    sentencePatterns: Optional[str] = Field(None, description="문장 패턴")


class SuggestInput(BaseModel):
    speakerId: str = Field(..., description="화자 캐릭터 ID")
    targetIds: List[str] = Field(..., description="대상 캐릭터 ID 목록")
    intent: str = Field(..., description="대화 의도 (e.g., reconcile, argue, comfort)")
    honorific: str = Field(..., description="존댓말 유형 (e.g., banmal, jondae)")
    maxLen: int = Field(default=80, description="최대 문장 길이")
    nCandidates: int = Field(default=3, ge=1, le=10, description="생성할 후보 개수")
    # 캐릭터 정보 추가
    characterInfo: Optional[CharacterInfo] = Field(None, description="화자 캐릭터 정보")
    targetNames: Optional[List[str]] = Field(None, description="대상 캐릭터 이름 목록")
    context: Optional[str] = Field(None, description="추가 컨텍스트")
    # LLM 프로바이더 선택
    provider: Optional[str] = Field(None, description="LLM 프로바이더 (openai, claude, gemini)")


class Candidate(BaseModel):
    text: str = Field(..., description="제안된 대사")
    score: float = Field(..., description="신뢰도 점수 (0.0 ~ 1.0)")


class SuggestResponse(BaseModel):
    candidates: List[Candidate] = Field(..., description="생성된 대사 후보 목록")


@app.get("/")
def root():
    return {"message": "Character Tone LLM Server is running", "version": "0.1.0"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/providers")
def get_providers():
    """
    사용 가능한 LLM 프로바이더 목록 조회

    Returns:
        사용 가능한 프로바이더 목록과 기본 프로바이더 정보
    """
    available = llm_service.get_available_providers()
    return {
        "available": available,
        "default": llm_service.default_provider,
        "providers": {
            "openai": {
                "name": "OpenAI GPT",
                "models": ["gpt-3.5-turbo", "gpt-4", "gpt-4-turbo"],
                "available": "openai" in available
            },
            "claude": {
                "name": "Anthropic Claude",
                "models": ["claude-3-haiku-20240307", "claude-3-sonnet-20240229", "claude-3-opus-20240229"],
                "available": "claude" in available
            },
            "gemini": {
                "name": "Google Gemini",
                "models": ["gemini-pro", "gemini-pro-vision"],
                "available": "gemini" in available
            }
        }
    }


@app.post("/gen/suggest", response_model=SuggestResponse)
def gen_suggest(inp: SuggestInput) -> SuggestResponse:
    """
    대사 톤 제안 API
    캐릭터 페르소나를 바탕으로 LLM을 사용하여 대사를 생성합니다.
    """
    logger.info(f"Generating dialogue for speaker: {inp.speakerId}, intent: {inp.intent}")

    # 캐릭터 정보가 없으면 더미 응답 반환
    if not inp.characterInfo:
        logger.warning("No character info provided, using fallback templates")
        return _generate_fallback_response(inp)

    try:
        # 캐릭터 정보를 딕셔너리로 변환
        character_dict = inp.characterInfo.model_dump()

        # 대상 캐릭터 이름 목록
        target_names = inp.targetNames or inp.targetIds

        # 프롬프트 생성
        system_prompt, user_prompt = PromptBuilder.build_full_prompt(
            character_info=character_dict,
            intent=inp.intent,
            honorific=inp.honorific,
            target_names=target_names,
            max_len=inp.maxLen,
            n_candidates=inp.nCandidates,
            context=inp.context
        )

        logger.info(f"System prompt length: {len(system_prompt)} chars")
        logger.info(f"User prompt length: {len(user_prompt)} chars")

        # LLM으로 대사 생성 (provider 지정)
        generated_dialogues = llm_service.generate_dialogue(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            n_candidates=inp.nCandidates,
            provider=inp.provider
        )

        logger.info(f"Generated {len(generated_dialogues)} dialogues")

        # 후보 목록 생성
        candidates = []
        for i, text in enumerate(generated_dialogues):
            # 최대 길이 제한
            if len(text) > inp.maxLen:
                text = text[:inp.maxLen - 3] + "..."

            # 점수 계산 (첫 번째가 가장 높음)
            score = 0.95 - (i * 0.05)
            candidates.append(Candidate(text=text, score=round(score, 2)))

        # 최소 1개는 반환
        if not candidates:
            logger.warning("No candidates generated, using fallback")
            return _generate_fallback_response(inp)

        return SuggestResponse(candidates=candidates)

    except Exception as e:
        logger.error(f"Error generating dialogue: {e}")
        return _generate_fallback_response(inp)


def _generate_fallback_response(inp: SuggestInput) -> SuggestResponse:
    """
    LLM 호출 실패 시 사용할 더미 응답 생성

    Args:
        inp: 요청 정보

    Returns:
        더미 응답
    """
    # 의도별 템플릿 예시
    intent_templates = {
        "reconcile": ["미안해, 내가 잘못했어", "다시 시작할 수 있을까?", "우리 화해하자"],
        "argue": ["그건 네가 틀렸어", "이해할 수 없어", "왜 그렇게 생각해?"],
        "comfort": ["괜찮아, 내가 있잖아", "너무 걱정하지 마", "다 잘 될 거야"],
        "greet": ["안녕!", "오랜만이야", "어떻게 지냈어?"],
        "thank": ["고마워", "정말 감사해", "네 덕분이야"],
    }

    # 존댓말 변환 (간단한 예시)
    templates = intent_templates.get(inp.intent, ["기본 대사 예시", "다른 대사", "또 다른 대사"])

    if inp.honorific == "jondae":
        templates = [t.replace("해", "해요").replace("야", "요").replace("어", "어요") for t in templates]

    # 후보 생성
    candidates = []
    for i in range(min(inp.nCandidates, len(templates))):
        text = templates[i]
        if len(text) > inp.maxLen:
            text = text[:inp.maxLen - 3] + "..."

        score = 0.85 - (i * 0.05)
        candidates.append(Candidate(text=text, score=round(score, 2)))

    if not candidates:
        candidates.append(
            Candidate(
                text=f"{inp.speakerId}: {inp.intent} ({inp.honorific})",
                score=0.75
            )
        )

    return SuggestResponse(candidates=candidates)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
