package org.fernandodev.ecommerce.returns.domain.value_object;

/**
 * Estado del reembolso solicitado al contexto Payment.
 *
 * <p>Este estado representa el resultado del proceso de reembolso
 * asociado a una devolución.</p>
 */
public enum RefundStatus {

    /**
     * El reembolso ha sido solicitado al contexto Payment,
     * pero aún no ha sido procesado o confirmado.
     */
    PENDING,

    /**
     * El reembolso ha sido procesado correctamente por Payment.
     */
    REFUNDED,

    /**
     * El procesamiento del reembolso ha fallado.
     */
    FAILED
}
