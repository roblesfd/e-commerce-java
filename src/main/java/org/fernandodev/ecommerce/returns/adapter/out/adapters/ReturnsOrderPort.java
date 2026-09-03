package org.fernandodev.ecommerce.returns.adapter.out.adapters;

import java.time.Instant;
import java.util.List;

import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatusEntry;
import org.fernandodev.ecommerce.returns.application.port.out.OrderPort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link OrderPort} hacia el contexto Order Management.
 */
@Component
@RequiredArgsConstructor
public class ReturnsOrderPort implements OrderPort {

    private final OrderUseCases orders;

    @Override
    public OrderInfo getOrder(OrderId orderId) {
        Order order = orders.getById(orderId);
        Instant deliveredAt = order.getStatusHistory().stream()
                .filter(entry -> entry.status() == OrderStatus.DELIVERED)
                .map(OrderStatusEntry::at)
                .findFirst()
                .orElse(null);
        List<ItemInfo> items = order.getItems().stream()
                .map(item -> new ItemInfo(item.id().value(), item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        return new OrderInfo(order.getCustomerId(), order.getStatus().name(), deliveredAt, items);
    }
}