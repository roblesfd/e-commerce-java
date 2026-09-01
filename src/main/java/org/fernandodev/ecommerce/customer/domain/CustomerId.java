package org.fernandodev.ecommerce.customer.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador del cliente dentro de E-Commerce.
 */
public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}