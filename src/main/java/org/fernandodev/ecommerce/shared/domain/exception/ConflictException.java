package org.fernandodev.ecommerce.shared.domain.exception;

/**
 * Indica un conflicto de estado que impide completar la operación.
 * <p>
 * Por ejemplo, crear una entidad que ya existe o realizar una transición de
 * estado inválida.
 * </p>
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}