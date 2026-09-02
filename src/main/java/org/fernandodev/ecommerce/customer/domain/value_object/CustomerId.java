package org.fernandodev.ecommerce.customer.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador del cliente dentro de E-Commerce.
 */
public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}
