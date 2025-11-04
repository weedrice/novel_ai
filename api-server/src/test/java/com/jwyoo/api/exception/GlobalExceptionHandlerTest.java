package com.jwyoo.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ResourceNotFoundException 처리 테스트")
    void handleResourceNotFoundException_Success() {
        // given
        ResourceNotFoundException exception = new ResourceNotFoundException("Character not found with id: 999");

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Character not found with id: 999");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("Validation 예외 처리 테스트")
    void handleValidationException_Success() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("characterDto", "name", "이름은 필수입니다");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        org.springframework.core.MethodParameter methodParameter = mock(org.springframework.core.MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("입력값 검증 실패");
        assertThat(response.getBody().get("details")).isNotNull();
    }

    @Test
    @DisplayName("RestClientException 처리 테스트")
    void handleRestClientException_Success() {
        // given
        RestClientException exception = new RestClientException("External service error");

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleRestClientException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("message")).isEqualTo("외부 서비스 호출 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("ResourceAccessException 처리 테스트 (LLM 서버 연결 실패)")
    void handleResourceAccessException_Success() {
        // given
        ResourceAccessException exception = new ResourceAccessException("Connection timeout");

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleLlmServerConnectionError(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(503);
        assertThat(response.getBody().get("message")).isEqualTo("LLM 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("일반 예외 처리 테스트")
    void handleGeneralException_Success() {
        // given
        Exception exception = new RuntimeException("Unexpected error");

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleGeneralException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("message")).isEqualTo("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
    }

    @Test
    @DisplayName("NoResourceFoundException 처리 테스트 - actuator 경로")
    void handleNoResourceFound_ActuatorPath() {
        // given
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getMessage()).thenReturn("No static resource actuator/health");

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("NoResourceFoundException 처리 테스트 - 일반 경로")
    void handleNoResourceFound_GeneralPath() {
        // given
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getMessage()).thenReturn("No resource found for /api/unknown");

        // when
        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
    }
}
