package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnRepository;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnItem;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaReturnRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ReturnRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaDevolucionConItems() {
        UUID orderItemId = UUID.randomUUID();
        Return aReturn = devolucion(orderItemId);

        repository.save(aReturn);
        Return loaded = repository.findById(aReturn.getId()).orElseThrow();

        assertThat(loaded.getStatus()).isEqualTo(ReturnStatus.REQUESTED);
        assertThat(loaded.getReason()).isEqualTo(ReturnReason.DEFECTIVE);
        assertThat(loaded.getItems()).hasSize(1);
        assertThat(loaded.getItems().get(0).orderItemId()).isEqualTo(orderItemId);
        assertThat(loaded.getStatusHistory()).hasSize(1);
    }

    @Test
    void existsByOrderIdDevuelveTrueTrasGuardar() {
        OrderId orderId = OrderId.generate();
        repository.save(Return.create(ReturnId.generate(), orderId, UUID.randomUUID(),
                ReturnReason.DEFECTIVE, List.of(item(UUID.randomUUID())), AT));

        assertThat(repository.existsByOrderId(orderId)).isTrue();
        assertThat(repository.existsByOrderId(OrderId.generate())).isFalse();
    }

    @Test
    void findByOrderIdYPorCustomerDevuelvenLaDevolucion() {
        UUID customerId = UUID.randomUUID();
        OrderId orderId = OrderId.generate();
        Return aReturn = Return.create(ReturnId.generate(), orderId, customerId,
                ReturnReason.DEFECTIVE, List.of(item(UUID.randomUUID())), AT);
        repository.save(aReturn);

        assertThat(repository.findByOrderId(orderId))
                .map(Return::getOrderId).contains(orderId);
        assertThat(repository.findByCustomerId(customerId))
                .map(Return::getCustomerId).contains(customerId);
    }

    private static Return devolucion(UUID orderItemId) {
        return Return.create(ReturnId.generate(), OrderId.generate(), UUID.randomUUID(),
                ReturnReason.DEFECTIVE, List.of(item(orderItemId)), AT);
    }

    private static ReturnItem item(UUID orderItemId) {
        return new ReturnItem(orderItemId, ProductId.generate(), 1, usd(900));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}