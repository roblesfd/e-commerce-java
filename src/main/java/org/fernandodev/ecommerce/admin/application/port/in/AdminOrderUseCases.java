package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Orquestación administrativa de órdenes.
 */
public interface AdminOrderUseCases {

/**
     * Lista todas las órdenes.
     */
    List<Order> listAllOrders();

/**
     * Consulta el detalle de una orden.
     */
    Order getOrder(OrderId id);

/**
     * Cancela una orden y lo registra en auditoría.
     */
    Order cancelOrder(OrderId id);

/**
     * Marca la orden como enviada y lo registra en auditoría.
     */
    Order markOrderShipped(OrderId id);

/**
     * Marca la orden como entregada y lo registra en auditoría.
     */
    Order markOrderDelivered(OrderId id);
}