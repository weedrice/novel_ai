#!/usr/bin/env python3
"""Gemini API 테스트 스크립트"""

import requests
import json

# Test data - 올바른 API 스키마에 맞춤
test_payload = {
    "speakerId": "char.seha",
    "targetIds": ["char.minho"],
    "intent": "greet",
    "honorific": "banmal",
    "maxLen": 80,
    "nCandidates": 3,
    "characterInfo": {
        "name": "세하",
        "personality": "밝고 긍정적인 성격",
        "speakingStyle": "친근한 어투",
        "examples": "안녕! 오랜만이야!"
    },
    "targetNames": ["민호"],
    "context": "친구와 오랜만에 만났다",
    "provider": "gemini"
}

print("=== Gemini API 테스트 ===\n")
print(f"요청 데이터:")
print(json.dumps(test_payload, ensure_ascii=False, indent=2))
print("\n" + "="*50 + "\n")

# Call LLM server
response = requests.post(
    "http://localhost:8000/gen/suggest",
    json=test_payload,
    timeout=30
)

print(f"응답 상태 코드: {response.status_code}\n")

if response.status_code == 200:
    result = response.json()
    print("응답 전체 데이터:")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    print("\n생성된 대사 후보:")

    # Try different possible keys
    candidates = result.get("candidates", result.get("suggestions", []))

    if candidates:
        for i, candidate in enumerate(candidates, 1):
            if isinstance(candidate, dict):
                text = candidate.get("text", candidate.get("dialogue", str(candidate)))
            else:
                text = str(candidate)
            print(f"  {i}. {text}")
        print(f"\n총 {len(candidates)}개 생성됨")
    else:
        print("  (생성된 대사 없음)")
else:
    print("에러 발생:")
    print(response.text)
