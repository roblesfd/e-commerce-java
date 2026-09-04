package org.fernandodev.ecommerce.cart.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un item de carrito dentro del agregado Cart.
 */
public record CartItemId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public CartItemId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static CartItemId generate() {
        return new CartItemId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link CartItemId} con el valor dado.
     */
    public static CartItemId of(UUID value) {
        return new CartItemId(value);
    }
}