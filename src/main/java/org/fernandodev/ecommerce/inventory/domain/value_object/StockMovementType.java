package org.fernandodev.ecommerce.inventory.domain.value_object;

/**
 * Tipo de movimiento de inventario.
 * <p>
 * Los movimientos de stock ({@code INCREASE/DECREASE/ADJUST}) reflejan el delta
 * aplicado a {@code onHand}; los de reserva ({@code RESERVE/CONFIRM/RELEASE/EXPIRE})
 * reflejan el delta aplicado a {@code reserved}.
 * </p>
 */
public enum StockMovementType {

    INCREASE,
    DECREASE,
    ADJUST,
    RESERVE,
    CONFIRM,
    RELEASE,
    EXPIRE
}