package org.fernandodev.ecommerce.shipping.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Entrada del historial de estados de un envío.
 */
public record ShipmentStatusEntry(ShipmentStatus status, Instant at) {

    public ShipmentStatusEntry {
        Objects.requireNonNull(status, "status no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}