package org.example.back_end1.resilience;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class Lab10ResilienceExceptionHandler {

    record ResilienceErrorResponse(int status, String error, String message, Instant timestamp) {}

    @ExceptionHandler(CircuitOpenException.class)
    ResponseEntity<ResilienceErrorResponse> handleCircuitOpen(CircuitOpenException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ResilienceErrorResponse(
                        503,
                        "Service Unavailable",
                        ex.getMessage(),
                        Instant.now()
                ));
    }
}