package com.busticketbookingsystem.exception;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the new exception handlers added to GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("POSITIVE: DataIntegrityViolationException → 409 Conflict with generic message")
    void handleDataIntegrity() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("duplicate key", new RuntimeException("root cause"));

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Data Conflict", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("conflicts with existing data"));
        assertEquals("uri=/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("POSITIVE: OptimisticLockException → 409 Concurrent Modification")
    void handleOptimisticLock() {
        OptimisticLockException ex = new OptimisticLockException("row changed");

        ResponseEntity<ErrorResponse> response = handler.handleLockConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Concurrent Modification", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("retry"));
    }

    @Test
    @DisplayName("POSITIVE: Spring OptimisticLockingFailureException → 409")
    void handleSpringOptimistic() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("stale");

        ResponseEntity<ErrorResponse> response = handler.handleLockConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("POSITIVE: Spring PessimisticLockingFailureException → 409")
    void handleSpringPessimistic() {
        PessimisticLockingFailureException ex = new PessimisticLockingFailureException("timeout");

        ResponseEntity<ErrorResponse> response = handler.handleLockConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Concurrent Modification", response.getBody().getError());
    }

    @Test
    @DisplayName("NEGATIVE: generic Exception still routes to 500")
    void handleGeneric() {
        Exception ex = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleGlobal(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("boom", response.getBody().getMessage());
    }
}
