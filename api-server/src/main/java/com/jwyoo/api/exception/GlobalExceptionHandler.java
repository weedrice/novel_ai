package com.jwyoo.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 통합 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 처리 (리소스를 찾을 수 없음)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "입력값 검증 실패",
                errors
        );
    }

    /**
     * LLM 서버 연결 오류 처리
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleLlmServerConnectionError(ResourceAccessException ex) {
        log.error("LLM server connection error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "LLM 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.",
                null
        );
    }

    /**
     * RestClient 예외 처리
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException ex) {
        log.error("RestClient error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "외부 서비스 호출 중 오류가 발생했습니다.",
                null
        );
    }

    /**
     * NoResourceFoundException 예외 처리 (actuator 등 설정되지 않은 리소스 요청)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        String message = ex.getMessage();

        // actuator 관련 요청은 로그를 남기지 않고 조용히 처리
        if (message != null && message.contains("actuator")) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", null);
        }

        log.warn("Resource not found: {}", message);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", null);
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        // NoResourceFoundException은 이미 위에서 처리되므로 제외
        if (ex instanceof NoResourceFoundException) {
            return handleNoResourceFound((NoResourceFoundException) ex);
        }

        log.error("Unexpected error occurred", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.",
                null
        );
    }

    /**
     * 에러 응답 생성 헬퍼 메서드
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            Object details
    ) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        if (details != null) {
            errorResponse.put("details", details);
        }

        return ResponseEntity.status(status).body(errorResponse);
    }
}