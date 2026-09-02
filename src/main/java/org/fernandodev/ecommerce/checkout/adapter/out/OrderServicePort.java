package org.fernandodev.ecommerce.checkout.adapter.out;

import org.fernandodev.ecommerce.checkout.application.dto.OrderData;
import org.fernandodev.ecommerce.checkout.application.port.out.OrderPort;
import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.application.dto.OrderItemCommand;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link OrderPort} consultando el contexto Order
 * Management.
 */
@Component
@RequiredArgsConstructor
public class OrderServicePort implements OrderPort {

    private final OrderUseCases orderUseCases;

    @Override
    public OrderId createOrder(OrderData data) {
        return orderUseCases.create(new CreateOrderCommand(
                data.customerId(),
                data.addressId(),
                data.addressSnapshot(),
                data.couponCode(),
                data.items().stream()
                        .map(item -> new OrderItemCommand(
                                item.productId(), item.variantId(), item.sku(), item.name(),
                                item.quantity(), item.unitPrice()))
                        .toList(),
                data.subtotal(),
                data.discounts(),
                data.total()))
                .getId();
    }

    @Override
    public void confirm(OrderId orderId) {
        orderUseCases.confirm(orderId);
    }

    @Override
    public void cancel(OrderId orderId) {
        orderUseCases.cancel(orderId);
    }
}