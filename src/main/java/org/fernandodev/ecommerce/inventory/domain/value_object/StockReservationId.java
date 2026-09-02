package org.fernandodev.ecommerce.inventory.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una reserva de stock dentro del agregado Inventory.
 */
public record StockReservationId(UUID value) {

    public StockReservationId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static StockReservationId generate() {
        return new StockReservationId(UUID.randomUUID());
    }

    public static StockReservationId of(UUID value) {
        return new StockReservationId(value);
    }
}