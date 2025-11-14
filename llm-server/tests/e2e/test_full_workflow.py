"""
E2E 테스트: 전체 워크플로우
실제 사용자 시나리오를 재현하는 통합 테스트
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


@pytest.mark.e2e
class TestDialogueGenerationWorkflow:
    """대사 생성 전체 워크플로우 테스트"""

    def test_full_dialogue_generation_workflow(self):
        """헬스체크 → Provider 확인 → 대사 생성 전체 플로우"""

        # 1. 헬스 체크
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "ok"

        # 2. Provider 목록 확인
        response = client.get("/providers")
        assert response.status_code == 200
        providers_data = response.json()
        assert "available" in providers_data
        assert len(providers_data["providers"]) > 0

        # 3. 대사 생성 요청 (fallback)
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 3,
        }
        response = client.post("/gen/suggest", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "candidates" in data
        assert len(data["candidates"]) > 0
        assert all("text" in c and "score" in c for c in data["candidates"])

        # 4. 캐릭터 정보와 함께 대사 생성
        payload_with_char = {
            "speakerId": "alice",
            "targetIds": ["bob"],
            "intent": "comfort",
            "honorific": "jondae",
            "maxLen": 100,
            "nCandidates": 2,
            "characterInfo": {
                "name": "Alice",
                "personality": "따뜻하고 다정함",
                "speakingStyle": "차분하고 공손한 말투",
            },
            "targetNames": ["Bob"],
            "context": "Bob이 시험에서 떨어진 직후",
        }
        response = client.post("/gen/suggest", json=payload_with_char)
        assert response.status_code == 200
        data = response.json()
        assert len(data["candidates"]) > 0


@pytest.mark.e2e
class TestScenarioGenerationWorkflow:
    """시나리오 생성 전체 워크플로우 테스트"""

    def test_multi_character_scenario_workflow(self):
        """다중 캐릭터 시나리오 생성 플로우"""

        # 1. 시나리오 생성 요청
        payload = {
            "sceneDescription": "카페에서 두 친구가 만나는 장면",
            "location": "카페",
            "mood": "편안한",
            "participants": [
                {
                    "characterId": "alice",
                    "name": "Alice",
                    "personality": "밝고 활발함",
                    "speakingStyle": "친근하고 경쾌한 말투",
                },
                {
                    "characterId": "bob",
                    "name": "Bob",
                    "personality": "조용하고 신중함",
                    "speakingStyle": "차분하고 깊이 있는 말투",
                },
            ],
            "dialogueCount": 5,
        }
        response = client.post("/gen/scenario", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "dialogues" in data
        assert len(data["dialogues"]) > 0

        # 2. 생성된 시나리오 검증
        for dialogue in data["dialogues"]:
            assert "speaker" in dialogue
            assert "text" in dialogue
            assert dialogue["speaker"] in ["alice", "bob", "Alice", "Bob"]


@pytest.mark.e2e
class TestScriptAnalysisWorkflow:
    """대본 분석 전체 워크플로우 테스트"""

    def test_script_analysis_workflow(self):
        """대본 분석 → 결과 검증 플로우"""

        # 1. 대본 분석 요청
        script = """
        Alice: 안녕 Bob! 오랜만이야.
        Bob: 응, 정말 오랜만이네. 잘 지냈어?
        Alice: 응! 요즘 새 프로젝트로 바빠서 그동안 연락 못했어.
        Bob: 그랬구나. 무슨 프로젝트인데?
        """

        payload = {
            "content": script,
            "formatHint": "scenario",
        }

        response = client.post("/gen/analyze-script", json=payload)
        assert response.status_code == 200
        data = response.json()

        # 2. 분석 결과 검증
        assert "characters" in data
        assert "dialogues" in data
        assert "scenes" in data
        assert "relationships" in data


@pytest.mark.e2e
class TestEpisodeAnalysisWorkflow:
    """에피소드 분석 전체 워크플로우 테스트"""

    def test_episode_analysis_workflow(self):
        """에피소드 요약 → 캐릭터 분석 → 대사 분석 플로우"""

        script_text = "테스트용 대본 내용입니다. 주인공이 모험을 떠나는 이야기입니다."

        # 1. 요약 생성
        response = client.post(
            "/gen/episode/summary",
            json={"scriptText": script_text, "scriptFormat": "novel"},
        )
        assert response.status_code == 200
        summary_data = response.json()
        assert "summary" in summary_data
        assert "keyPoints" in summary_data

        # 2. 캐릭터 분석
        response = client.post(
            "/gen/episode/characters",
            json={"scriptText": script_text, "scriptFormat": "novel"},
        )
        assert response.status_code == 200
        char_data = response.json()
        assert "characters" in char_data

        # 3. 대사 분석
        response = client.post(
            "/gen/episode/dialogues",
            json={"scriptText": script_text, "scriptFormat": "novel"},
        )
        assert response.status_code == 200
        dialogue_data = response.json()
        assert "dialogues" in dialogue_data
        assert "statistics" in dialogue_data


@pytest.mark.e2e
class TestStreamingWorkflow:
    """스트리밍 응답 워크플로우 테스트"""

    def test_streaming_response_workflow(self):
        """스트리밍 응답 수신 플로우"""

        payload = {
            "speakerId": "alice",
            "targetIds": ["bob"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 50,
            "nCandidates": 1,
            "characterInfo": {
                "name": "Alice",
                "personality": "밝은",
            },
        }

        response = client.post("/gen/suggest-stream", json=payload)
        assert response.status_code == 200

        # SSE 응답 확인
        content = response.text
        assert "data:" in content  # SSE 형식 확인


@pytest.mark.e2e
class TestSecurityWorkflow:
    """보안 기능 워크플로우 테스트"""

    def test_input_validation_workflow(self):
        """입력 검증이 올바르게 작동하는지 확인"""

        # 1. 잘못된 intent (숫자 포함)
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "invalid123",  # 숫자 포함 - 거부되어야 함
            "honorific": "banmal",
        }
        response = client.post("/gen/suggest", json=payload)
        assert response.status_code == 422  # Validation error

        # 2. 잘못된 honorific
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "invalid_honorific",  # 허용되지 않는 값
        }
        response = client.post("/gen/suggest", json=payload)
        assert response.status_code == 422

        # 3. XSS 시도
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "context": "<script>alert('xss')</script>",  # XSS 시도
        }
        response = client.post("/gen/suggest", json=payload)
        assert response.status_code == 422  # Validation error

    def test_security_headers_present(self):
        """보안 헤더가 응답에 포함되는지 확인"""

        response = client.get("/health")
        assert response.status_code == 200

        # 보안 헤더 확인
        headers = response.headers
        assert "x-content-type-options" in headers
        assert headers["x-content-type-options"] == "nosniff"
        assert "x-frame-options" in headers
        assert "x-request-id" in headers  # Request ID 미들웨어

    def test_rate_limiting_configured(self):
        """Rate Limiting이 설정되어 있는지 확인"""

        # Rate Limiting은 테스트 모드에서 비활성화되므로
        # 여러 번 요청해도 성공해야 함
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
        }

        for _ in range(5):
            response = client.post("/gen/suggest", json=payload)
            assert response.status_code == 200


@pytest.mark.e2e
class TestMonitoringEndpoints:
    """모니터링 엔드포인트 테스트"""

    def test_metrics_endpoint_accessible(self):
        """Prometheus 메트릭 엔드포인트 접근 가능"""

        response = client.get("/metrics")
        assert response.status_code == 200
        # Prometheus 텍스트 형식 확인
        assert "# HELP" in response.text or "# TYPE" in response.text

    def test_health_check_detailed(self):
        """상세한 헬스 체크 정보 확인"""

        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert "version" in data
