package org.fernandodev.ecommerce.returns.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una solicitud de devolución.
 */
public record ReturnId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ReturnId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ReturnId generate() {
        return new ReturnId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ReturnId} con el valor dado.
     */
    public static ReturnId of(UUID value) {
        return new ReturnId(value);
    }
}