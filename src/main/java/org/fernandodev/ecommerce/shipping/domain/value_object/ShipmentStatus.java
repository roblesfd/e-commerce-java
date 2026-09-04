package org.fernandodev.ecommerce.shipping.domain.value_object;

/**
 * Representa los estados que puede atravesar un {@code Shipment}
 * durante su ciclo de vida dentro del contexto de Shipping.
 *
 * <p>El estado actual determina la situación del envío y permite
 * controlar las transiciones válidas desde su preparación hasta
 * su entrega, cancelación o fallo.</p>
 *
 * <p>El flujo habitual de un envío comienza en {@link #PENDING},
 * continúa con su preparación y despacho, y posteriormente avanza
 * por los estados de tránsito hasta alcanzar {@link #DELIVERED}.</p>
 *
 * <p>{@link #DELIVERED}, {@link #FAILED} y {@link #CANCELLED}
 * representan estados terminales, por lo que un envío que alcanza
 * cualquiera de ellos no debe continuar su ciclo de vida.</p>
 */
public enum ShipmentStatus {

    /**
     * Indica que el envío fue creado y está pendiente de ser preparado.
     */
    PENDING,

    /**
     * Indica que el envío se encuentra en proceso de preparación
     * para ser despachado.
     */
    PREPARING,

    /**
     * Indica que el envío fue preparado y entregado al carrier
     * para iniciar su proceso de transporte.
     */
    SHIPPED,

    /**
     * Indica que el envío se encuentra actualmente en tránsito
     * hacia su destino.
     */
    IN_TRANSIT,

    /**
     * Indica que el envío llegó a la etapa final de distribución
     * y se encuentra próximo a ser entregado al destinatario.
     */
    OUT_FOR_DELIVERY,

    /**
     * Indica que el envío fue entregado correctamente al destinatario.
     *
     * <p>Este es un estado terminal.</p>
     */
    DELIVERED,

    /**
     * Indica que ocurrió un fallo que impidió completar correctamente
     * el proceso de envío.
     *
     * <p>Este es un estado terminal.</p>
     */
    FAILED,

    /**
     * Indica que el envío fue cancelado antes de completar su ciclo
     * de entrega.
     *
     * <p>Este es un estado terminal.</p>
     */
    CANCELLED
}
