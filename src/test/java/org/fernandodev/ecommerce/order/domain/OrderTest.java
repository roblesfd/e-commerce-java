package org.fernandodev.ecommerce.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItem;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItemId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    void createCreaOrdenPendingConSnapshots() {
        Order order = orden();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).unitPrice()).isEqualTo(usd(900));
        assertThat(order.getSubtotal()).isEqualTo(usd(1800));
        assertThat(order.getDiscounts()).isEqualTo(usd(180));
        assertThat(order.getTotal()).isEqualTo(usd(1620));
        assertThat(order.getStatusHistory()).hasSize(1);
    }

    @Test
    void createSinItemsLanzaExcepcion() {
        assertThatThrownBy(() -> Order.create(OrderId.generate(), UUID.randomUUID(), List.of(),
                null, null, null, usd(0), usd(0), usd(0), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createConTotalesInconsistentesLanzaExcepcion() {
        assertThatThrownBy(() -> Order.create(OrderId.generate(), UUID.randomUUID(),
                List.of(item()), null, null, null, usd(1000), usd(100), usd(850), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void confirmCambiaEstadoYRegistraHistorial() {
        Order order = orden();

        order.confirm(T2);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getStatusHistory()).hasSize(2);
    }

    @Test
    void confirmarOrdenConfirmadaLanzaExcepcion() {
        Order order = orden();
        order.confirm(T2);

        assertThatThrownBy(() -> order.confirm(T2))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cancelDesdePendingOConfirmadaEsValido() {
        Order pending = orden();
        pending.cancel(T1);
        assertThat(pending.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        Order confirmada = orden();
        confirmada.confirm(T1);
        confirmada.cancel(T2);
        assertThat(confirmada.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelarOrdenCanceladaLanzaExcepcion() {
        Order order = orden();
        order.cancel(T1);

        assertThatThrownBy(() -> order.cancel(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cicloDeFulfillmentCompleto() {
        Order order = orden();

        order.confirm(T1);
        order.process(T1);
        order.markReadyToShip(T1);
        order.markShipped(T1);
        order.markDelivered(T1);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getStatusHistory()).hasSize(6);
        assertThat(order.getStatusHistory())
                .extracting(entry -> entry.status())
                .containsExactly(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING,
                        OrderStatus.READY_TO_SHIP, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    @Test
    void transicionesDeFulfillmentInvalidasLanzanExcepcion() {
        Order order = orden();

        assertThatThrownBy(() -> order.markShipped(T1))
                .isInstanceOf(BusinessRuleViolationException.class);

        order.confirm(T1);
        order.process(T1);
        assertThatThrownBy(() -> order.markDelivered(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Order orden() {
        return Order.create(OrderId.generate(), UUID.randomUUID(),
                List.of(item()), null, null, "WELCOME10", usd(1800), usd(180), usd(1620), T1);
    }

    private static OrderItem item() {
        return new OrderItem(OrderItemId.generate(), ProductId.generate(), null, "CAMISA", "Camisa",
                2, usd(900));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}