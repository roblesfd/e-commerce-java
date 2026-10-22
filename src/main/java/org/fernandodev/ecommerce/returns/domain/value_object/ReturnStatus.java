package org.fernandodev.ecommerce.returns.domain.value_object;

/**
 * Ciclo de vida de una solicitud de devolución.
 *
 * <p>Representa las diferentes etapas por las que puede pasar una
 * solicitud de devolución desde que es creada hasta que finaliza.</p>
 */
public enum ReturnStatus {

    /**
     * La solicitud de devolución ha sido creada y está pendiente
     * de revisión o aprobación.
     */
    REQUESTED,

    /**
     * La solicitud de devolución ha sido aprobada y puede continuar
     * con el proceso de devolución.
     */
    APPROVED,

    /**
     * La solicitud de devolución ha sido rechazada y no puede
     * continuar con el proceso.
     */
    REJECTED,

    /**
     * El producto devuelto ha sido entregado al transportista
     * y se encuentra en proceso de traslado hacia el vendedor.
     */
    IN_TRANSIT,

    /**
     * El producto devuelto ha sido recibido por el vendedor
     * o centro de devolución.
     */
    RECEIVED,

    /**
     * El producto recibido se encuentra en proceso de inspección
     * para determinar si cumple con las condiciones de devolución.
     */
    INSPECTED,

    /**
     * El proceso de devolución ha finalizado correctamente.
     */
    COMPLETED,

    /**
     * La solicitud de devolución ha sido cancelada antes
     * de completar el proceso.
     */
    CANCELLED
}
