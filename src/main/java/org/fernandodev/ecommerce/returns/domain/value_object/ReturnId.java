package org.fernandodev.ecommerce.returns.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una solicitud de devolución.
 */
public record ReturnId(UUID value) {

    public ReturnId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ReturnId generate() {
        return new ReturnId(UUID.randomUUID());
    }

    public static ReturnId of(UUID value) {
        return new ReturnId(value);
    }
}