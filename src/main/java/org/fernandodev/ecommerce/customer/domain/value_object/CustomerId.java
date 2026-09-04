package org.fernandodev.ecommerce.customer.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador del cliente dentro de E-Commerce.
 */
public record CustomerId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public CustomerId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link CustomerId} con el valor dado.
     */
    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}
