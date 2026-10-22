package org.fernandodev.ecommerce.order.domain.value_object;

/**
 * Ciclo de vida de una orden desde su creación hasta su entrega,
 * cancelación o devolución.
 */
public enum OrderStatus {

    /**
     * Orden creada, pero aún no confirmada.
     *
     * En este estado se realizan las validaciones iniciales y,
     * dependiendo del flujo de Checkout/Payment, puede estar
     * pendiente de confirmación del pago.
     */
    PENDING,

    /**
     * Orden confirmada y aceptada para su procesamiento.
     *
     * El pedido ha superado las validaciones necesarias y el pago
     * ha sido confirmado según las reglas del sistema.
     */
    CONFIRMED,

    /**
     * Orden en proceso de preparación/fulfillment.
     *
     * Representa el momento en que el sistema o la operación de
     * fulfillment comienza a preparar los productos del pedido.
     */
    PROCESSING,

    /**
     * Orden preparada y lista para ser entregada al transportista.
     *
     * Los productos han sido preparados y el pedido puede pasar
     * al proceso de Shipping.
     */
    READY_TO_SHIP,

    /**
     * Orden enviada y entregada al transportista.
     *
     * El shipment ha sido creado y el pedido salió del control
     * directo del almacén/comercio.
     */
    SHIPPED,

    /**
     * Orden entregada exitosamente al cliente.
     *
     * Representa la finalización normal del ciclo de fulfillment
     * de la orden.
     */
    DELIVERED,

    /**
     * Orden cancelada antes de completar su ciclo normal.
     *
     * Puede ocurrir por cancelación del cliente, fallo del proceso,
     * falta de stock, problemas de pago u otras reglas de negocio.
     */
    CANCELLED,

    /**
     * El cliente ha solicitado devolver uno o más productos
     * asociados a la orden.
     *
     * El procesamiento de la devolución pertenece al contexto
     * Returns & Refunds.
     */
    RETURN_REQUESTED,

    /**
     * La devolución de la orden ha sido completada.
     *
     * Representa que el proceso de devolución correspondiente
     * terminó satisfactoriamente.
     */
    RETURNED
}
