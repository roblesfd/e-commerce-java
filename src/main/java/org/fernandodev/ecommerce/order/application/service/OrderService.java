package org.fernandodev.ecommerce.order.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.application.dto.OrderItemCommand;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.application.port.out.OrderRepository;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItem;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItemId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Order Management.
 */
@RequiredArgsConstructor
public class OrderService implements OrderUseCases {

    private final OrderRepository orders;

    @Override
    public Order create(CreateOrderCommand command) {
        List<OrderItem> items = command.items().stream()
                .map(OrderService::toDomainItem)
                .toList();
        Order order = Order.create(
                OrderId.generate(),
                command.customerId(),
                items,
                command.addressId(),
                command.addressSnapshot(),
                command.couponCode(),   
                command.subtotal(),
                command.discounts(),
                command.total(),
                Instant.now());
        return orders.save(order);
    }

    @Override
    public Order getById(OrderId id) {
        return getExisting(id);
    }

    @Override
    public List<Order> listByCustomer(UUID customerId) {
        return orders.findByCustomerId(customerId);
    }

    @Override
    public List<Order> listAll() {
        return orders.findAll();
    }

    @Override
    public Order confirm(OrderId id) {
        Order order = getExisting(id);
        order.confirm(Instant.now());
        return orders.save(order);
    }

    @Override
    public Order process(OrderId id) {
        Order order = getExisting(id);
        order.process(Instant.now());
        return orders.save(order);
    }

    @Override
    public Order markReadyToShip(OrderId id) {
        Order order = getExisting(id);
        order.markReadyToShip(Instant.now());
        return orders.save(order);
    }

    @Override
    public Order markShipped(OrderId id) {
        Order order = getExisting(id);
        order.markShipped(Instant.now());
        return orders.save(order);
    }

    @Override
    public Order markDelivered(OrderId id) {
        Order order = getExisting(id);
        order.markDelivered(Instant.now());
        return orders.save(order);
    }

    @Override
    public Order cancel(OrderId id) {
        Order order = getExisting(id);
        order.cancel(Instant.now());
        return orders.save(order);
    }

    private Order getExisting(OrderId id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    private static OrderItem toDomainItem(OrderItemCommand command) {
        return new OrderItem(
                OrderItemId.generate(),
                command.productId(),
                command.variantId(),
                command.sku(),
                command.name(),
                command.quantity(),
                command.unitPrice());
    }
}
