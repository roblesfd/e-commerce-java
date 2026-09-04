package org.fernandodev.ecommerce.shared.domain.exception;

import lombok.Getter;

/**
 * Excepción base del dominio de E-Commerce.
 * <p>
 * Transporta un {@link ErrorCode} que clasifica el fallo y permite a la capa
 * de adaptadores construir respuestas de error consistentes sin filtrar
 * detalles de infraestructura.
 * </p>
 */
@Getter
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructor.
     *
     * @param errorCode código de error.
     * @param message   mensaje.
     */
    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor con causa.
     *
     * @param errorCode código de error.
     * @param message   mensaje.
     * @param cause     causa raíz.
     */
    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}