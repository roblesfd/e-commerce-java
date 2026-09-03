package org.fernandodev.ecommerce.order.domain.value_object;

/**
 * Ciclo de vida de una orden.
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
