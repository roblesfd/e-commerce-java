package org.fernandodev.ecommerce.shared.domain.exception;

/**
 * Tipos de error que el dominio puede producir.
 * <p>
 * Clasifican las excepciones para que los adaptadores HTTP puedan traducirlas
 * a respuestas de error estandarizadas sin conocer los detalles internos del
 * dominio (spec §33).
 * </p>
 */
public enum ErrorCode {

    VALIDATION_ERROR,
    BUSINESS_RULE_VIOLATION,
    AUTHENTICATION_ERROR,
    AUTHORIZATION_ERROR,
    RESOURCE_NOT_FOUND,
    CONFLICT,
    EXTERNAL_SERVICE_FAILURE,
    INTERNAL_ERROR
}