package org.fernandodev.ecommerce.returns.domain.value_object;

/**
 * Estado del reembolso solicitado al contexto Payment.
 */
public enum RefundStatus {

    PENDING,
    REFUNDED,
    FAILED
}