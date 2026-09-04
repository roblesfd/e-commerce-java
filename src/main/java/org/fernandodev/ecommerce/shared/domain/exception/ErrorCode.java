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

    /** Error de validación de entrada. */
    VALIDATION_ERROR,
    /** Violación de una regla de negocio. */
    BUSINESS_RULE_VIOLATION,
    /** Error de autenticación. */
    AUTHENTICATION_ERROR,
    /** Error de autorización. */
    AUTHORIZATION_ERROR,
    /** Recurso no encontrado. */
    RESOURCE_NOT_FOUND,
    /** Conflicto de estado. */
    CONFLICT,
    /** Fallo de un servicio externo. */
    EXTERNAL_SERVICE_FAILURE,
    /** Error interno. */
    INTERNAL_ERROR
}