package org.fernandodev.ecommerce.checkout.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un checkout dentro del contexto Checkout.
 */
public record CheckoutId(UUID value) {

    public CheckoutId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static CheckoutId generate() {
        return new CheckoutId(UUID.randomUUID());
    }

    public static CheckoutId of(UUID value) {
        return new CheckoutId(value);
    }
}