package org.fernandodev.ecommerce.inventory.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Movimiento de inventario (auditoría).
 *
 * @param type     tipo de movimiento.
 * @param quantity delta aplicado (positivo o negativo según {@link StockMovementType}).
 * @param reason   motivo del movimiento (opcional).
 * @param at       momento en que ocurrió.
 */
public record StockMovement(StockMovementType type, int quantity, String reason, Instant at) {

    public StockMovement {
        Objects.requireNonNull(type, "type no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}