package org.fernandodev.ecommerce.cart.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un carrito dentro del contexto Shopping Cart.
 */
public record CartId(UUID value) {

    public CartId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static CartId generate() {
        return new CartId(UUID.randomUUID());
    }

    public static CartId of(UUID value) {
        return new CartId(value);
    }
}