import pytest
from app.services.prompt_builder import PromptBuilder


def test_build_system_prompt_minimal():
    """Test building system prompt with minimal character info"""
    character_info = {
        "name": "Alice"
    }

    prompt = PromptBuilder.build_system_prompt(character_info)

    assert "Alice" in prompt
    assert "Character Profile" in prompt
    assert "Output Rules" in prompt
    assert len(prompt) > 0


def test_build_system_prompt_full():
    """Test building system prompt with full character info"""
    character_info = {
        "name": "Alice",
        "description": "A cheerful person",
        "personality": "friendly and optimistic",
        "speakingStyle": "casual and warm",
        "vocabulary": "Hey, awesome, cool",
        "toneKeywords": "bright, positive",
        "examples": "Hey there!\nHow's it going?",
        "prohibitedWords": "never, hate",
        "sentencePatterns": "Pattern 1\nPattern 2"
    }

    prompt = PromptBuilder.build_system_prompt(character_info)

    assert "Alice" in prompt
    assert "cheerful person" in prompt
    assert "friendly and optimistic" in prompt
    assert "casual and warm" in prompt
    assert "Hey, awesome, cool" in prompt
    assert "bright, positive" in prompt
    assert "Hey there!" in prompt
    assert "never, hate" in prompt
    assert "Pattern 1" in prompt


def test_build_system_prompt_with_empty_values():
    """Test system prompt with empty string values"""
    character_info = {
        "name": "Bob",
        "description": "",
        "personality": "",
        "speakingStyle": "",
        "vocabulary": "",
        "toneKeywords": "",
        "examples": "",
        "prohibitedWords": "",
        "sentencePatterns": ""
    }

    prompt = PromptBuilder.build_system_prompt(character_info)

    assert "Bob" in prompt
    assert len(prompt) > 0


def test_build_user_prompt_basic():
    """Test building user prompt with basic parameters"""
    prompt = PromptBuilder.build_user_prompt(
        intent="greet",
        honorific="banmal",
        target_names=["Bob", "Charlie"],
        max_len=50,
        n_candidates=3
    )

    assert "greet" in prompt
    assert "banmal" in prompt
    assert "Bob, Charlie" in prompt
    assert "50" in prompt
    assert "3" in prompt
    assert "Output Format" in prompt


def test_build_user_prompt_with_context():
    """Test user prompt with additional context"""
    prompt = PromptBuilder.build_user_prompt(
        intent="comfort",
        honorific="jondae",
        target_names=["Alice"],
        max_len=80,
        n_candidates=2,
        context="After a difficult exam"
    )

    assert "comfort" in prompt
    assert "jondae" in prompt
    assert "Alice" in prompt
    assert "After a difficult exam" in prompt


def test_build_user_prompt_empty_targets():
    """Test user prompt with empty target names"""
    prompt = PromptBuilder.build_user_prompt(
        intent="greet",
        honorific="banmal",
        target_names=[],
        max_len=50,
        n_candidates=3
    )

    assert "unknown" in prompt


def test_build_full_prompt():
    """Test building both system and user prompts together"""
    character_info = {
        "name": "Alice",
        "description": "Friendly person",
        "personality": "cheerful",
        "speakingStyle": "casual"
    }

    system_prompt, user_prompt = PromptBuilder.build_full_prompt(
        character_info=character_info,
        intent="greet",
        honorific="banmal",
        target_names=["Bob"],
        max_len=50,
        n_candidates=3,
        context="First meeting"
    )

    # Check system prompt
    assert "Alice" in system_prompt
    assert "Friendly person" in system_prompt
    assert "cheerful" in system_prompt

    # Check user prompt
    assert "greet" in user_prompt
    assert "banmal" in user_prompt
    assert "Bob" in user_prompt
    assert "First meeting" in user_prompt

    # Check both prompts are non-empty
    assert len(system_prompt) > 0
    assert len(user_prompt) > 0


def test_build_full_prompt_without_context():
    """Test full prompt building without optional context"""
    character_info = {
        "name": "Bob",
        "personality": "serious"
    }

    system_prompt, user_prompt = PromptBuilder.build_full_prompt(
        character_info=character_info,
        intent="argue",
        honorific="mixed",
        target_names=["Alice"],
        max_len=100,
        n_candidates=5
    )

    assert "Bob" in system_prompt
    assert "argue" in user_prompt
    assert "mixed" in user_prompt
    assert len(system_prompt) > 0
    assert len(user_prompt) > 0


def test_honorific_styles():
    """Test different honorific styles in user prompt"""
    honorifics = ["banmal", "jondae", "mixed"]

    for honorific in honorifics:
        prompt = PromptBuilder.build_user_prompt(
            intent="greet",
            honorific=honorific,
            target_names=["Test"],
            max_len=50,
            n_candidates=2
        )
        assert honorific in prompt


def test_various_intents():
    """Test different intent types"""
    intents = ["reconcile", "argue", "comfort", "greet", "thank", "question"]

    for intent in intents:
        prompt = PromptBuilder.build_user_prompt(
            intent=intent,
            honorific="banmal",
            target_names=["Test"],
            max_len=50,
            n_candidates=2
        )
        assert intent in prompt


def test_prompt_length_constraints():
    """Test that max_len constraint is included in prompt"""
    max_lengths = [20, 50, 100, 200]

    for max_len in max_lengths:
        prompt = PromptBuilder.build_user_prompt(
            intent="greet",
            honorific="banmal",
            target_names=["Test"],
            max_len=max_len,
            n_candidates=3
        )
        assert str(max_len) in prompt


def test_multiple_targets():
    """Test user prompt with multiple target names"""
    targets = ["Alice", "Bob", "Charlie", "David"]

    prompt = PromptBuilder.build_user_prompt(
        intent="greet",
        honorific="banmal",
        target_names=targets,
        max_len=50,
        n_candidates=3
    )

    for target in targets:
        assert target in prompt


def test_system_prompt_multiline_examples():
    """Test system prompt with multiline examples"""
    character_info = {
        "name": "Alice",
        "examples": "Line 1\nLine 2\nLine 3"
    }

    prompt = PromptBuilder.build_system_prompt(character_info)

    assert "Line 1" in prompt
    assert "Line 2" in prompt
    assert "Line 3" in prompt


def test_system_prompt_multiline_patterns():
    """Test system prompt with multiline sentence patterns"""
    character_info = {
        "name": "Bob",
        "sentencePatterns": "Pattern A\nPattern B\nPattern C"
    }

    prompt = PromptBuilder.build_system_prompt(character_info)

    assert "Pattern A" in prompt
    assert "Pattern B" in prompt
    assert "Pattern C" in prompt
    assert "Sentence Patterns" in prompt
