"""
프롬프트 빌더 서비스
캐릭터 정보를 바탕으로 LLM 프롬프트를 생성합니다.
"""

from typing import Dict, List, Optional


class PromptBuilder:
    """캐릭터 페르소나 기반 프롬프트 생성"""

    @staticmethod
    def build_system_prompt(character_info: Dict) -> str:
        """
        캐릭터 정보를 바탕으로 시스템 프롬프트 생성

        Args:
            character_info: 캐릭터 정보 딕셔너리
                - name: 캐릭터 이름
                - description: 캐릭터 설명
                - personality: 성격
                - speakingStyle: 말투 특징
                - vocabulary: 자주 사용하는 어휘
                - toneKeywords: 말투 키워드
                - examples: 실제 대사 예시 (선택적)
                - prohibitedWords: 사용하지 않는 단어 (선택적)
                - sentencePatterns: 문장 패턴 (선택적)

        Returns:
            시스템 프롬프트 문자열
        """
        name = character_info.get('name', '캐릭터')
        description = character_info.get('description', '')
        personality = character_info.get('personality', '')
        speaking_style = character_info.get('speakingStyle', '')
        vocabulary = character_info.get('vocabulary', '')
        tone_keywords = character_info.get('toneKeywords', '')
        examples = character_info.get('examples', '')
        prohibited_words = character_info.get('prohibitedWords', '')
        sentence_patterns = character_info.get('sentencePatterns', '')

        prompt = f"""당신은 '{name}' 캐릭터의 대사를 생성하는 AI입니다.

## 캐릭터 프로필
- 이름: {name}
- 설명: {description}
- 성격: {personality}
- 말투 특징: {speaking_style}
"""

        if vocabulary:
            prompt += f"- 자주 사용하는 어휘: {vocabulary}\n"

        if tone_keywords:
            prompt += f"- 말투 키워드: {tone_keywords}\n"

        if sentence_patterns:
            prompt += f"\n## 문장 패턴\n"
            for pattern in sentence_patterns.split('\n'):
                if pattern.strip():
                    prompt += f"- {pattern.strip()}\n"

        if prohibited_words:
            prompt += f"\n## 사용하지 않는 표현\n"
            prompt += f"다음 단어나 표현은 절대 사용하지 마세요: {prohibited_words}\n"

        if examples:
            prompt += f"\n## 대사 예시\n"
            for example in examples.split('\n'):
                if example.strip():
                    prompt += f'- "{example.strip()}"\n'

        prompt += """

## 대사 생성 원칙
1. 위 캐릭터의 성격과 말투를 정확히 반영하세요
2. 캐릭터가 자주 사용하는 어휘를 자연스럽게 포함하세요
3. 캐릭터의 문장 패턴을 따르세요
4. 사용하지 않는 표현은 절대 사용하지 마세요
5. 대사는 자연스럽고 실제 사람이 말하는 것처럼 작성하세요
6. 주어진 의도와 어투(존댓말/반말)를 정확히 따르세요
"""

        return prompt.strip()

    @staticmethod
    def build_user_prompt(
        intent: str,
        honorific: str,
        target_names: List[str],
        max_len: int,
        n_candidates: int,
        context: Optional[str] = None
    ) -> str:
        """
        사용자 프롬프트 생성

        Args:
            intent: 대화 의도 (reconcile, argue, comfort, greet 등)
            honorific: 존댓말 유형 (banmal, jondae)
            target_names: 대상 캐릭터 이름 목록
            max_len: 최대 문장 길이
            n_candidates: 생성할 후보 개수
            context: 추가 컨텍스트 (선택적)

        Returns:
            사용자 프롬프트 문자열
        """
        intent_korean = {
            'greet': '인사',
            'reconcile': '화해',
            'argue': '논쟁/반박',
            'comfort': '위로',
            'thank': '감사',
            'apologize': '사과',
            'request': '요청',
            'suggest': '제안',
            'question': '질문',
            'answer': '답변',
        }.get(intent, intent)

        honorific_korean = {
            'banmal': '반말',
            'jondae': '존댓말',
            'mixed': '존댓말 섞인 반말'
        }.get(honorific, honorific)

        target_str = ', '.join(target_names) if target_names else '상대방'

        prompt = f"""다음 조건에 맞는 대사를 {n_candidates}개 생성해주세요:

## 조건
- 대상: {target_str}
- 의도: {intent_korean}
- 어투: {honorific_korean}
- 최대 길이: {max_len}자
"""

        if context:
            prompt += f"- 상황: {context}\n"

        prompt += f"""

## 응답 형식
각 대사를 줄바꿈으로 구분하여 작성하고, 번호나 기호 없이 대사만 작성해주세요.
예시:
안녕? 오랜만이야!
어떻게 지냈어?
보고 싶었어.

대사를 생성해주세요:"""

        return prompt.strip()

    @staticmethod
    def build_full_prompt(
        character_info: Dict,
        intent: str,
        honorific: str,
        target_names: List[str],
        max_len: int,
        n_candidates: int,
        context: Optional[str] = None
    ) -> tuple[str, str]:
        """
        전체 프롬프트 생성 (시스템 + 사용자)

        Returns:
            (system_prompt, user_prompt) 튜플
        """
        system_prompt = PromptBuilder.build_system_prompt(character_info)
        user_prompt = PromptBuilder.build_user_prompt(
            intent=intent,
            honorific=honorific,
            target_names=target_names,
            max_len=max_len,
            n_candidates=n_candidates,
            context=context
        )

        return system_prompt, user_prompt