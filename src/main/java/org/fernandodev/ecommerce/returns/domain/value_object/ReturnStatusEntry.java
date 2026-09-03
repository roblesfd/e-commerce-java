package org.fernandodev.ecommerce.returns.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Entrada del historial de estados de una devolución.
 */
public record ReturnStatusEntry(ReturnStatus status, Instant at) {

    public ReturnStatusEntry {
        Objects.requireNonNull(status, "status no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}