package org.fernandodev.ecommerce.shipping.adapter.out;

import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.application.port.out.OrderPort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link OrderPort} consultando el contexto Order
 * Management.
 */
@Component
@RequiredArgsConstructor
public class ShippingOrderPort implements OrderPort {

    private final OrderUseCases orderUseCases;

    @Override
    public OrderInfo getOrder(OrderId orderId) {
        Order order = orderUseCases.getById(orderId);
        return new OrderInfo(order.getCustomerId(), order.getStatus().name());
    }

    @Override
    public void markShipped(OrderId orderId) {
        orderUseCases.markShipped(orderId);
    }

    @Override
    public void markDelivered(OrderId orderId) {
        orderUseCases.markDelivered(orderId);
    }
}