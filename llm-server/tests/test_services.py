"""
Service Layer 에러 케이스 테스트
DialogueService, ScenarioService, ScriptAnalysisService, EpisodeAnalysisService 에러 처리 테스트
"""

import pytest
from unittest.mock import Mock, patch
from app.core.llm_provider_manager import LLMProviderManager
from app.services.dialogue_service import DialogueService
from app.services.scenario_service import ScenarioService
from app.services.script_analysis_service import ScriptAnalysisService
from app.services.episode_analysis_service import EpisodeAnalysisService
from app.models.dialogue_models import SuggestInput, CharacterInfo
from app.models.scenario_models import ScenarioInput, ParticipantInfo
from app.models.script_analysis_models import ScriptAnalysisInput
from app.models.episode_analysis_models import EpisodeAnalysisInput


class TestDialogueServiceErrorCases:
    """DialogueService 에러 케이스 테스트"""

    @pytest.fixture
    def mock_llm_manager(self):
        """Mock LLM Manager"""
        mock = Mock(spec=LLMProviderManager)
        mock.default_provider = "openai"
        return mock

    @pytest.fixture
    def dialogue_service(self, mock_llm_manager):
        """DialogueService 인스턴스"""
        return DialogueService(mock_llm_manager)

    def test_fallback_when_no_character_info(self, dialogue_service):
        """캐릭터 정보 없을 때 fallback 응답"""
        inp = SuggestInput(
            speakerId="char1",
            targetIds=["char2"],
            intent="greet",
            honorific="banmal",
            maxLen=80,
            nCandidates=3,
            characterInfo=None  # No character info
        )

        result = dialogue_service.generate_dialogue_suggestions(inp)

        assert result is not None
        assert len(result.candidates) > 0
        assert all(c.text and c.score >= 0 for c in result.candidates)

    def test_fallback_when_llm_fails(self, dialogue_service, mock_llm_manager):
        """LLM 호출 실패 시 fallback"""
        mock_llm_manager.generate.side_effect = Exception("LLM API Error")

        inp = SuggestInput(
            speakerId="char1",
            targetIds=["char2"],
            intent="comfort",
            honorific="jondae",
            maxLen=80,
            nCandidates=3,
            characterInfo=CharacterInfo(
                name="Alice",
                description="Kind person"
            )
        )

        result = dialogue_service.generate_dialogue_suggestions(inp)

        assert result is not None
        assert len(result.candidates) > 0

    def test_fallback_when_empty_response(self, dialogue_service, mock_llm_manager):
        """LLM이 빈 응답을 반환할 때"""
        mock_llm_manager.generate.return_value = ""

        inp = SuggestInput(
            speakerId="char1",
            targetIds=["char2"],
            intent="greet",
            honorific="banmal",
            maxLen=80,
            nCandidates=3,
            characterInfo=CharacterInfo(name="Bob")
        )

        result = dialogue_service.generate_dialogue_suggestions(inp)

        assert result is not None
        assert len(result.candidates) > 0

    def test_dialogue_truncation_when_exceeds_max_length(self, dialogue_service, mock_llm_manager):
        """대사가 최대 길이를 초과할 때 자르기"""
        long_dialogue = "This is a very long dialogue that definitely exceeds the maximum length limit"
        mock_llm_manager.generate.return_value = long_dialogue

        inp = SuggestInput(
            speakerId="char1",
            targetIds=["char2"],
            intent="greet",
            honorific="banmal",
            maxLen=20,  # Very short limit
            nCandidates=1,
            characterInfo=CharacterInfo(name="Alice")
        )

        result = dialogue_service.generate_dialogue_suggestions(inp)

        assert result is not None
        assert len(result.candidates) > 0
        assert all(len(c.text) <= 20 for c in result.candidates)

    def test_parse_dialogues_with_numbered_list(self, dialogue_service):
        """번호가 매겨진 대사 파싱"""
        text = """1. Hello there!
2. How are you doing?
3. Nice to meet you."""

        dialogues = dialogue_service._parse_dialogues(text, 3)

        assert len(dialogues) == 3
        assert "Hello there!" in dialogues[0]
        assert "How are you doing?" in dialogues[1]

    def test_parse_dialogues_with_bullets(self, dialogue_service):
        """불릿 포인트가 있는 대사 파싱"""
        text = """- First dialogue
• Second dialogue
* Third dialogue"""

        dialogues = dialogue_service._parse_dialogues(text, 3)

        assert len(dialogues) == 3
        assert dialogues[0] == "First dialogue"

    def test_parse_dialogues_with_quotes(self, dialogue_service):
        """따옴표가 있는 대사 파싱"""
        text = """"Hello!"
'How are you?'
"Nice to meet you\""""

        dialogues = dialogue_service._parse_dialogues(text, 3)

        assert len(dialogues) == 3
        assert dialogues[0] == "Hello!"

    def test_fallback_dialogues_when_parsing_fails(self, dialogue_service):
        """파싱 실패 시 fallback 대사"""
        text = ""  # Empty text

        dialogues = dialogue_service._parse_dialogues(text, 3)

        assert len(dialogues) > 0
        assert all(isinstance(d, str) for d in dialogues)

    def test_honorific_jondae_fallback(self, dialogue_service):
        """존댓말 fallback 응답"""
        inp = SuggestInput(
            speakerId="char1",
            targetIds=["char2"],
            intent="thank",
            honorific="jondae",
            maxLen=80,
            nCandidates=2
        )

        result = dialogue_service.generate_dialogue_suggestions(inp)

        assert result is not None
        assert len(result.candidates) > 0


class TestScenarioServiceErrorCases:
    """ScenarioService 에러 케이스 테스트"""

    @pytest.fixture
    def mock_llm_manager(self):
        """Mock LLM Manager"""
        mock = Mock(spec=LLMProviderManager)
        mock.default_provider = "openai"
        return mock

    @pytest.fixture
    def scenario_service(self, mock_llm_manager):
        """ScenarioService 인스턴스"""
        return ScenarioService(mock_llm_manager)

    def test_fallback_when_llm_fails(self, scenario_service, mock_llm_manager):
        """LLM 호출 실패 시 fallback"""
        mock_llm_manager.generate.side_effect = Exception("LLM Error")

        inp = ScenarioInput(
            sceneDescription="Two friends meeting",
            location="Cafe",
            mood="relaxed",
            participants=[
                ParticipantInfo(
                    characterId="char1",
                    name="Alice",
                    personality="cheerful",
                    speakingStyle="casual"
                )
            ],
            dialogueCount=3
        )

        result = scenario_service.generate_scenario(inp)

        assert result is not None
        assert len(result.dialogues) > 0

    def test_fallback_when_empty_dialogues(self, scenario_service, mock_llm_manager):
        """파싱된 대화가 없을 때 fallback"""
        mock_llm_manager.generate.return_value = "This is not a dialogue format"

        inp = ScenarioInput(
            sceneDescription="Test scene",
            participants=[
                ParticipantInfo(characterId="char1", name="Alice")
            ],
            dialogueCount=5
        )

        result = scenario_service.generate_scenario(inp)

        assert result is not None
        assert len(result.dialogues) > 0

    def test_parse_dialogues_with_valid_format(self, scenario_service):
        """정상적인 대화 형식 파싱"""
        text = """Alice: Hello there!
Bob: Hi Alice, how are you?
Alice: I'm doing great!"""

        participants = [
            ParticipantInfo(characterId="char1", name="Alice"),
            ParticipantInfo(characterId="char2", name="Bob")
        ]

        dialogues = scenario_service._parse_scenario_dialogues(text, participants)

        assert len(dialogues) >= 2
        assert any(d.speaker == "Alice" for d in dialogues)
        assert any(d.speaker == "Bob" for d in dialogues)

    def test_parse_dialogues_ignores_comments(self, scenario_service):
        """주석 라인 무시"""
        text = """# This is a comment
Alice: Hello!
// Another comment
Bob: Hi there!"""

        participants = [
            ParticipantInfo(characterId="char1", name="Alice"),
            ParticipantInfo(characterId="char2", name="Bob")
        ]

        dialogues = scenario_service._parse_scenario_dialogues(text, participants)

        assert len(dialogues) == 2
        assert all(d.text not in ["This is a comment", "Another comment"] for d in dialogues)

    def test_parse_dialogues_with_brackets(self, scenario_service):
        """대괄호로 감싸진 화자 이름 파싱"""
        text = """[Alice]: How are you?
[Bob]: I'm fine, thanks."""

        participants = [
            ParticipantInfo(characterId="char1", name="Alice"),
            ParticipantInfo(characterId="char2", name="Bob")
        ]

        dialogues = scenario_service._parse_scenario_dialogues(text, participants)

        assert len(dialogues) == 2

    def test_fallback_scenario_generation(self, scenario_service):
        """Fallback 시나리오 생성"""
        inp = ScenarioInput(
            sceneDescription="Test",
            participants=[
                ParticipantInfo(characterId="char1", name="Alice"),
                ParticipantInfo(characterId="char2", name="Bob")
            ],
            dialogueCount=3
        )

        dialogues = scenario_service._generate_fallback_scenario(inp)

        assert len(dialogues) > 0
        assert all(d.speaker and d.text for d in dialogues)


class TestScriptAnalysisServiceErrorCases:
    """ScriptAnalysisService 에러 케이스 테스트"""

    @pytest.fixture
    def mock_llm_manager(self):
        """Mock LLM Manager"""
        mock = Mock(spec=LLMProviderManager)
        mock.default_provider = "openai"
        return mock

    @pytest.fixture
    def script_analysis_service(self, mock_llm_manager):
        """ScriptAnalysisService 인스턴스"""
        return ScriptAnalysisService(mock_llm_manager)

    def test_empty_analysis_when_llm_fails(self, script_analysis_service, mock_llm_manager):
        """LLM 실패 시 빈 분석 결과"""
        mock_llm_manager.generate.side_effect = Exception("LLM Error")

        inp = ScriptAnalysisInput(
            content="Alice said hello to Bob.",
            formatHint="novel"
        )

        result = script_analysis_service.analyze_script(inp)

        assert result is not None
        assert result.characters == []
        assert result.dialogues == []
        assert result.scenes == []
        assert result.relationships == []

    def test_empty_analysis_when_no_response(self, script_analysis_service, mock_llm_manager):
        """LLM이 빈 응답을 반환할 때"""
        mock_llm_manager.generate.return_value = ""

        inp = ScriptAnalysisInput(
            content="Test content",
            formatHint="scenario"
        )

        result = script_analysis_service.analyze_script(inp)

        assert result is not None
        assert result.characters == []

    def test_empty_analysis_when_json_parsing_fails(self, script_analysis_service, mock_llm_manager):
        """JSON 파싱 실패 시 빈 분석 결과"""
        mock_llm_manager.generate.return_value = "This is not JSON"

        inp = ScriptAnalysisInput(
            content="Test content",
            formatHint="novel"
        )

        result = script_analysis_service.analyze_script(inp)

        assert result is not None
        assert result.characters == []

    @patch('app.services.script_analysis_service.JSONParser.parse_json_response')
    def test_empty_analysis_when_pydantic_conversion_fails(
        self, mock_parse, script_analysis_service, mock_llm_manager
    ):
        """Pydantic 모델 변환 실패 시 빈 분석 결과"""
        mock_llm_manager.generate.return_value = '{"characters": [{"invalid": "data"}]}'
        mock_parse.return_value = {"characters": [{"invalid": "data"}]}

        inp = ScriptAnalysisInput(
            content="Test content",
            formatHint="novel"
        )

        result = script_analysis_service.analyze_script(inp)

        # Pydantic validation 실패 시 빈 결과 반환
        assert result is not None


class TestEpisodeAnalysisServiceErrorCases:
    """EpisodeAnalysisService 에러 케이스 테스트"""

    @pytest.fixture
    def mock_llm_manager(self):
        """Mock LLM Manager"""
        mock = Mock(spec=LLMProviderManager)
        mock.default_provider = "openai"
        return mock

    @pytest.fixture
    def episode_analysis_service(self, mock_llm_manager):
        """EpisodeAnalysisService 인스턴스"""
        return EpisodeAnalysisService(mock_llm_manager)

    def test_summary_fallback_when_llm_fails(self, episode_analysis_service, mock_llm_manager):
        """LLM 실패 시 요약 fallback"""
        mock_llm_manager.generate.side_effect = Exception("LLM Error")

        inp = EpisodeAnalysisInput(
            scriptText="Test script content",
            scriptFormat="novel"
        )

        result = episode_analysis_service.generate_summary(inp)

        assert result is not None
        assert "오류" in result.summary
        assert result.wordCount > 0

    def test_summary_fallback_when_no_response(self, episode_analysis_service, mock_llm_manager):
        """LLM 빈 응답 시 요약 fallback"""
        mock_llm_manager.generate.return_value = ""

        inp = EpisodeAnalysisInput(
            scriptText="Test content",
            scriptFormat="scenario"
        )

        result = episode_analysis_service.generate_summary(inp)

        assert result is not None
        assert "생성할 수 없습니다" in result.summary

    def test_summary_fallback_when_json_parsing_fails(self, episode_analysis_service, mock_llm_manager):
        """JSON 파싱 실패 시 요약 fallback"""
        mock_llm_manager.generate.return_value = "Not JSON"

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.generate_summary(inp)

        assert result is not None
        assert result.summary is not None

    def test_characters_empty_when_llm_fails(self, episode_analysis_service, mock_llm_manager):
        """LLM 실패 시 빈 캐릭터 목록"""
        mock_llm_manager.generate.side_effect = Exception("Error")

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.analyze_characters(inp)

        assert result is not None
        assert result.characters == []

    def test_characters_empty_when_no_response(self, episode_analysis_service, mock_llm_manager):
        """빈 응답 시 빈 캐릭터 목록"""
        mock_llm_manager.generate.return_value = ""

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.analyze_characters(inp)

        assert result.characters == []

    def test_scenes_empty_when_llm_fails(self, episode_analysis_service, mock_llm_manager):
        """LLM 실패 시 빈 장면 목록"""
        mock_llm_manager.generate.side_effect = Exception("Error")

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.analyze_scenes(inp)

        assert result is not None
        assert result.scenes == []

    def test_dialogues_empty_when_llm_fails(self, episode_analysis_service, mock_llm_manager):
        """LLM 실패 시 빈 대사 목록"""
        mock_llm_manager.generate.side_effect = Exception("Error")

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.analyze_dialogues(inp)

        assert result is not None
        assert result.dialogues == []
        assert result.statistics == {}

    def test_spell_check_empty_when_llm_fails(self, episode_analysis_service, mock_llm_manager):
        """LLM 실패 시 빈 맞춤법 검사 결과"""
        mock_llm_manager.generate.side_effect = Exception("Error")

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.spell_check(inp)

        assert result is not None
        assert result.issues == []
        assert result.summary == {}

    @patch('app.services.episode_analysis_service.JSONParser.parse_json_response')
    def test_summary_with_valid_json(self, mock_parse, episode_analysis_service, mock_llm_manager):
        """정상 JSON 응답 처리"""
        mock_llm_manager.generate.return_value = '{"summary": "Test summary", "keyPoints": ["point1"], "wordCount": 100}'
        mock_parse.return_value = {
            "summary": "Test summary",
            "keyPoints": ["point1"],
            "wordCount": 100
        }

        inp = EpisodeAnalysisInput(
            scriptText="Test",
            scriptFormat="novel"
        )

        result = episode_analysis_service.generate_summary(inp)

        assert result.summary == "Test summary"
        assert result.keyPoints == ["point1"]
        assert result.wordCount == 100


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
