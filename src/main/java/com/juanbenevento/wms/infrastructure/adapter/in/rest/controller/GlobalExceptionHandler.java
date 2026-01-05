package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.domain.exception.*;
import com.juanbenevento.wms.application.ports.in.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =================================================================================
    // 1. ERRORES DE DOMINIO Y RECURSOS (404 Not Found)
    // =================================================================================

    @ExceptionHandler({
            ProductNotFoundException.class,
            LocationNotFoundException.class,
            InventoryItemNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleResourceNotFound(DomainException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), request);
    }

    // =================================================================================
    // 2. REGLAS DE NEGOCIO Y ESTADO (409 Conflict)
    // =================================================================================

    @ExceptionHandler(LocationCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> handleCapacityExceeded(LocationCapacityExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Capacidad Excedida", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de Estado", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConstraint(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Violación de Integridad de Datos", "No se puede realizar la operación porque el registro está en uso o duplica un dato único.", request);
    }

    // =================================================================================
    // 3. CONCURRENCIA (409 Conflict)
    // =================================================================================

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrencyError(Exception ex, HttpServletRequest request) {
        String mensajeUsuario = "El registro fue modificado por otro usuario mientras usted trabajaba. " +
                "Por favor, actualice la página e intente nuevamente.";
        return buildResponse(HttpStatus.CONFLICT, "Datos Desactualizados", mensajeUsuario, request);
    }

    // =================================================================================
    // 4. VALIDACIONES DE ENTRADA (400 Bad Request)
    // =================================================================================

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleGenericDomainException(DomainException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Regla de Negocio", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Parámetro Inválido", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de Validación", details, request);
    }

    // =================================================================================
    // 5. SEGURIDAD (401 / 403)
    // =================================================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Autenticación Fallida", "Usuario o contraseña incorrectos.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso Denegado", "No tiene permisos suficientes para realizar esta acción.", request);
    }

    // =================================================================================
    // 6. ERROR INTERNO (500 Internal Server Error)
    // =================================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("🔥 ERROR CRÍTICO NO CONTROLADO en {}: ", request.getRequestURI(), ex);

        // AL CLIENTE: Mensaje genérico seguro
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error Interno del Servidor",
                "Ocurrió un error inesperado. Por favor contacte al soporte técnico si el problema persiste.",
                request
        );
    }

    // =================================================================================
    // METODO PRIVADO HELPER (Para evitar código repetido)
    // =================================================================================

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorTitle, String message, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorTitle,
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, status);
    }
}