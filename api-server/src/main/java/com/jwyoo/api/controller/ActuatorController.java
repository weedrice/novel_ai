package com.jwyoo.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Actuator 엔드포인트 요청을 처리하는 더미 컨트롤러
 */
@RestController
@RequestMapping("/actuator")
public class ActuatorController {

    @GetMapping("/**")
    public ResponseEntity<Map<String, String>> handleActuatorRequests() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Actuator endpoints are not enabled"));
    }
}
