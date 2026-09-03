package org.fernandodev.ecommerce.returns.domain.value_object;

/**
 * Ciclo de vida de una solicitud de devolución.
 */
public enum ReturnStatus {

    REQUESTED,
    APPROVED,
    REJECTED,
    IN_TRANSIT,
    RECEIVED,
    INSPECTED,
    COMPLETED,
    CANCELLED
}