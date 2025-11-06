import requests
import json

# Test data matching what the API server would send
test_data = {
    "speakerId": "char.seha",
    "targetIds": ["char.jiho"],
    "intent": "reconcile",
    "honorific": "banmal",
    "maxLen": 80,
    "nCandidates": 3,
    "characterInfo": {
        "name": "세하",
        "description": "주인공. 밝고 긍정적인 성격",
        "personality": "외향적, 낙천적",
        "speakingStyle": "반말, 친근한 어투",
        "vocabulary": "대박,진짜,ㅋㅋ",
        "toneKeywords": "밝음,경쾌함",
        "examples": "안녕? 나 세하야!\n오늘 날씨 진짜 좋다!\n대박! 이거 완전 재밌는데?\nㅋㅋ 그러게! 나도 그렇게 생각해.",
        "prohibitedWords": "~요,~습니다,~네요",
        "sentencePatterns": "~야!\n~지 뭐!\n대박 ~!\n완전 ~!"
    },
    "targetNames": ["지호"],
    "context": None,
    "provider": "gemini"
}

print("Sending request to LLM server...")
print(json.dumps(test_data, indent=2, ensure_ascii=False))

response = requests.post(
    "http://localhost:8000/gen/suggest",
    json=test_data,
    headers={"Content-Type": "application/json"}
)

print(f"\nStatus: {response.status_code}")
print(f"Response: {response.text}")

if response.status_code == 200:
    result = response.json()
    print("\nGenerated dialogues:")
    for candidate in result.get("candidates", []):
        print(f"  - {candidate['text']} (score: {candidate['score']})")