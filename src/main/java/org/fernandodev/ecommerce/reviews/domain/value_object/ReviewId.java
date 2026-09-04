package org.fernandodev.ecommerce.reviews.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un review.
 */
public record ReviewId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ReviewId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ReviewId generate() {
        return new ReviewId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ReviewId} con el valor dado.
     */
    public static ReviewId of(UUID value) {
        return new ReviewId(value);
    }
}