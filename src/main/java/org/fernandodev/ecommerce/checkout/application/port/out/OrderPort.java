package org.fernandodev.ecommerce.checkout.application.port.out;

import org.fernandodev.ecommerce.checkout.application.dto.OrderData;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Puerto hacia el contexto Order Management: crear/confirmar/cancelar órdenes.
 */
public interface OrderPort {

/**
     * Crea una orden en estado {@code PENDING}.
     *
     * @param data datos de la orden.
     * @return el identificador de la orden creada.
     */
    OrderId createOrder(OrderData data);

/**
     * Confirma la orden.
     *
     * @param orderId identificador de la orden.
     */
    void confirm(OrderId orderId);

/**
     * Cancela la orden.
     *
     * @param orderId identificador de la orden.
     */
    void cancel(OrderId orderId);
}