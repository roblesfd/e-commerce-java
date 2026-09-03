package org.fernandodev.ecommerce.wishlist.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una wishlist.
 */
public record WishlistId(UUID value) {

    public WishlistId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static WishlistId generate() {
        return new WishlistId(UUID.randomUUID());
    }

    public static WishlistId of(UUID value) {
        return new WishlistId(value);
    }
}