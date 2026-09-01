package org.fernandodev.ecommerce.shared.domain.exception;

/**
 * Indica que un recurso solicitado no existe.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}