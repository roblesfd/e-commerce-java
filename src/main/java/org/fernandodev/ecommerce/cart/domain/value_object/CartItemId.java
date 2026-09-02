package org.fernandodev.ecommerce.cart.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un item de carrito dentro del agregado Cart.
 */
public record CartItemId(UUID value) {

    public CartItemId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static CartItemId generate() {
        return new CartItemId(UUID.randomUUID());
    }

    public static CartItemId of(UUID value) {
        return new CartItemId(value);
    }
}