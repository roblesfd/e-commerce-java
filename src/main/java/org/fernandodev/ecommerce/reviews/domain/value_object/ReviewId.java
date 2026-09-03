package org.fernandodev.ecommerce.reviews.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un review.
 */
public record ReviewId(UUID value) {

    public ReviewId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ReviewId generate() {
        return new ReviewId(UUID.randomUUID());
    }

    public static ReviewId of(UUID value) {
        return new ReviewId(value);
    }
}