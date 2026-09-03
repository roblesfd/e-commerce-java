package org.fernandodev.ecommerce.shipping.domain.value_object;

/**
 * Ciclo de vida de un envío.
 */
public enum ShipmentStatus {

    PENDING,
    PREPARING,
    SHIPPED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED,
    CANCELLED
}