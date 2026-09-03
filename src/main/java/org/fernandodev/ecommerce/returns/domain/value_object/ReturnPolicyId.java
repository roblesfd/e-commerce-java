package org.fernandodev.ecommerce.returns.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una política de devoluciones.
 */
public record ReturnPolicyId(UUID value) {

    public ReturnPolicyId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ReturnPolicyId generate() {
        return new ReturnPolicyId(UUID.randomUUID());
    }

    public static ReturnPolicyId of(UUID value) {
        return new ReturnPolicyId(value);
    }
}