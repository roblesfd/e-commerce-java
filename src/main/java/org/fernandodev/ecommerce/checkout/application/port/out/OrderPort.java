package org.fernandodev.ecommerce.checkout.application.port.out;

import org.fernandodev.ecommerce.checkout.application.dto.OrderData;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Puerto hacia el contexto Order Management: crear/confirmar/cancelar órdenes.
 */
public interface OrderPort {

    OrderId createOrder(OrderData data);

    void confirm(OrderId orderId);

    void cancel(OrderId orderId);
}