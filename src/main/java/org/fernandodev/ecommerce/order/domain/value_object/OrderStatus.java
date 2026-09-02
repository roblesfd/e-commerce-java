package org.fernandodev.ecommerce.order.domain.value_object;

/**
 * Ciclo de vida de una orden.
 * <p>
 * Las transiciones accesibles en el alcance actual son
 * {@code PENDING → CONFIRMED}, {@code PENDING → CANCELLED} y
 * {@code CONFIRMED → CANCELLED}. El resto se integra con la cadena de
 * fulfillment y envíos en fases posteriores.
 * </p>
 */
public enum OrderStatus {

    PENDING,
    CONFIRMED,
    PROCESSING,
    READY_TO_SHIP,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURNED
}