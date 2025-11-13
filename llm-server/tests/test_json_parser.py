"""
JSON Parser 유닛 테스트
LLM 응답에서 JSON 추출 및 파싱 로직 테스트
"""

import pytest
from app.utils.json_parser import JSONParser


class TestJSONParser:
    """JSONParser 테스트 클래스"""

    # ============================================================
    # extract_json_from_response 테스트
    # ============================================================

    def test_extract_json_from_plain_json(self):
        """일반 JSON 텍스트에서 추출"""
        response = '{"name": "Alice", "age": 30}'
        result = JSONParser.extract_json_from_response(response)
        assert result == '{"name": "Alice", "age": 30}'

    def test_extract_json_from_markdown_json_block(self):
        """```json 마크다운 블록에서 JSON 추출"""
        response = '''```json
{
  "name": "Bob",
  "age": 25
}
```'''
        result = JSONParser.extract_json_from_response(response)
        assert '"name": "Bob"' in result
        assert '```' not in result

    def test_extract_json_from_markdown_generic_block(self):
        """``` 일반 마크다운 블록에서 JSON 추출"""
        response = '''```
{"name": "Charlie", "age": 35}
```'''
        result = JSONParser.extract_json_from_response(response)
        assert '"name": "Charlie"' in result
        assert '```' not in result

    def test_extract_json_with_surrounding_text(self):
        """JSON 전후에 텍스트가 있는 경우"""
        response = '''Here is the result:
```json
{"status": "success"}
```
That's it!'''
        result = JSONParser.extract_json_from_response(response)
        assert '"status": "success"' in result
        assert 'Here is' not in result
        assert "That's it" not in result

    def test_extract_json_from_empty_string(self):
        """빈 문자열 처리"""
        result = JSONParser.extract_json_from_response("")
        assert result is None

    def test_extract_json_from_none(self):
        """None 입력 처리"""
        result = JSONParser.extract_json_from_response(None)
        assert result is None

    def test_extract_json_with_whitespace(self):
        """공백이 많은 JSON"""
        response = '''

        {"key": "value"}

        '''
        result = JSONParser.extract_json_from_response(response)
        assert result == '{"key": "value"}'

    def test_extract_json_from_malformed_markdown(self):
        """잘못된 마크다운 블록"""
        response = '```json\n{"name": "Alice"}'  # 닫는 ``` 없음
        result = JSONParser.extract_json_from_response(response)
        # 실패하더라도 원본 반환
        assert result is not None

    # ============================================================
    # parse_json_response 테스트
    # ============================================================

    def test_parse_valid_json_response(self):
        """유효한 JSON 파싱"""
        response = '{"name": "Alice", "age": 30, "city": "Seoul"}'
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["name"] == "Alice"
        assert result["age"] == 30
        assert result["city"] == "Seoul"

    def test_parse_json_from_markdown(self):
        """마크다운 블록의 JSON 파싱"""
        response = '''```json
{
  "characters": [
    {"name": "Alice"},
    {"name": "Bob"}
  ]
}
```'''
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert "characters" in result
        assert len(result["characters"]) == 2

    def test_parse_invalid_json(self):
        """잘못된 JSON 처리"""
        response = '{"name": "Alice", age: 30}'  # 키에 따옴표 없음
        result = JSONParser.parse_json_response(response)
        assert result is None

    def test_parse_empty_json(self):
        """빈 JSON 객체"""
        response = '{}'
        result = JSONParser.parse_json_response(response)
        assert result == {}

    def test_parse_json_array(self):
        """JSON 배열 파싱"""
        response = '[{"id": 1}, {"id": 2}]'
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert isinstance(result, list)
        assert len(result) == 2

    def test_parse_nested_json(self):
        """중첩된 JSON 구조"""
        response = '''```json
{
  "user": {
    "name": "Alice",
    "address": {
      "city": "Seoul",
      "country": "Korea"
    }
  }
}
```'''
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["user"]["name"] == "Alice"
        assert result["user"]["address"]["city"] == "Seoul"

    def test_parse_json_with_unicode(self):
        """유니코드 문자가 포함된 JSON"""
        response = '{"message": "안녕하세요", "emoji": "🎉"}'
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["message"] == "안녕하세요"
        assert result["emoji"] == "🎉"

    def test_parse_json_with_escaped_characters(self):
        """이스케이프 문자가 포함된 JSON"""
        response = r'{"text": "Line 1\nLine 2\tTabbed"}'
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert "\n" in result["text"]
        assert "\t" in result["text"]

    def test_parse_empty_string(self):
        """빈 문자열 파싱"""
        result = JSONParser.parse_json_response("")
        assert result is None

    def test_parse_none(self):
        """None 파싱"""
        result = JSONParser.parse_json_response(None)
        assert result is None

    def test_parse_json_with_boolean_and_null(self):
        """Boolean 및 null 값"""
        response = '{"active": true, "deleted": false, "data": null}'
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["active"] is True
        assert result["deleted"] is False
        assert result["data"] is None

    def test_parse_json_with_numbers(self):
        """다양한 숫자 타입"""
        response = '{"integer": 42, "float": 3.14, "negative": -10, "scientific": 1.5e-10}'
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["integer"] == 42
        assert result["float"] == 3.14
        assert result["negative"] == -10
        assert result["scientific"] == 1.5e-10

    # ============================================================
    # safe_get 테스트
    # ============================================================

    def test_safe_get_existing_key(self):
        """존재하는 키 가져오기"""
        data = {"name": "Alice", "age": 30}
        result = JSONParser.safe_get(data, "name")
        assert result == "Alice"

    def test_safe_get_missing_key_with_default(self):
        """존재하지 않는 키, 기본값 사용"""
        data = {"name": "Alice"}
        result = JSONParser.safe_get(data, "age", default=0)
        assert result == 0

    def test_safe_get_missing_key_without_default(self):
        """존재하지 않는 키, 기본값 없음"""
        data = {"name": "Alice"}
        result = JSONParser.safe_get(data, "age")
        assert result is None

    def test_safe_get_from_none(self):
        """None 딕셔너리에서 가져오기"""
        result = JSONParser.safe_get(None, "key", default="default")
        assert result == "default"

    def test_safe_get_with_none_default(self):
        """None 기본값"""
        data = {"name": "Alice"}
        result = JSONParser.safe_get(data, "missing", default=None)
        assert result is None

    def test_safe_get_nested_dict(self):
        """중첩된 딕셔너리"""
        data = {"user": {"name": "Alice", "age": 30}}
        result = JSONParser.safe_get(data, "user")
        assert result == {"name": "Alice", "age": 30}

    def test_safe_get_with_empty_dict(self):
        """빈 딕셔너리"""
        data = {}
        result = JSONParser.safe_get(data, "key", default="default")
        assert result == "default"

    # ============================================================
    # 실제 LLM 응답 시뮬레이션 테스트
    # ============================================================

    def test_parse_llm_response_with_explanation(self):
        """LLM이 설명과 함께 JSON을 반환하는 경우"""
        response = '''Sure! Here's the analysis:

```json
{
  "characters": [
    {"name": "Alice", "role": "protagonist"},
    {"name": "Bob", "role": "antagonist"}
  ],
  "summary": "A story about two people"
}
```

I hope this helps!'''
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert len(result["characters"]) == 2
        assert result["summary"] == "A story about two people"

    def test_parse_llm_response_multiple_code_blocks(self):
        """여러 코드 블록이 있을 때 첫 번째 JSON만 파싱"""
        response = '''```json
{"result": "first"}
```

```json
{"result": "second"}
```'''
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["result"] == "first"

    def test_parse_llm_response_with_formatting(self):
        """LLM이 포맷팅된 JSON을 반환하는 경우"""
        response = '''```json
{
  "characters": [
    {
      "name": "Alice",
      "personality": "brave",
      "dialogueExamples": [
        "I won't give up!",
        "Let's do this together."
      ]
    }
  ]
}
```'''
        result = JSONParser.parse_json_response(response)
        assert result is not None
        assert result["characters"][0]["name"] == "Alice"
        assert len(result["characters"][0]["dialogueExamples"]) == 2


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
