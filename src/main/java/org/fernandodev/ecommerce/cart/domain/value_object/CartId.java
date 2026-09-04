package org.fernandodev.ecommerce.cart.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un carrito dentro del contexto Shopping Cart.
 */
public record CartId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public CartId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static CartId generate() {
        return new CartId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link CartId} con el valor dado.
     */
    public static CartId of(UUID value) {
        return new CartId(value);
    }
}