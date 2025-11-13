"""
Main application tests
리팩토링된 구조에 맞춘 통합 테스트
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


# ============================================================
# System Endpoints
# ============================================================

def test_root_endpoint():
    """Test root endpoint returns server info"""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json()["message"] == "Character Tone LLM Server is running"
    assert response.json()["version"] == "0.3.0"


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


# ============================================================
# Dialogue Endpoints
# ============================================================

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
        "provider": "gemini"
    }
    response = client.post("/gen/suggest", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "candidates" in data
    # May use fallback if LLM not available
    assert len(data["candidates"]) >= 0


def test_suggest_with_max_length():
    """Test that suggestions respect max length"""
    payload = {
        "speakerId": "test",
        "targetIds": ["target"],
        "intent": "greet",
        "honorific": "banmal",
        "maxLen": 20,
        "nCandidates": 3
    }
    response = client.post("/gen/suggest", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert all(len(c["text"]) <= 20 for c in data["candidates"])


# ============================================================
# Scenario Endpoints
# ============================================================

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
        "provider": "gemini"
    }
    response = client.post("/gen/scenario", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "dialogues" in data
    assert len(data["dialogues"]) <= payload["dialogueCount"]


# ============================================================
# Script Analysis Endpoints
# ============================================================

def test_analyze_script_endpoint():
    """Test script analysis endpoint"""
    payload = {
        "content": "Alice: Hello Bob! How are you?\nBob: I'm doing well, thanks Alice.",
        "formatHint": "scenario",
        "provider": "gemini"
    }
    response = client.post("/gen/analyze-script", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "characters" in data
    assert "dialogues" in data
    assert "scenes" in data
    assert "relationships" in data


# ============================================================
# Episode Analysis Endpoints
# ============================================================

def test_episode_summary_endpoint():
    """Test episode summary generation"""
    payload = {
        "scriptText": "This is a test script about a character going on an adventure.",
        "scriptFormat": "novel",
        "provider": "gemini"
    }
    response = client.post("/gen/episode/summary", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "summary" in data
    assert "keyPoints" in data
    assert "wordCount" in data


def test_episode_characters_endpoint():
    """Test episode character analysis"""
    payload = {
        "scriptText": "Alice walked into the room. Bob was sitting by the window.",
        "scriptFormat": "novel",
        "provider": "gemini"
    }
    response = client.post("/gen/episode/characters", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "characters" in data


def test_episode_scenes_endpoint():
    """Test episode scene analysis"""
    payload = {
        "scriptText": "Scene 1: Morning at the park. Scene 2: Afternoon at the cafe.",
        "scriptFormat": "novel",
        "provider": "gemini"
    }
    response = client.post("/gen/episode/scenes", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "scenes" in data


def test_episode_dialogues_endpoint():
    """Test episode dialogue analysis"""
    payload = {
        "scriptText": '"Hello," said Alice. "Hi there," Bob replied.',
        "scriptFormat": "novel",
        "provider": "gemini"
    }
    response = client.post("/gen/episode/dialogues", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "dialogues" in data
    assert "statistics" in data


def test_episode_spell_check_endpoint():
    """Test episode spell check"""
    payload = {
        "scriptText": "This is a test text for spell checking.",
        "scriptFormat": "novel",
        "provider": "gemini"
    }
    response = client.post("/gen/episode/spell-check", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "issues" in data
    assert "summary" in data


# ============================================================
# Error Handling
# ============================================================

def test_suggest_endpoint_missing_required_fields():
    """Test suggestion endpoint with missing required fields"""
    payload = {
        "intent": "greet"
        # Missing speakerId, targetIds, honorific
    }
    response = client.post("/gen/suggest", json=payload)
    assert response.status_code == 422  # Validation error


def test_scenario_endpoint_invalid_dialogue_count():
    """Test scenario endpoint with invalid dialogue count"""
    payload = {
        "sceneDescription": "Test scene",
        "participants": [
            {"characterId": "char1", "name": "Alice"}
        ],
        "dialogueCount": 100,  # Exceeds max limit
        "provider": "gemini"
    }
    response = client.post("/gen/scenario", json=payload)
    assert response.status_code == 422  # Validation error
