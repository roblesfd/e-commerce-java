package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Orquestación administrativa de órdenes.
 */
public interface AdminOrderUseCases {

    List<Order> listAllOrders();

    Order getOrder(OrderId id);

    Order cancelOrder(OrderId id);

    Order markOrderShipped(OrderId id);

    Order markOrderDelivered(OrderId id);
}