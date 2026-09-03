package org.fernandodev.ecommerce.admin.application.service;

import java.util.List;

import org.fernandodev.ecommerce.admin.application.port.in.AdminOrderUseCases;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de órdenes.
 */
@RequiredArgsConstructor
public class AdminOrderService implements AdminOrderUseCases {

    private final OrderUseCases orders;
    private final AuditService audit;

    @Override
    public List<Order> listAllOrders() {
        return orders.listAll();
    }

    @Override
    public Order getOrder(OrderId id) {
        return orders.getById(id);
    }

    @Override
    @Transactional
    public Order cancelOrder(OrderId id) {
        audit.record("CANCEL_ORDER", "order", id.value(), "Orden cancelada por administración");
        return orders.cancel(id);
    }

    @Override
    @Transactional
    public Order markOrderShipped(OrderId id) {
        audit.record("MARK_ORDER_SHIPPED", "order", id.value(), "Orden marcada como enviada");
        return orders.markShipped(id);
    }

    @Override
    @Transactional
    public Order markOrderDelivered(OrderId id) {
        audit.record("MARK_ORDER_DELIVERED", "order", id.value(), "Orden marcada como entregada");
        return orders.markDelivered(id);
    }
}