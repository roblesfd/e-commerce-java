package org.fernandodev.ecommerce.reviews.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Entrada del historial de estados de un review.
 */
public record ReviewStatusEntry(ReviewStatus status, Instant at) {

    public ReviewStatusEntry {
        Objects.requireNonNull(status, "status no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}