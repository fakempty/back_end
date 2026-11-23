package org.example.back_end1.lookup;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Lab9ErrorAndConfigTest {

    @Test
    void mapsNotFoundTo404AndUnavailableTo503() {
        LookupExceptionHandler handler = new LookupExceptionHandler();

        ResponseEntity<LookupExceptionHandler.ApiError> r404 =
                handler.handleNotFound(new PartNotFoundException("missing"));
        ResponseEntity<LookupExceptionHandler.ApiError> r503 =
                handler.handleUnavailable(new LookupServiceUnavailableException("down", null));

        assertEquals(HttpStatus.NOT_FOUND, r404.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r503.getStatusCode());
    }

    @Test
    void rejectsUnknownTransportOnStartupValidation() {
        Lab9LookupProperties props = new Lab9LookupProperties();
        props.setTransport("invalid-mode");

        assertThrows(IllegalStateException.class, props::validateTransport);
    }
}