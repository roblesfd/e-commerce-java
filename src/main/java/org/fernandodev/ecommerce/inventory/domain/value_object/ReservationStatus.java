package org.fernandodev.ecommerce.inventory.domain.value_object;

/**
 * Estado del ciclo de vida de una reserva de stock.
 */
public enum ReservationStatus {

    /** Reserva vigente: retiene stock disponible. */
    ACTIVE,

    /** Reserva consumida: el stock se descuenta del inventario (orden creada). */
    CONFIRMED,

    /** Reserva cancelada: el stock vuelve a estar disponible. */
    RELEASED,

    /** Reserva vencida: el stock vuelve a estar disponible. */
    EXPIRED
}