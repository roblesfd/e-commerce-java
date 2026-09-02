package org.fernandodev.ecommerce.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.application.dto.OrderItemCommand;
import org.fernandodev.ecommerce.order.application.port.out.OrderRepository;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private final FakeOrderRepository repository = new FakeOrderRepository();
    private final OrderService service = new OrderService(repository);

    @Test
    void createGuardaOrden() {
        Order order = service.create(comando());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        assertThat(repository.findById(order.getId())).contains(order);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(OrderId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmCambiaElEstado() {
        Order order = service.create(comando());

        Order confirmed = service.confirm(order.getId());

        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void cancelCambiaElEstado() {
        Order order = service.create(comando());

        Order cancelled = service.cancel(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void listByCustomerDevuelveSoloLasDelCliente() {
        UUID customerId = UUID.randomUUID();
        service.create(new CreateOrderCommand(customerId, null, null, null,
                List.of(item()), usd(900), usd(0), usd(900)));
        service.create(new CreateOrderCommand(UUID.randomUUID(), null, null, null,
                List.of(item()), usd(900), usd(0), usd(900)));

        List<Order> result = service.listByCustomer(customerId);

        assertThat(result).hasSize(1);
    }

    private static CreateOrderCommand comando() {
        return new CreateOrderCommand(UUID.randomUUID(), null, null, null,
                List.of(item()), usd(1800), usd(180), usd(1620));
    }

    private static OrderItemCommand item() {
        return new OrderItemCommand(ProductId.generate(), null, "CAMISA", "Camisa", 2, usd(900));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeOrderRepository implements OrderRepository {

        private final Map<OrderId, Order> byId = new HashMap<>();

        @Override
        public Order save(Order order) {
            byId.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Order> findByCustomerId(UUID customerId) {
            return byId.values().stream().filter(o -> o.getCustomerId().equals(customerId)).toList();
        }
    }
}