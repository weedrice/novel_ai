"""
LLM Provider Manager 테스트
Mock을 사용한 LLM API 호출 테스트
"""

import pytest
import pytest_asyncio
from unittest.mock import Mock, patch, MagicMock, AsyncMock
from app.core.llm_provider_manager import LLMProviderManager

# pytest-asyncio 설정
pytestmark = pytest.mark.asyncio


class TestLLMProviderManager:
    """LLMProviderManager 테스트 클래스"""

    @pytest.fixture
    def llm_manager(self):
        """LLMProviderManager 인스턴스 생성"""
        with patch.dict('os.environ', {
            'OPENAI_API_KEY': 'test-openai-key',
            'ANTHROPIC_API_KEY': 'test-anthropic-key',
            'GOOGLE_API_KEY': 'test-google-key',
            'DEFAULT_LLM_PROVIDER': 'openai'
        }):
            manager = LLMProviderManager()
            return manager

    # ============================================================
    # 초기화 테스트
    # ============================================================

    def test_initialization_with_env_vars(self, llm_manager):
        """환경 변수로 초기화 확인"""
        assert llm_manager.default_provider == 'openai'
        assert llm_manager.openai_api_key == 'test-openai-key'
        assert llm_manager.anthropic_api_key == 'test-anthropic-key'
        assert llm_manager.google_api_key == 'test-google-key'

    def test_get_available_providers(self, llm_manager):
        """사용 가능한 프로바이더 목록"""
        providers = llm_manager.get_available_providers()
        # Mock이므로 실제 클라이언트 초기화는 안되지만 테스트용

    # ============================================================
    # OpenAI 테스트
    # ============================================================

    @patch('app.core.llm_provider_manager.OpenAI')
    def test_openai_generation(self, mock_openai_class):
        """OpenAI 텍스트 생성 테스트"""
        # Mock 설정
        mock_client = Mock()
        mock_openai_class.return_value = mock_client

        mock_response = Mock()
        mock_choice = Mock()
        mock_message = Mock()
        mock_message.content = "Generated dialogue"
        mock_choice.message = mock_message
        mock_response.choices = [mock_choice]

        mock_client.chat.completions.create.return_value = mock_response

        # LLM Manager 생성 (patch된 OpenAI 사용)
        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.openai_client = mock_client

            # 테스트 실행
            result = manager.generate(
                system_prompt="You are helpful",
                user_prompt="Say hello",
                provider="openai"
            )

            # 검증
            assert result == "Generated dialogue"
            mock_client.chat.completions.create.assert_called_once()

    @patch('app.core.llm_provider_manager.OpenAI')
    def test_openai_generation_with_parameters(self, mock_openai_class):
        """OpenAI 파라미터 전달 테스트"""
        mock_client = Mock()
        mock_openai_class.return_value = mock_client

        mock_response = Mock()
        mock_choice = Mock()
        mock_message = Mock()
        mock_message.content = "Response"
        mock_choice.message = mock_message
        mock_response.choices = [mock_choice]

        mock_client.chat.completions.create.return_value = mock_response

        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.openai_client = mock_client

            result = manager.generate(
                system_prompt="System",
                user_prompt="User",
                max_tokens=100,
                temperature=0.7,
                n_candidates=3,
                provider="openai"
            )

            # 호출 파라미터 검증
            call_args = mock_client.chat.completions.create.call_args
            assert call_args[1]['max_tokens'] == 100
            assert call_args[1]['temperature'] == 0.7
            assert call_args[1]['n'] == 3

    # ============================================================
    # Anthropic (Claude) 테스트
    # ============================================================

    @patch('app.core.llm_provider_manager.anthropic.Anthropic')
    def test_claude_generation(self, mock_anthropic_class):
        """Claude 텍스트 생성 테스트"""
        mock_client = Mock()
        mock_anthropic_class.return_value = mock_client

        mock_message = Mock()
        mock_content = Mock()
        mock_content.text = "Claude response"
        mock_message.content = [mock_content]

        mock_client.messages.create.return_value = mock_message

        with patch.dict('os.environ', {'ANTHROPIC_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.anthropic_client = mock_client

            result = manager.generate(
                system_prompt="You are helpful",
                user_prompt="Say hello",
                provider="claude"
            )

            assert result == "Claude response"
            mock_client.messages.create.assert_called_once()

    @patch('app.core.llm_provider_manager.anthropic.Anthropic')
    def test_claude_generation_multiple_candidates(self, mock_anthropic_class):
        """Claude 여러 후보 생성 (프롬프트에 명시)"""
        mock_client = Mock()
        mock_anthropic_class.return_value = mock_client

        mock_message = Mock()
        mock_content = Mock()
        mock_content.text = "Response 1\nResponse 2\nResponse 3"
        mock_message.content = [mock_content]

        mock_client.messages.create.return_value = mock_message

        with patch.dict('os.environ', {'ANTHROPIC_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.anthropic_client = mock_client

            result = manager.generate(
                system_prompt="System",
                user_prompt="User",
                n_candidates=3,
                provider="claude"
            )

            # 프롬프트에 후보 개수가 추가되었는지 확인
            call_args = mock_client.messages.create.call_args
            user_message = call_args[1]['messages'][0]['content']
            assert '3개' in user_message or '3' in user_message

    # ============================================================
    # Google Gemini 테스트
    # ============================================================

    @patch('app.core.llm_provider_manager.genai')
    def test_gemini_generation(self, mock_genai):
        """Gemini 텍스트 생성 테스트"""
        mock_model = Mock()
        mock_response = Mock()
        mock_response.text = "Gemini response"
        mock_response.candidates = [Mock()]
        mock_response.candidates[0].finish_reason = 1  # STOP
        mock_response.candidates[0].content.parts = [Mock()]

        mock_model.generate_content.return_value = mock_response

        with patch.dict('os.environ', {'GOOGLE_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.gemini_model_instance = mock_model

            result = manager.generate(
                system_prompt="You are helpful",
                user_prompt="Say hello",
                provider="gemini"
            )

            assert result == "Gemini response"
            mock_model.generate_content.assert_called_once()

    @patch('app.core.llm_provider_manager.genai')
    def test_gemini_safety_filter_blocked(self, mock_genai):
        """Gemini 안전 필터 차단 처리"""
        mock_model = Mock()
        mock_response = Mock()
        mock_response.candidates = [Mock()]
        mock_response.candidates[0].finish_reason = 2  # SAFETY
        mock_response.candidates[0].safety_ratings = []

        mock_model.generate_content.return_value = mock_response

        with patch.dict('os.environ', {'GOOGLE_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.gemini_model_instance = mock_model

            with pytest.raises(RuntimeError, match="safety filter"):
                manager.generate(
                    system_prompt="System",
                    user_prompt="User",
                    provider="gemini"
                )

    # ============================================================
    # 에러 핸들링 테스트
    # ============================================================

    def test_generate_with_unknown_provider(self, llm_manager):
        """알 수 없는 프로바이더 처리"""
        with pytest.raises(ValueError, match="Unknown provider"):
            llm_manager.generate(
                system_prompt="System",
                user_prompt="User",
                provider="unknown_provider"
            )

    def test_generate_with_unavailable_client(self):
        """클라이언트가 초기화되지 않은 경우"""
        with patch.dict('os.environ', {'OPENAI_API_KEY': ''}):
            manager = LLMProviderManager()

            with pytest.raises(RuntimeError, match="not available"):
                manager.generate(
                    system_prompt="System",
                    user_prompt="User",
                    provider="openai"
                )

    # ============================================================
    # 스트리밍 테스트
    # ============================================================

    @pytest.mark.asyncio
    @patch('app.core.llm_provider_manager.OpenAI')
    async def test_openai_streaming(self, mock_openai_class):
        """OpenAI 스트리밍 테스트"""
        mock_client = Mock()
        mock_openai_class.return_value = mock_client

        # 스트리밍 응답 Mock
        mock_chunk1 = Mock()
        mock_chunk1.choices = [Mock()]
        mock_chunk1.choices[0].delta.content = "Hello"

        mock_chunk2 = Mock()
        mock_chunk2.choices = [Mock()]
        mock_chunk2.choices[0].delta.content = " World"

        mock_client.chat.completions.create.return_value = [mock_chunk1, mock_chunk2]

        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            manager = LLMProviderManager()
            manager.openai_client = mock_client

            chunks = []
            async for chunk in manager.generate_stream(
                system_prompt="System",
                user_prompt="User",
                provider="openai"
            ):
                chunks.append(chunk)

            assert len(chunks) == 2
            assert chunks[0] == "Hello"
            assert chunks[1] == " World"

    @pytest.mark.asyncio
    async def test_streaming_with_unknown_provider(self, llm_manager):
        """스트리밍: 알 수 없는 프로바이더"""
        with pytest.raises(ValueError, match="Unknown provider"):
            async for _ in llm_manager.generate_stream(
                system_prompt="System",
                user_prompt="User",
                provider="unknown"
            ):
                pass

    # ============================================================
    # 기본 프로바이더 테스트
    # ============================================================

    @patch('app.core.llm_provider_manager.genai')
    def test_generate_with_default_provider(self, mock_genai):
        """기본 프로바이더 사용 (provider 파라미터 없음)"""
        mock_model = Mock()
        mock_response = Mock()
        mock_response.text = "Response"
        mock_response.candidates = [Mock()]
        mock_response.candidates[0].finish_reason = 1
        mock_response.candidates[0].content.parts = [Mock()]

        mock_model.generate_content.return_value = mock_response

        with patch.dict('os.environ', {
            'GOOGLE_API_KEY': 'test-key',
            'DEFAULT_LLM_PROVIDER': 'gemini'
        }):
            manager = LLMProviderManager()
            manager.gemini_model_instance = mock_model

            # provider 미지정 시 default 사용
            result = manager.generate(
                system_prompt="System",
                user_prompt="User"
            )

            assert result == "Response"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
