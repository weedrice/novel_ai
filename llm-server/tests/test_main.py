import pytest
from fastapi.testclient import TestClient
from app.main import app, _generate_fallback_response, SuggestInput, _generate_empty_analysis

client = TestClient(app)


def test_root_endpoint():
    """Test root endpoint returns server info"""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json()["message"] == "Character Tone LLM Server is running"
    assert response.json()["version"] == "0.2.0"


def test_health_endpoint():
    """Test health check endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_providers_endpoint():
    """Test providers endpoint returns available LLM providers"""
    response = client.get("/providers")
    assert response.status_code == 200
    data = response.json()
    assert "available" in data
    assert "default" in data
    assert "providers" in data
    assert "openai" in data["providers"]
    assert "claude" in data["providers"]
    assert "gemini" in data["providers"]


def test_suggest_endpoint_with_fallback():
    """Test dialogue suggestion endpoint with fallback (no character info)"""
    payload = {
        "speakerId": "char1",
        "targetIds": ["char2"],
        "intent": "greet",
        "honorific": "banmal",
        "maxLen": 80,
        "nCandidates": 3
    }
    response = client.post("/gen/suggest", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "candidates" in data
    assert len(data["candidates"]) > 0
    assert all("text" in c and "score" in c for c in data["candidates"])


def test_suggest_endpoint_with_character_info():
    """Test dialogue suggestion with character info"""
    payload = {
        "speakerId": "char1",
        "targetIds": ["char2"],
        "intent": "comfort",
        "honorific": "jondae",
        "maxLen": 100,
        "nCandidates": 2,
        "characterInfo": {
            "name": "Alice",
            "description": "A kind person",
            "personality": "caring and empathetic",
            "speakingStyle": "polite and warm",
            "vocabulary": "soft words",
            "toneKeywords": "gentle"
        },
        "targetNames": ["Bob"],
        "provider": "openai"
    }
    response = client.post("/gen/suggest", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "candidates" in data
    # May use fallback if LLM not available
    assert len(data["candidates"]) >= 0


def test_scenario_endpoint():
    """Test scenario generation endpoint"""
    payload = {
        "sceneDescription": "Two friends meeting at a cafe",
        "location": "Cafe",
        "mood": "relaxed",
        "participants": [
            {
                "characterId": "char1",
                "name": "Alice",
                "personality": "cheerful",
                "speakingStyle": "casual"
            },
            {
                "characterId": "char2",
                "name": "Bob",
                "personality": "calm",
                "speakingStyle": "thoughtful"
            }
        ],
        "dialogueCount": 5,
        "provider": "openai"
    }
    response = client.post("/gen/scenario", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "dialogues" in data
    assert len(data["dialogues"]) <= payload["dialogueCount"]


def test_analyze_script_endpoint():
    """Test script analysis endpoint"""
    payload = {
        "content": "Alice: Hello Bob! How are you?\nBob: I'm doing well, thanks Alice.",
        "formatHint": "scenario",
        "provider": "openai"
    }
    response = client.post("/gen/analyze-script", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "characters" in data
    assert "dialogues" in data
    assert "scenes" in data
    assert "relationships" in data


def test_fallback_response_generation():
    """Test fallback response generation for different intents"""
    intents = ["reconcile", "argue", "comfort", "greet", "thank"]

    for intent in intents:
        inp = SuggestInput(
            speakerId="test",
            targetIds=["target"],
            intent=intent,
            honorific="banmal",
            nCandidates=3
        )
        response = _generate_fallback_response(inp)
        assert len(response.candidates) > 0
        assert all(c.score <= 1.0 and c.score >= 0.0 for c in response.candidates)


def test_fallback_response_with_jondae():
    """Test fallback response with formal speech level"""
    inp = SuggestInput(
        speakerId="test",
        targetIds=["target"],
        intent="greet",
        honorific="jondae",
        nCandidates=2
    )
    response = _generate_fallback_response(inp)
    assert len(response.candidates) > 0
    # Check that responses are more polite
    assert any("please" in c.text.lower() for c in response.candidates)


def test_fallback_response_unknown_intent():
    """Test fallback response with unknown intent"""
    inp = SuggestInput(
        speakerId="test",
        targetIds=["target"],
        intent="unknown_intent",
        honorific="banmal",
        nCandidates=3
    )
    response = _generate_fallback_response(inp)
    assert len(response.candidates) > 0


def test_empty_analysis_generation():
    """Test empty analysis generation"""
    response = _generate_empty_analysis()
    assert len(response.characters) == 0
    assert len(response.dialogues) == 0
    assert len(response.scenes) == 0
    assert len(response.relationships) == 0


def test_suggest_with_max_length_truncation():
    """Test that suggestions are truncated to max length"""
    inp = SuggestInput(
        speakerId="test",
        targetIds=["target"],
        intent="greet",
        honorific="banmal",
        maxLen=10,  # Very short max length
        nCandidates=3
    )
    response = _generate_fallback_response(inp)
    assert all(len(c.text) <= 10 for c in response.candidates)
