package org.fernandodev.ecommerce.wishlist.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una wishlist.
 */
public record WishlistId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public WishlistId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static WishlistId generate() {
        return new WishlistId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link WishlistId} con el valor dado.
     */
    public static WishlistId of(UUID value) {
        return new WishlistId(value);
    }
}