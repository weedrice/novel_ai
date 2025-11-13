"""
프롬프트 템플릿 모음
각 분석 유형별 시스템/유저 프롬프트 템플릿
"""

from typing import Optional


class PromptTemplates:
    """프롬프트 템플릿 관리 클래스"""

    # ============================================================
    # 스크립트 분석 템플릿
    # ============================================================

    SCRIPT_ANALYSIS_SYSTEM = """
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

    @staticmethod
    def script_analysis_user_prompt(content: str, format_hint: Optional[str]) -> str:
        hint_text = f"Format hint: This appears to be a {format_hint}.\n" if format_hint else ""
        return f"""
{hint_text}
Please analyze the following script and extract characters, dialogues, scenes, and relationships in JSON format.

## Script Content:
{content}

Remember: Return ONLY valid JSON with the exact structure specified.
""".strip()

    # ============================================================
    # 에피소드 요약 템플릿
    # ============================================================

    EPISODE_SUMMARY_SYSTEM = """
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
""".strip()

    @staticmethod
    def episode_summary_user_prompt(script_text: str, format_hint: Optional[str]) -> str:
        hint = f"형식: {format_hint}\n" if format_hint else ""
        return f"""
{hint}다음 스크립트를 요약해주세요:

{script_text}

JSON 형식으로만 응답하세요.
""".strip()

    # ============================================================
    # 캐릭터 분석 템플릿
    # ============================================================

    CHARACTER_ANALYSIS_SYSTEM = """
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
""".strip()

    @staticmethod
    def character_analysis_user_prompt(script_text: str, format_hint: Optional[str]) -> str:
        hint = f"형식: {format_hint}\n" if format_hint else ""
        return f"""
{hint}다음 스크립트에서 캐릭터를 추출하고 분석해주세요:

{script_text}

JSON 형식으로만 응답하세요.
""".strip()

    # ============================================================
    # 장면 분석 템플릿
    # ============================================================

    SCENE_ANALYSIS_SYSTEM = """
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
""".strip()

    @staticmethod
    def scene_analysis_user_prompt(script_text: str, format_hint: Optional[str]) -> str:
        hint = f"형식: {format_hint}\n" if format_hint else ""
        return f"""
{hint}다음 스크립트를 장면 단위로 분석해주세요:

{script_text}

JSON 형식으로만 응답하세요.
""".strip()

    # ============================================================
    # 대사 분석 템플릿
    # ============================================================

    DIALOGUE_ANALYSIS_SYSTEM = """
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
""".strip()

    @staticmethod
    def dialogue_analysis_user_prompt(script_text: str, format_hint: Optional[str]) -> str:
        hint = f"형식: {format_hint}\n" if format_hint else ""
        return f"""
{hint}다음 스크립트에서 대사를 추출하고 분석해주세요:

{script_text}

JSON 형식으로만 응답하세요.
""".strip()

    # ============================================================
    # 맞춤법 검사 템플릿
    # ============================================================

    SPELL_CHECK_SYSTEM = """
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
""".strip()

    @staticmethod
    def spell_check_user_prompt(script_text: str, format_hint: Optional[str]) -> str:
        hint = f"형식: {format_hint}\n" if format_hint else ""
        return f"""
{hint}다음 텍스트의 맞춤법과 문법을 검사해주세요:

{script_text}

JSON 형식으로만 응답하세요.
""".strip()

    # ============================================================
    # 시나리오 생성 템플릿
    # ============================================================

    @staticmethod
    def scenario_system_prompt(scene_description: str, location: Optional[str], mood: Optional[str], participants_desc: str) -> str:
        return f"""
You are a creative dialogue writer. Based on the scene and participants below, generate short back-and-forth lines that feel natural and reflect each character.

## Scene
- Location: {location or 'N/A'}
- Mood: {mood or 'neutral'}
- Description: {scene_description}

## Participants
{participants_desc}

## Rules
1. Keep each line concise and conversational.
2. Alternate speakers naturally; avoid narration.
3. Reflect each character's personality and style.
4. Do not include numbering or brackets; just lines like "Name: text".
""".strip()

    @staticmethod
    def scenario_user_prompt(dialogue_count: int) -> str:
        return f"Generate {dialogue_count} short lines in a conversation-like format.\nUse the format: Name: text"
