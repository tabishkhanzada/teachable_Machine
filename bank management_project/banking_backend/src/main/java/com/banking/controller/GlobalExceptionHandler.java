package com.banking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Translates service/validation exceptions into clean JSON error responses
 * so the desktop client can show the real reason instead of a blank HTTP 500.
 *
 * NOTE: this does not change any business logic or scheduling algorithm —
 * it only formats the error that the services already throw.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Validation failures (bad amount, account not found, etc.) -> 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", message(ex)));
    }

    /** State conflicts (blocked user, request not pending, etc.) -> 409. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", message(ex)));
    }

    /** Anything unexpected -> 500, but still with a readable message. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", message(ex)));
    }

    private String message(Throwable ex) {
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank()) ? ex.getClass().getSimpleName() : msg;
    }
}
