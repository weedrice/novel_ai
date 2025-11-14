"""
커스텀 예외 클래스
"""


class LLMServerException(Exception):
    """LLM Server 기본 예외 클래스"""

    def __init__(self, message: str, details: dict = None):
        self.message = message
        self.details = details or {}
        super().__init__(self.message)


class LLMProviderError(LLMServerException):
    """LLM Provider 관련 예외"""

    pass


class ProviderNotAvailableError(LLMProviderError):
    """LLM Provider가 사용 불가능할 때 발생"""

    def __init__(self, provider: str):
        super().__init__(
            f"LLM Provider '{provider}' is not available",
            details={"provider": provider}
        )


class ProviderAPIError(LLMProviderError):
    """LLM Provider API 호출 실패 시 발생"""

    def __init__(self, provider: str, original_error: Exception):
        super().__init__(
            f"LLM Provider '{provider}' API call failed: {str(original_error)}",
            details={"provider": provider, "original_error": str(original_error)}
        )


class PromptGenerationError(LLMServerException):
    """프롬프트 생성 실패 시 발생"""

    def __init__(self, reason: str):
        super().__init__(
            f"Prompt generation failed: {reason}",
            details={"reason": reason}
        )


class JSONParsingError(LLMServerException):
    """JSON 파싱 실패 시 발생"""

    def __init__(self, content: str, original_error: Exception = None):
        super().__init__(
            f"JSON parsing failed: {str(original_error) if original_error else 'Invalid JSON'}",
            details={
                "content_preview": content[:200] if content else None,
                "original_error": str(original_error) if original_error else None
            }
        )


class ValidationError(LLMServerException):
    """입력 검증 실패 시 발생"""

    def __init__(self, field: str, reason: str):
        super().__init__(
            f"Validation failed for '{field}': {reason}",
            details={"field": field, "reason": reason}
        )


class ServiceError(LLMServerException):
    """서비스 레이어에서 발생하는 일반 오류"""

    def __init__(self, service_name: str, operation: str, reason: str):
        super().__init__(
            f"Service '{service_name}' failed during '{operation}': {reason}",
            details={
                "service": service_name,
                "operation": operation,
                "reason": reason
            }
        )
