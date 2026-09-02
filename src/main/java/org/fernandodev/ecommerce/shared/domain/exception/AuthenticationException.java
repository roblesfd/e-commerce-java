package org.fernandodev.ecommerce.shared.domain.exception;

/**
 * Indica un error de autenticación (p. ej. firma de webhook inválida).
 */
public class AuthenticationException extends DomainException {

    public AuthenticationException(String message) {
        super(ErrorCode.AUTHENTICATION_ERROR, message);
    }
}