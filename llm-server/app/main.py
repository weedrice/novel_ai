from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import List, Dict

app = FastAPI(
    title="Character Tone LLM Server",
    description="AI-powered dialogue tone suggestion service",
    version="0.1.0"
)


class SuggestInput(BaseModel):
    speakerId: str = Field(..., description="화자 캐릭터 ID")
    targetIds: List[str] = Field(..., description="대상 캐릭터 ID 목록")
    intent: str = Field(..., description="대화 의도 (e.g., reconcile, argue, comfort)")
    honorific: str = Field(..., description="존댓말 유형 (e.g., banmal, jondae)")
    maxLen: int = Field(default=80, description="최대 문장 길이")
    nCandidates: int = Field(default=3, ge=1, le=10, description="생성할 후보 개수")


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


@app.post("/gen/suggest", response_model=SuggestResponse)
def gen_suggest(inp: SuggestInput) -> SuggestResponse:
    """
    대사 톤 제안 API (더미 구현)
    실제 구현 시 LLM 모델을 호출하여 대사를 생성합니다.
    """
    base_text = f"{inp.speakerId} → {', '.join(inp.targetIds)}"

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
        # 최대 길이 제한
        if len(text) > inp.maxLen:
            text = text[:inp.maxLen - 3] + "..."

        score = 0.85 - (i * 0.05)  # 첫 번째 후보가 가장 높은 점수
        candidates.append(Candidate(text=text, score=round(score, 2)))

    # 최소 1개는 반환
    if not candidates:
        candidates.append(
            Candidate(
                text=f"{base_text}: {inp.intent} ({inp.honorific})",
                score=0.75
            )
        )

    return SuggestResponse(candidates=candidates)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
