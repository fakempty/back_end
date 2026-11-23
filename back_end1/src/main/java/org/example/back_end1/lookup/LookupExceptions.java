package org.example.back_end1.lookup;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class LookupExceptionHandler {

    record ApiError(int status, String error, String message, Instant timestamp) {}

    @ExceptionHandler(PartNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(PartNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(LookupServiceUnavailableException.class)
    ResponseEntity<ApiError> handleUnavailable(LookupServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(503, "Service Unavailable", ex.getMessage(), Instant.now()));
    }
}