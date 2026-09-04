package org.fernandodev.ecommerce.returns.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una política de devoluciones.
 */
public record ReturnPolicyId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ReturnPolicyId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ReturnPolicyId generate() {
        return new ReturnPolicyId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ReturnPolicyId} con el valor dado.
     */
    public static ReturnPolicyId of(UUID value) {
        return new ReturnPolicyId(value);
    }
}