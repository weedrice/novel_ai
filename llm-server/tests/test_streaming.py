"""
스트리밍 컨트롤러 테스트
Server-Sent Events (SSE) 스트리밍 응답 테스트
"""

import pytest
import json
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch, AsyncMock
from app.main import app
from app.models.dialogue_models import CharacterInfo

client = TestClient(app)


class TestStreamingController:
    """스트리밍 컨트롤러 테스트"""

    def test_streaming_endpoint_with_character_info(self):
        """캐릭터 정보가 있는 스트리밍 테스트"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice",
                "description": "A friendly character",
                "personality": "cheerful",
                "speakingStyle": "casual"
            },
            "targetNames": ["Bob"],
            "provider": "gemini"
        }

        # 스트리밍은 실제 LLM 호출이 필요하므로 Mock을 사용
        with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
            async def mock_generator():
                yield "Hello"
                yield " there"
                yield "!"

            mock_stream.return_value = mock_generator()

            with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                assert response.status_code == 200
                assert response.headers["content-type"] == "text/event-stream; charset=utf-8"

                # SSE 이벤트 수집
                events = []
                for line in response.iter_lines():
                    if line.startswith("data:"):
                        data_str = line[5:].strip()  # "data: " 제거
                        if data_str:
                            events.append(json.loads(data_str))

                # 시작, 청크, 완료 이벤트 확인
                assert len(events) >= 2  # 최소한 start와 done 이벤트
                assert events[0]["type"] == "start"
                assert events[-1]["type"] == "done"

    def test_streaming_endpoint_without_character_info(self):
        """캐릭터 정보 없이 스트리밍 요청 시 에러"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1
            # characterInfo 없음
        }

        with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
            assert response.status_code == 200

            events = []
            for line in response.iter_lines():
                if line.startswith("data:"):
                    data_str = line[5:].strip()
                    if data_str:
                        events.append(json.loads(data_str))

            # 에러 이벤트가 있어야 함
            assert len(events) > 0
            assert any(e.get("error") is not None for e in events)

    def test_streaming_sse_event_format(self):
        """SSE 이벤트 형식 검증"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice"
            }
        }

        with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
            async def mock_generator():
                yield "Test"
                yield " message"

            mock_stream.return_value = mock_generator()

            with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                events = []
                for line in response.iter_lines():
                    if line.startswith("data:"):
                        data_str = line[5:].strip()
                        if data_str:
                            event = json.loads(data_str)
                            events.append(event)

                # 이벤트 타입 검증
                assert events[0]["type"] == "start"
                assert "message" in events[0]

                # 청크 이벤트 검증 (있을 경우)
                chunk_events = [e for e in events if e.get("type") == "chunk"]
                for chunk_event in chunk_events:
                    assert "text" in chunk_event
                    assert isinstance(chunk_event["text"], str)

                # 완료 이벤트 검증
                assert events[-1]["type"] == "done"
                assert "message" in events[-1]

    @patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream')
    def test_streaming_error_handling(self, mock_stream):
        """스트리밍 중 에러 처리"""
        async def error_generator():
            yield "Start"
            raise Exception("Streaming error")

        mock_stream.return_value = error_generator()

        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice"
            }
        }

        with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
            assert response.status_code == 200

            events = []
            for line in response.iter_lines():
                if line.startswith("data:"):
                    data_str = line[5:].strip()
                    if data_str:
                        events.append(json.loads(data_str))

            # 에러 이벤트가 포함되어야 함
            error_events = [e for e in events if e.get("type") == "error"]
            assert len(error_events) > 0

    def test_streaming_response_headers(self):
        """스트리밍 응답 헤더 검증"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice"
            }
        }

        with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
            async def mock_generator():
                yield "Test"

            mock_stream.return_value = mock_generator()

            with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                # 필수 헤더 검증
                assert response.headers["content-type"] == "text/event-stream; charset=utf-8"
                assert response.headers["cache-control"] == "no-cache"
                assert response.headers["connection"] == "keep-alive"
                assert response.headers["x-accel-buffering"] == "no"

    def test_streaming_with_context(self):
        """대화 컨텍스트가 포함된 스트리밍"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice"
            },
            "context": "Previous conversation about the weather"
        }

        with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
            async def mock_generator():
                yield "Nice weather"

            mock_stream.return_value = mock_generator()

            with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                assert response.status_code == 200

    def test_streaming_with_different_providers(self):
        """다양한 프로바이더로 스트리밍 테스트"""
        providers = ["openai", "claude", "gemini"]

        for provider in providers:
            payload = {
                "speakerId": "char1",
                "targetIds": ["char2"],
                "intent": "greet",
                "honorific": "banmal",
                "maxLen": 80,
                "nCandidates": 1,
                "characterInfo": {
                    "name": "Alice"
                },
                "provider": provider
            }

            with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
                async def mock_generator():
                    yield f"Hello from {provider}"

                mock_stream.return_value = mock_generator()

                with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                    assert response.status_code == 200

    def test_streaming_with_multiple_chunks(self):
        """여러 청크로 나뉜 스트리밍 응답"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice"
            }
        }

        with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
            async def mock_generator():
                for word in ["Hello", " ", "there", ",", " ", "friend", "!"]:
                    yield word

            mock_stream.return_value = mock_generator()

            with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                events = []
                for line in response.iter_lines():
                    if line.startswith("data:"):
                        data_str = line[5:].strip()
                        if data_str:
                            events.append(json.loads(data_str))

                # 청크 이벤트 확인
                chunk_events = [e for e in events if e.get("type") == "chunk"]
                assert len(chunk_events) > 0

                # 모든 청크 텍스트 합치기
                full_text = "".join(e["text"] for e in chunk_events)
                assert len(full_text) > 0

    def test_streaming_with_empty_character_name(self):
        """빈 캐릭터 이름으로 스트리밍"""
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1,
            "characterInfo": {
                "name": ""
            }
        }

        with patch('app.core.llm_provider_manager.LLMProviderManager.generate_stream') as mock_stream:
            async def mock_generator():
                yield "Hello"

            mock_stream.return_value = mock_generator()

            with client.stream("POST", "/gen/suggest-stream", json=payload) as response:
                assert response.status_code == 200


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
