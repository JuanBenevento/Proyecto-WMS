package com.juanbenevento.wms.shared.infrastructure.rest;

import com.juanbenevento.wms.shared.domain.exception.DomainException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * Converts all exceptions to standardized ApiResponse format.
 * 
 * IMPORTANT: Always log the ROOT CAUSE, not just the surface exception.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle domain-specific exceptions.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        log.warn("Domain exception: {}", ex.getMessage());
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "DOMAIN_ERROR";
        
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(ex.getMessage(), errorCode));
    }

    /**
     * Handle validation exceptions from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        log.warn("Validation failed: {}", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.validationError("Validation failed", errors));
    }

    /**
     * Handle constraint violations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(path, message);
        });
        
        log.warn("Constraint violation: {}", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.validationError("Validation failed", errors));
    }

    /**
     * Handle IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), "BAD_REQUEST"));
    }

    /**
     * Handle IllegalStateException.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(), "CONFLICT"));
    }

    /**
     * Handle DataAccessException specifically with better messages.
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(
            org.springframework.dao.DataAccessException ex) {
        
        // Get root cause
        Throwable root = getRootCause(ex);
        String userMessage = "Error de base de datos";
        
        // Provide specific messages based on root cause
        if (root instanceof org.hibernate.exception.ConstraintViolationException) {
            userMessage = "Violación de restricción: el dato ya existe o viola una regla de negocio";
        } else if (root.getMessage() != null && root.getMessage().contains("tenant")) {
            userMessage = "Error de configuración de tenant. Contacte al administrador.";
        } else if (root instanceof java.sql.SQLException) {
            userMessage = "Error de base de datos: " + root.getMessage();
        } else {
            userMessage = "Error de base de datos: " + root.getMessage();
        }
        
        // Log con detalle para el desarrollador
        log.error("DataAccessException - User seeing: {} - Full error:", userMessage, ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(userMessage, "DB_ERROR"));
    }

    /**
     * Handle all other uncaught exceptions - with ROOT CAUSE logging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // Get root cause for better message
        Throwable root = getRootCause(ex);
        String rootMessage = root.getMessage();
        
        if (rootMessage == null || rootMessage.isBlank()) {
            rootMessage = root.getClass().getSimpleName();
        }
        
        // Log THE ROOT CAUSE, not the wrapper
        log.error("Unhandled exception. Root cause: {} (of type: {})", 
                   rootMessage, root.getClass().getSimpleName(), ex);
        
        // User-friendly message
        String userMessage = "Error interno del servidor. Details: " + rootMessage;
        
        // Check for specific common errors
        if (root instanceof IllegalStateException) {
            userMessage = "Estado inválido: " + rootMessage;
        } else if (root instanceof IllegalArgumentException) {
            userMessage = "Datos inválidos: " + rootMessage;
        } else if (root instanceof NullPointerException) {
            userMessage = "Error de configuración. Faltan datos requeridos.";
        }
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(userMessage, "INTERNAL_ERROR"));
    }
    
    /**
     * Helper to get the actual root cause of an exception.
     */
    private Throwable getRootCause(Throwable ex) {
        while (ex.getCause() != null && ex.getCause() != ex) {
            ex = ex.getCause();
        }
        return ex;
    }
}