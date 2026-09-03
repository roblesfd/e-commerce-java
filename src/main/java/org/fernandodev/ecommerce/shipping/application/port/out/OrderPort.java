package org.fernandodev.ecommerce.shipping.application.port.out;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Puerto hacia el contexto Order Management: consultar y avanzar el estado de
 * fulfillment de la orden.
 */
public interface OrderPort {

    OrderInfo getOrder(OrderId orderId);

    void markShipped(OrderId orderId);

    void markDelivered(OrderId orderId);

    record OrderInfo(java.util.UUID customerId, String status) {
    }
}