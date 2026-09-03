package org.fernandodev.ecommerce.shared.adapter.in.rest.error;

import org.fernandodev.ecommerce.shared.domain.exception.DomainException;
import org.fernandodev.ecommerce.shared.domain.exception.ErrorCode;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejo global de errores de la API.
 * <p>
 * Traduce excepciones de dominio y de validación a respuestas {@link ApiError}
 * consistentes, sin exponer detalles de infraestructura.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex, WebRequest request) {
        HttpStatus status = statusFor(ex.getErrorCode());
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false),
                MDC.get("correlationId"));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                "Validación fallida: " + errors,
                request.getDescription(false),
                MDC.get("correlationId"));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false),
                MDC.get("correlationId"));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                "Error interno del servidor",
                request.getDescription(false),
                MDC.get("correlationId"));
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus statusFor(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case BUSINESS_RULE_VIOLATION -> HttpStatus.UNPROCESSABLE_ENTITY;
            case AUTHENTICATION_ERROR -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION_ERROR -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case EXTERNAL_SERVICE_FAILURE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}