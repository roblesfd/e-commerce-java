package org.fernandodev.ecommerce.order.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.application.port.out.OrderRepository;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItem;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItemId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaOrderRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private OrderRepository repository;

    @Test
    void guardaYRecuperaOrdenConItemsEHistorial() {
        UUID customerId = UUID.randomUUID();
        Order order = Order.create(OrderId.generate(), customerId,
                List.of(item()), UUID.randomUUID(), "Calle 1, Madrid, España", "WELCOME10",
                usd(1800), usd(180), usd(1620), T1);
        order.confirm(T1);

        repository.save(order);
        Order loaded = repository.findById(order.getId()).orElseThrow();

        assertThat(loaded.getCustomerId()).isEqualTo(customerId);
        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(loaded.getItems()).hasSize(1);
        assertThat(loaded.getItems().get(0).sku()).isEqualTo("CAMISA");
        assertThat(loaded.getItems().get(0).unitPrice()).isEqualTo(usd(900));
        assertThat(loaded.getSubtotal()).isEqualTo(usd(1800));
        assertThat(loaded.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(loaded.getStatusHistory()).hasSize(2);
    }

    @Test
    void findByCustomerIdDevuelveSoloLasDelCliente() {
        UUID customerId = UUID.randomUUID();
        Order order = repository.save(Order.create(OrderId.generate(), customerId,
                List.of(item()), null, null, null, usd(900), usd(0), usd(900), T1));
        repository.save(Order.create(OrderId.generate(), UUID.randomUUID(),
                List.of(item()), null, null, null, usd(900), usd(0), usd(900), T1));

        assertThat(repository.findByCustomerId(customerId))
                .extracting(Order::getId)
                .containsExactly(order.getId());
    }

    private static OrderItem item() {
        return new OrderItem(OrderItemId.generate(), ProductId.generate(), null, "CAMISA", "Camisa",
                2, usd(900));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}