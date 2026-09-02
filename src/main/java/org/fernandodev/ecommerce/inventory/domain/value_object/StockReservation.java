package org.fernandodev.ecommerce.inventory.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Reserva temporal de stock dentro del agregado Inventory.
 *
 * @param id         identificador de la reserva.
 * @param quantity   cantidad reservada.
 * @param status     estado actual de la reserva.
 * @param createdAt  momento de creación.
 * @param expiresAt  momento de vencimiento.
 */
public record StockReservation(
        StockReservationId id,
        int quantity,
        ReservationStatus status,
        Instant createdAt,
        Instant expiresAt
) {

    public StockReservation {
        Objects.requireNonNull(id, "id no puede ser null");
        Objects.requireNonNull(status, "status no puede ser null");
        Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        Objects.requireNonNull(expiresAt, "expiresAt no puede ser null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity debe ser positivo");
        }
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    public StockReservation withStatus(ReservationStatus newStatus) {
        return new StockReservation(id, quantity, newStatus, createdAt, expiresAt);
    }
}